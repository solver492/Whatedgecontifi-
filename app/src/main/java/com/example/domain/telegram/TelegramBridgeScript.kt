package com.example.domain.telegram

object TelegramBridgeScript {

    const val TELEGRAM_DEFAULT_PORT = 8088

    /**
     * Commande Termux complète d'initialisation (installation Python + dépendances + téléchargement + démarrage du bridge Python Telethon)
     */
    const val COMPLETE_TERMUX_COMMAND = "killall python 2>/dev/null ; pkg update -y && pkg install -y python curl && pip install --upgrade telethon aiohttp && mkdir -p ~/tg-bridge && cd ~/tg-bridge && (curl -s http://127.0.0.1:8081/telegram-bridge.py > telegram-bridge.py 2>/dev/null || curl -s http://127.0.0.1:8080/telegram-bridge.py > telegram-bridge.py 2>/dev/null || curl -s http://127.0.0.1:8082/telegram-bridge.py > telegram-bridge.py) && python telegram-bridge.py"

    /**
     * Commande rapide de démarrage (si Python et les packages sont déjà installés dans Termux)
     */
    const val FAST_START_COMMAND = "killall python 2>/dev/null ; mkdir -p ~/tg-bridge && cd ~/tg-bridge && (curl -s http://127.0.0.1:8081/telegram-bridge.py > telegram-bridge.py 2>/dev/null || curl -s http://127.0.0.1:8080/telegram-bridge.py > telegram-bridge.py 2>/dev/null || curl -s http://127.0.0.1:8082/telegram-bridge.py > telegram-bridge.py) && python telegram-bridge.py"

    const val INSTALL_COMMAND = COMPLETE_TERMUX_COMMAND

    const val LAUNCH_COMMAND = FAST_START_COMMAND

