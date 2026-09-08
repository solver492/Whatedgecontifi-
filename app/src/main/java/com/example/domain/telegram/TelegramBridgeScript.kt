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

    const val RESET_SESSION_COMMAND = "killall python 2>/dev/null ; rm -f ~/tg-bridge/telethon_session.session* && cd ~/tg-bridge && python telegram-bridge.py"

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
        raw_phone = str(data.get("phone", "")).strip().replace(" ", "").replace("-", "")
        force_sms = bool(data.get("force_sms", False))
        
        if not api_id or not api_hash or not raw_phone:
            return web.json_response({"success": False, "error": "api_id, api_hash et phone requis"}, status=400)
            
        phone = raw_phone
        if phone.startswith("00"):
            phone = "+" + phone[2:]
        elif not phone.startswith("+") and not phone.startswith("0"):
            phone = "+" + phone
            
        print(f"\n=======================================================", flush=True)
        print(f"📩 [TELEGRAM] Demande de code pour : {phone}", flush=True)
        print(f"🔑 API ID: {api_id} | Force SMS: {force_sms}", flush=True)
        print(f"=======================================================", flush=True)
        
        tg = await get_client(api_id, api_hash)
        
        # 1. Vérifier si le compte est DÉJÀ connecté !
        if await tg.is_user_authorized():
            me = await tg.get_me()
            first_n = me.first_name or "Utilisateur"
            print(f"🎉 Compte DÉJÀ connecté : {first_n} (@{me.username}) ID:{me.id}", flush=True)
            await log_to_android("SUCCESS", f"Compte déjà connecté sur Telegram : {first_n}")
            return web.json_response({
                "success": True,
                "already_authorized": True,
                "phone": phone,
                "user": {
                    "id": me.id,
                    "first_name": first_n,
                    "last_name": me.last_name or "",
                    "username": me.username or "",
                    "phone": me.phone or phone
                },
                "message": f"Session déjà active et connectée ({first_n}) !"
            })

        # 2. Envoi officiel de la demande de code
        result = await tg.send_code_request(phone, force_sms=force_sms)
        phone_code_hash_cache[phone] = result.phone_code_hash
        app_state["phone"] = phone
        app_state["api_id"] = api_id
        app_state["api_hash"] = api_hash
        
        type_obj = getattr(result, 'type', None)
        type_name = type(type_obj).__name__ if type_obj else "SentCodeTypeApp"
        timeout = getattr(result, 'timeout', 60) or 60
        
        # Identification claire de la destination pour l'utilisateur
        if "App" in type_name:
            delivery = "APP"
            user_msg = "Code envoyé DANS votre application TELEGRAM (discussion 'Telegram' avec coche bleue). Ce n'est PAS un SMS !"
        elif "Sms" in type_name:
            delivery = "SMS"
            user_msg = f"Code envoyé par SMS sur votre mobile ({phone})."
        elif "Call" in type_name:
            delivery = "CALL"
            user_msg = f"Telegram va vous dicter le code par appel téléphonique au {phone}."
        elif "Flash" in type_name:
            delivery = "FLASH_CALL"
            user_msg = f"Telegram effectue un appel flash au {phone}."
        elif "Email" in type_name:
            delivery = "EMAIL"
            user_msg = "Code envoyé sur votre adresse email de récupération Telegram."
        else:
            delivery = "APP"
            user_msg = f"Code généré par Telegram ({type_name}). Consultez votre application Telegram."

        print(f"✅ Code généré avec succès par Telegram !", flush=True)
        print(f"👉 DESTINATION : {type_name} ({delivery})", flush=True)
        print(f"👉 MESSAGE : {user_msg}", flush=True)
        print(f"⏳ Délai d'attente : {timeout} secondes\n", flush=True)
        
        await log_to_android("AUTH", f"Code envoyé via {type_name} ({delivery}). {user_msg}")
        
        return web.json_response({
            "success": True,
            "phone": phone,
            "phone_code_hash": result.phone_code_hash,
            "delivery_type": delivery,
            "delivery_raw": type_name,
            "timeout": timeout,
            "message": user_msg
        })
    except Exception as e:
        err_msg = str(e)
        print(f"❌ Erreur send_code_request: {err_msg}", flush=True)
        await log_to_android("ERROR", f"Échec demande code Telegram : {err_msg}")
        return web.json_response({"success": False, "error": err_msg}, status=400)

