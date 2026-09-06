package com.example.domain.telegram

object TelegramBridgeScript {

    const val TELEGRAM_DEFAULT_PORT = 8088

    val INSTALL_COMMAND = """
pkg update -y && pkg install python -y && pip install telethon aiohttp
    """.trimIndent()

    val LAUNCH_COMMAND = """
curl -sSL -o telegram_bridge.py http://127.0.0.1:8081/telegram_bridge.py && python telegram_bridge.py
    """.trimIndent()

    val COMPLETE_TERMUX_COMMAND = """
pkg update -y && pkg install python -y && pip install telethon aiohttp && curl -sSL -o telegram_bridge.py http://127.0.0.1:8081/telegram_bridge.py && python telegram_bridge.py
    """.trimIndent()

    val PYTHON_BRIDGE_SCRIPT = """# =========================================================================
# AI Edge - Telegram Telethon Bridge for Termux / Local Python Server
# Port d'écoute HTTP : 8088 | Relais vers Android : 8081 / 8080 / 8082
# =========================================================================

import asyncio
import json
import os
import sys
import aiohttp
from aiohttp import web
from telethon import TelegramClient, events
from telethon.tl.types import Channel, Chat

PORT = int(os.environ.get('PORT', 8088))
SESSION_NAME = os.environ.get('TG_SESSION', 'telethon_session')

client = None
phone_code_hash_cache = {}
monitored_channels = set()
listener_attached = False

app_state = {
    "api_id": None,
    "api_hash": None,
    "phone": None,
    "status": "DISCONNECTED"
}

async def forward_to_android(endpoint, payload):
    for p in [8081, 8080, 8082]:
        try:
            async with aiohttp.ClientSession() as session:
                async with session.post(f"http://127.0.0.1:{p}{endpoint}", json=payload, timeout=aiohttp.ClientTimeout(total=3.0)) as resp:
                    if resp.status in [200, 201, 204]:
                        return True
        except Exception:
            pass
    return False

async def log_to_android(level, message, source="Telethon"):
    print(f"[{level}] {message}", flush=True)
    await forward_to_android("/api/telegram/log", {
        "level": level,
        "source": source,
        "message": message
    })

def attach_telethon_listener(tg):
    global listener_attached
    if listener_attached:
        return
    
    @tg.on(events.NewMessage)
    async def new_message_handler(event):
        try:
            chat = await event.get_chat()
            chat_id = event.chat_id
            
            # Filtre de surveillance si spécifié
            if monitored_channels and chat_id not in monitored_channels:
                return

            title = getattr(chat, 'title', '') or getattr(chat, 'first_name', 'Fournisseur Telegram')
            username = getattr(chat, 'username', '') or ''
            text = event.raw_text or ''
            
            media_type = "none"
            if event.photo:
                media_type = "photo"
            elif event.document:
                media_type = "document"

            sender = await event.get_sender()
            sender_name = getattr(sender, 'first_name', '') or title

            payload = {
                "channel_id": chat_id,
                "channel_title": title,
                "channel_username": username,
                "message_id": event.id,
                "sender_id": event.sender_id or 0,
                "sender_name": sender_name,
                "text": text,
                "media_type": media_type,
                "timestamp": int(event.date.timestamp() * 1000)
            }

            await log_to_android("INCOMING", f"Message reçu sur [{title}]: {text[:80]}...")
            await forward_to_android("/api/telegram/message", payload)
        except Exception as e:
            await log_to_android("ERROR", f"Erreur traitement NewMessage: {str(e)}")

    listener_attached = True
    print("✅ Écouteur de messages Telethon attaché avec succès.")

async def get_client(api_id, api_hash):
    global client
    if client is None or client.api_id != api_id:
        if client:
            await client.disconnect()
        client = TelegramClient(SESSION_NAME, int(api_id), str(api_hash))
        await client.connect()
        attach_telethon_listener(client)
    elif not client.is_connected():
        await client.connect()
        attach_telethon_listener(client)
    return client

async def handle_status(request):
    global client
    is_auth = False
    user_data = None
    if client and client.is_connected():
        try:
            is_auth = await client.is_user_authorized()
            if is_auth:
                me = await client.get_me()
                user_data = {
                    "id": me.id,
                    "first_name": me.first_name or "",
                    "last_name": me.last_name or "",
                    "username": me.username or "",
                    "phone": me.phone or ""
                }
                attach_telethon_listener(client)
        except Exception as e:
            is_auth = False
            
    return web.json_response({
        "online": True,
        "port": PORT,
        "authenticated": is_auth,
        "status": "CONNECTED" if is_auth else "READY",
        "monitored_channels_count": len(monitored_channels),
        "user": user_data
    })

async def handle_send_code(request):
    try:
        data = await request.json()
        api_id = data.get("api_id")
        api_hash = data.get("api_hash")
        phone = data.get("phone")
        
        if not api_id or not api_hash or not phone:
            return web.json_response({"success": False, "error": "api_id, api_hash et phone requis"}, status=400)
            
        tg = await get_client(api_id, api_hash)
        result = await tg.send_code_request(phone)
        phone_code_hash_cache[phone] = result.phone_code_hash
        app_state["phone"] = phone
        app_state["api_id"] = api_id
        app_state["api_hash"] = api_hash
        
        return web.json_response({
            "success": True,
            "phone": phone,
            "phone_code_hash": result.phone_code_hash,
            "message": "Code de vérification envoyé sur Telegram / SMS"
        })
    except Exception as e:
        return web.json_response({"success": False, "error": str(e)}, status=500)

async def handle_sign_in(request):
    try:
        data = await request.json()
        phone = data.get("phone") or app_state.get("phone")
        code = data.get("code")
        phone_code_hash = data.get("phone_code_hash") or phone_code_hash_cache.get(phone, "")
        password = data.get("password") # Pour 2FA si activé
        
        if not phone or not code:
            return web.json_response({"success": False, "error": "phone et code requis"}, status=400)
            
        tg = await get_client(app_state["api_id"], app_state["api_hash"])
        
        try:
            user = await tg.sign_in(phone=phone, code=code, phone_code_hash=phone_code_hash)
        except Exception as auth_err:
            if "Two-steps verification" in str(auth_err) or "SessionPasswordNeeded" in str(auth_err):
                if password:
                    user = await tg.sign_in(password=password)
                else:
                    return web.json_response({
                        "success": False, 
                        "requires_password": True,
                        "error": "Mot de passe 2FA (Double Authentification) requis"
                    }, status=401)
            else:
                raise auth_err
                
        me = await tg.get_me()
        return web.json_response({
            "success": True,
            "user": {
                "id": me.id,
                "first_name": me.first_name or "",
                "last_name": me.last_name or "",
                "username": me.username or "",
                "phone": me.phone or ""
            }
        })
    except Exception as e:
        return web.json_response({"success": False, "error": str(e)}, status=500)

async def handle_get_channels(request):
    try:
        global client
        if not client or not client.is_connected() or not await client.is_user_authorized():
            return web.json_response({"success": False, "error": "Non authentifié sur Telegram"}, status=401)
            
        dialogs = []
        async for dialog in client.iter_dialogs():
            if dialog.is_channel or dialog.is_group:
                entity = dialog.entity
                member_count = getattr(entity, 'participants_count', 0) or 0
                dialogs.append({
                    "id": dialog.id,
                    "title": dialog.title or "Sans nom",
                    "username": getattr(entity, 'username', '') or "",
                    "is_channel": dialog.is_channel,
                    "is_group": dialog.is_group,
                    "unread_count": dialog.unread_count,
                    "member_count": member_count
                })
                
        return web.json_response({
            "success": True,
            "channels": dialogs
        })
    except Exception as e:
        return web.json_response({"success": False, "error": str(e)}, status=500)

async def handle_disconnect(request):
    try:
        global client
        if client and client.is_connected():
            await client.log_out()
            await client.disconnect()
            client = None
        return web.json_response({"success": True, "message": "Déconnecté avec succès"})
    except Exception as e:
        return web.json_response({"success": False, "error": str(e)}, status=500)

async def handle_monitor_channels(request):
    try:
        data = await request.json()
        channel_ids = data.get("channel_ids", [])
        global monitored_channels
        monitored_channels = set(int(c) for c in channel_ids)
        return web.json_response({
            "success": True,
            "monitored_count": len(monitored_channels)
        })
    except Exception as e:
        return web.json_response({"success": False, "error": str(e)}, status=500)

async def handle_simulate_incoming(request):
    try:
        data = await request.json() if request.can_read_body else {}
        fake_msg = {
            "channel_id": data.get("channel_id", -1001928374821),
            "channel_title": data.get("channel_title", "📦 Fournisseurs Drop & Gros (Paris/Dubai)"),
            "channel_username": data.get("channel_username", "grossistes_dropship_officiel"),
            "message_id": int(asyncio.get_event_loop().time() * 1000),
            "sender_id": 99887766,
            "sender_name": "Grossiste Dubai",
            "text": data.get("text", "🔥 Arrivage Immédiat : Montre connectée AMOLED IP68 étanche avec 3 bracelets. Prix d'achat: 14.50€ | Prix conseillé: 49.90€. Stock Paris: 250 pièces dispo."),
            "media_type": data.get("media_type", "photo"),
            "timestamp": int(asyncio.get_event_loop().time() * 1000)
        }
        await log_to_android("INCOMING", f"[Simulation] Message arrivage: {fake_msg['text'][:60]}...")
        ok = await forward_to_android("/api/telegram/message", fake_msg)
        return web.json_response({"success": ok, "message": fake_msg})
    except Exception as e:
        return web.json_response({"success": False, "error": str(e)}, status=500)

def init_app():
    app = web.Application()
    app.router.add_get('/', lambda r: web.Response(text="Telegram Telethon Bridge Active"))
    app.router.add_get('/status', handle_status)
    app.router.add_post('/auth/send-code', handle_send_code)
    app.router.add_post('/auth/sign-in', handle_sign_in)
    app.router.add_get('/channels', handle_get_channels)
    app.router.add_post('/channels/monitor', handle_monitor_channels)
    app.router.add_post('/simulate/incoming', handle_simulate_incoming)
    app.router.add_post('/disconnect', handle_disconnect)
    return app

if __name__ == '__main__':
    print(f"🚀 Telegram Telethon Bridge démarré sur le port {PORT}...")
    web.run_app(init_app(), port=PORT, host='0.0.0.0')
""".trimIndent()
}