    val PYTHON_BRIDGE_SCRIPT = """# =========================================================================
# AI Edge - Telegram Telethon MTProto Real Bridge for Termux / Python Server
# Port d'écoute HTTP : 8088 | Relais vers Android : 8081 / 8080 / 8082
# Architecture réelle sans aucune simulation de données
# =========================================================================

import asyncio
import json
import os
import sys
import aiohttp
from aiohttp import web
from telethon import TelegramClient, events
from telethon.tl.types import Channel, Chat
from telethon.errors import SessionPasswordNeededError

PORT = int(os.environ.get('PORT', 8088))
SESSION_NAME = os.environ.get('TG_SESSION', 'telethon_session')

client = None
phone_code_hash_cache = {}
monitored_channels = set()
listener_attached = False

app_state = {
    "api_id": None,
    "api_hash": None,
    "phone": None
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
            
            # Écoute uniquement les canaux sous surveillance si une liste est active
            if monitored_channels and chat_id not in monitored_channels:
                return

            title = getattr(chat, 'title', '') or getattr(chat, 'first_name', 'Canal Telegram')
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

            await log_to_android("INCOMING", f"Message réel reçu sur [{title}]: {text[:80]}")
            await forward_to_android("/api/telegram/message", payload)
        except Exception as e:
            await log_to_android("ERROR", f"Erreur traitement NewMessage: {str(e)}")

    listener_attached = True
    print("✅ Écouteur Telethon NewMessage attaché aux flux réels.")

async def get_client(api_id, api_hash):
    global client
    if client is None or getattr(client, 'api_id', None) != int(api_id):
        if client:
            try:
                await client.disconnect()
            except Exception:
                pass
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
        except Exception:
            is_auth = False
            
    return web.json_response({
        "online": True,
        "port": PORT,
        "authenticated": is_auth,
        "status": "CONNECTED" if is_auth else "DISCONNECTED",
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
            "message": "Code de vérification envoyé sur votre compte Telegram officiel"
        })
    except Exception as e:
        # Erreur réelle renvoyée par Telegram / Telethon (ex: PhoneNumberInvalidError, ApiIdInvalidError)
        return web.json_response({"success": False, "error": str(e)}, status=400)

async def handle_sign_in(request):
    try:
        data = await request.json()
        phone = data.get("phone") or app_state.get("phone")
        code = data.get("code")
        phone_code_hash = data.get("phone_code_hash") or phone_code_hash_cache.get(phone, "")
        password = data.get("password")
        
        if not phone or not code:
            return web.json_response({"success": False, "error": "phone et code requis"}, status=400)
            
        if not app_state.get("api_id") or not app_state.get("api_hash"):
            return web.json_response({"success": False, "error": "Session non initialisée. Veuillez recommencer l'étape précédente."}, status=400)

        tg = await get_client(app_state["api_id"], app_state["api_hash"])
        
        try:
            if phone_code_hash:
                await tg.sign_in(phone=phone, code=code, phone_code_hash=phone_code_hash)
            else:
                await tg.sign_in(phone=phone, code=code)
        except SessionPasswordNeededError:
            if password:
                await tg.sign_in(password=password)
            else:
                return web.json_response({
                    "success": False, 
                    "requires_password": True,
                    "error": "Mot de passe 2FA (Double Authentification) requis"
                }, status=401)
                
        me = await tg.get_me()
        attach_telethon_listener(tg)
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
        return web.json_response({"success": False, "error": str(e)}, status=400)

async def handle_get_channels(request):
    try:
        global client
        if not client or not client.is_connected() or not await client.is_user_authorized():
            return web.json_response({"success": False, "error": "Compte non authentifié sur Telegram"}, status=401)
            
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
                    "member_count": member_count,
                    "is_monitored": dialog.id in monitored_channels
                })
                
        return web.json_response({
            "success": True,
            "channels": dialogs
        })
    except Exception as e:
        return web.json_response({"success": False, "error": str(e)}, status=500)

async def handle_watch_channel(request):
    try:
        channel_id = int(request.match_info.get('id', 0))
        data = await request.json() if request.can_read_body else {}
        active = data.get("active", True)
        global monitored_channels
        if active:
            monitored_channels.add(channel_id)
        else:
            monitored_channels.discard(channel_id)
        await log_to_android("INFO", f"Surveillance canal {channel_id}: {'ACTIVÉE' if active else 'DÉSACTIVÉE'}")
        return web.json_response({
            "success": True,
            "channel_id": channel_id,
            "active": active,
            "monitored_count": len(monitored_channels)
        })
    except Exception as e:
        return web.json_response({"success": False, "error": str(e)}, status=500)

async def handle_get_channel_messages(request):
    try:
        global client
        if not client or not client.is_connected() or not await client.is_user_authorized():
            return web.json_response({"success": False, "error": "Non authentifié sur Telegram"}, status=401)
            
        channel_id = int(request.match_info.get('id', 0))
        messages = []
        async for msg in client.iter_messages(channel_id, limit=25):
            media_type = "none"
            if msg.photo:
                media_type = "photo"
            elif msg.document:
                media_type = "document"
                
            sender = await msg.get_sender()
            sender_name = getattr(sender, 'first_name', '') or "Auteur"

            messages.append({
                "id": msg.id,
                "text": msg.raw_text or "",
                "timestamp": int(msg.date.timestamp() * 1000) if msg.date else 0,
                "media_type": media_type,
                "sender_id": msg.sender_id or 0,
                "sender_name": sender_name
            })
            
        return web.json_response({
            "success": True,
            "channel_id": channel_id,
            "messages": messages
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
        return web.json_response({"success": True, "message": "Déconnecté de Telegram avec succès"})
    except Exception as e:
        return web.json_response({"success": False, "error": str(e)}, status=500)

def init_app():
    app = web.Application()
    app.router.add_get('/', lambda r: web.Response(text="AI Edge Telegram Telethon Bridge Active"))
    
    # Routes standardisées selon spec
    app.router.add_get('/telegram/status', handle_status)
    app.router.add_get('/status', handle_status)
    
    app.router.add_post('/telegram/auth/start', handle_send_code)
    app.router.add_post('/auth/send-code', handle_send_code)
    
    app.router.add_post('/telegram/auth/confirm', handle_sign_in)
    app.router.add_post('/auth/sign-in', handle_sign_in)
    
    app.router.add_get('/telegram/channels', handle_get_channels)
    app.router.add_get('/channels', handle_get_channels)
    
    app.router.add_post('/telegram/channels/{id}/watch', handle_watch_channel)
    app.router.add_get('/telegram/channels/{id}/messages', handle_get_channel_messages)
    
    app.router.add_post('/telegram/disconnect', handle_disconnect)
    app.router.add_post('/disconnect', handle_disconnect)
    
    return app

if __name__ == '__main__':
    print(f"🚀 Telegram Telethon Bridge démarré sur le port {PORT}...")
    web.run_app(init_app(), port=PORT, host='0.0.0.0')
""".trimIndent()
}