async def handle_resend_code(request):
    try:
        data = await request.json()
        phone = str(data.get("phone") or app_state.get("phone") or "").strip().replace(" ", "").replace("-", "")
        phone_code_hash = data.get("phone_code_hash") or phone_code_hash_cache.get(phone, "")
        
        if not phone:
            return web.json_response({"success": False, "error": "Numéro de téléphone requis"}, status=400)
            
        if not app_state.get("api_id") or not app_state.get("api_hash"):
            return web.json_response({"success": False, "error": "Identifiants API non initialisés"}, status=400)
            
        tg = await get_client(app_state["api_id"], app_state["api_hash"])
        print(f"\n🔄 [TELEGRAM] Renvoi de code demandé pour {phone}...", flush=True)
        
        try:
            if phone_code_hash:
                result = await tg.resend_code(phone, phone_code_hash)
            else:
                result = await tg.send_code_request(phone, force_sms=True)
        except Exception as e_resend:
            print(f"⚠️ Resend direct a échoué ({e_resend}), essai force_sms=True...", flush=True)
            result = await tg.send_code_request(phone, force_sms=True)
            
        phone_code_hash_cache[phone] = result.phone_code_hash
        type_obj = getattr(result, 'type', None)
        type_name = type(type_obj).__name__ if type_obj else "SentCodeTypeSms"
        timeout = getattr(result, 'timeout', 60) or 60
        
        print(f"✅ Nouveau code renvoyé ! Mode: {type_name} | Timeout: {timeout}s\n", flush=True)
        await log_to_android("AUTH", f"Nouveau code renvoyé ({type_name}).")
        
        return web.json_response({
            "success": True,
            "phone": phone,
            "phone_code_hash": result.phone_code_hash,
            "delivery_type": type_name,
            "timeout": timeout,
            "message": "Nouveau code renvoyé par Telegram (priorité SMS)."
        })
    except Exception as e:
        err_msg = str(e)
        print(f"❌ Erreur resend_code: {err_msg}", flush=True)
        await log_to_android("ERROR", f"Échec renvoi code : {err_msg}")
        return web.json_response({"success": False, "error": err_msg}, status=400)

async def handle_reset_session(request):
    try:
        global client, phone_code_hash_cache, app_state, listener_attached
        if client:
            try:
                await client.disconnect()
            except Exception:
                pass
            client = None
        listener_attached = False
        phone_code_hash_cache.clear()
        app_state = {"api_id": None, "api_hash": None, "phone": None}
        
        deleted = 0
        for f in os.listdir(os.getcwd()):
            if f.startswith(SESSION_NAME):
                try:
                    os.remove(os.path.join(os.getcwd(), f))
                    deleted += 1
                except Exception:
                    pass
                    
        print(f"🧹 [RESET] Session purgée avec succès ({deleted} fichier(s) supprimé(s)).", flush=True)
        await log_to_android("AUTH", "Session réinitialisée. Prêt pour une nouvelle connexion.")
        return web.json_response({"success": True, "message": "Session réinitialisée avec succès."})
    except Exception as e:
        return web.json_response({"success": False, "error": str(e)}, status=500)

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

MEDIA_ROOT = os.path.join(os.getcwd(), "telegram_media")
os.makedirs(MEDIA_ROOT, exist_ok=True)

async def handle_serve_media(request):
    try:
        channel_id = request.match_info.get('channel_id')
        msg_id = request.match_info.get('msg_id')
        filename = request.match_info.get('filename')
        file_path = os.path.join(MEDIA_ROOT, str(channel_id), str(msg_id), filename)
        if os.path.exists(file_path) and os.path.isfile(file_path):
            return web.FileResponse(file_path)
        return web.Response(status=404, text="Fichier média introuvable")
    except Exception as e:
        return web.Response(status=500, text=str(e))

async def handle_get_channel_messages(request):
    try:
        global client
        if not client or not client.is_connected() or not await client.is_user_authorized():
            return web.json_response({"success": False, "error": "Non authentifié sur Telegram"}, status=401)
            
        channel_id = int(request.match_info.get('id', 0))
        raw_msgs = []
        async for msg in client.iter_messages(channel_id, limit=35):
            raw_msgs.append(msg)

        grouped_posts = {}
        ordered_keys = []

        for msg in raw_msgs:
            group_key = f"group_{msg.grouped_id}" if getattr(msg, 'grouped_id', None) else f"single_{msg.id}"
            if group_key not in grouped_posts:
                grouped_posts[group_key] = {
                    "id": msg.id,
                    "text": msg.raw_text or "",
                    "timestamp": int(msg.date.timestamp() * 1000) if msg.date else 0,
                    "media_type": "none",
                    "media_urls": [],
                    "local_media_paths": [],
                    "sender_id": msg.sender_id or 0,
                    "sender_name": "Auteur"
                }
                ordered_keys.append(group_key)
            else:
                if not grouped_posts[group_key]["text"] and msg.raw_text:
                    grouped_posts[group_key]["text"] = msg.raw_text

            sender = await msg.get_sender()
            if sender:
                s_name = getattr(sender, 'first_name', '') or getattr(sender, 'title', '') or ""
                if s_name:
                    grouped_posts[group_key]["sender_name"] = s_name

            if msg.media:
                post = grouped_posts[group_key]
                m_type = "photo"
                if getattr(msg, 'video', None):
                    m_type = "video"
                elif getattr(msg, 'document', None):
                    mime = getattr(msg.document, 'mime_type', '') or ''
                    if 'video' in mime:
                        m_type = "video"
                    elif 'image' in mime:
                        m_type = "photo"
                    else:
                        m_type = "document"

                if post["media_type"] == "none":
                    post["media_type"] = m_type
                elif post["media_type"] == "photo" and m_type == "photo":
                    post["media_type"] = "album"

                target_dir = os.path.join(MEDIA_ROOT, str(channel_id), str(msg.id))
                os.makedirs(target_dir, exist_ok=True)
                existing = [f for f in os.listdir(target_dir) if os.path.isfile(os.path.join(target_dir, f))]
                if not existing:
                    try:
                        dl_path = await asyncio.wait_for(client.download_media(msg, file=target_dir), timeout=12.0)
                        if dl_path and os.path.exists(dl_path):
                            existing.append(os.path.basename(dl_path))
                    except Exception as dl_err:
                        print(f"Erreur téléchargement média msg {msg.id}: {dl_err}")

                for f in existing:
                    m_url = f"http://127.0.0.1:{PORT}/media/{channel_id}/{msg.id}/{f}"
                    if m_url not in post["media_urls"]:
                        post["media_urls"].append(m_url)
                        post["local_media_paths"].append(os.path.join(target_dir, f))

        messages = []
        for k in ordered_keys:
            p = grouped_posts[k]
            p["media_url"] = p["media_urls"][0] if p["media_urls"] else None
            messages.append(p)

        return web.json_response({
            "success": True,
            "channel_id": channel_id,
            "count": len(messages),
            "messages": messages
        })
    except Exception as e:
        return web.json_response({"success": False, "error": str(e)}, status=500)

async def handle_disconnect(request):
    try:
        global client, listener_attached
        if client and client.is_connected():
            try:
                await client.log_out()
            except Exception:
                pass
            try:
                await client.disconnect()
            except Exception:
                pass
            client = None
        listener_attached = False
        
        # Nettoyage des fichiers de session
        for f in os.listdir(os.getcwd()):
            if f.startswith(SESSION_NAME):
                try:
                    os.remove(os.path.join(os.getcwd(), f))
                except Exception:
                    pass
                    
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
    
    app.router.add_post('/telegram/auth/resend', handle_resend_code)
    app.router.add_post('/auth/resend-code', handle_resend_code)
    
    app.router.add_post('/telegram/auth/reset', handle_reset_session)
    app.router.add_post('/auth/reset', handle_reset_session)
    
    app.router.add_post('/telegram/auth/confirm', handle_sign_in)
    app.router.add_post('/auth/sign-in', handle_sign_in)
    
    app.router.add_get('/telegram/channels', handle_get_channels)
    app.router.add_get('/channels', handle_get_channels)
    
    app.router.add_post('/telegram/channels/{id}/watch', handle_watch_channel)
    app.router.add_get('/telegram/channels/{id}/messages', handle_get_channel_messages)
    app.router.add_get('/media/{channel_id}/{msg_id}/{filename}', handle_serve_media)
    
    app.router.add_post('/telegram/disconnect', handle_disconnect)
    app.router.add_post('/disconnect', handle_disconnect)
    
    return app

if __name__ == '__main__':
    print(f"🚀 Telegram Telethon Bridge démarré sur le port {PORT}...")
    web.run_app(init_app(), port=PORT, host='0.0.0.0')
""".trimIndent()
}
