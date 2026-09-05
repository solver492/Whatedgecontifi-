package com.example.domain.baileys

object NodeJsBridgeScript {

    /**
     * Complete production-grade Node.js script for Termux using @whiskeysockets/baileys
     */
    const val SCRIPT_CONTENT = """// =========================================================================
// AI Edge WhatsApp - Baileys Node.js Bridge for Termux / Local Server
// =========================================================================

const {
  default: makeWASocket,
  useMultiFileAuthState,
  DisconnectReason,
  fetchLatestBaileysVersion
} = require('@whiskeysockets/baileys');
const pino = require('pino');
const qrcode = require('qrcode-terminal');

const APP_URLS = ['http://127.0.0.1:8080', 'http://127.0.0.1:8081', 'http://localhost:8080'];
const INSTANCE_ID = process.env.INSTANCE_ID || 'inst-main';
const PHONE_NUMBER = process.env.PHONE_NUMBER || '';

async function sendToApp(endpoint, payload) {
  for (const baseUrl of APP_URLS) {
    try {
      const res = await fetch(`${'$'}{baseUrl}${'$'}{endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
        signal: AbortSignal.timeout(10000)
      });
      if (res.ok) {
        return await res.json();
      }
    } catch (err) {
      // Try next url
    }
  }
  console.error(`❌ [PONT ANDROID] Impossible de joindre l'application Android sur ${'$'}{APP_URLS[0]}.`);
  console.error(`👉 Vérifiez que l'application Android est lancée et que le "Serveur Local" est démarré (Onglet Instances).`);
  return null;
}

async function startBaileys() {
  console.log(`\n======================================================`);
  console.log(`🤖 Pont Baileys WhatsApp Multi-Device pour AI Edge`);
  console.log(`📡 Communication App : ${'$'}{APP_URLS[0]}`);
  console.log(`🆔 Instance ID       : ${'$'}{INSTANCE_ID}`);
  console.log(`======================================================\n`);

  const { state, saveCreds } = await useMultiFileAuthState(`auth_${'$'}{INSTANCE_ID}`);
  const { version } = await fetchLatestBaileysVersion();

  const sock = makeWASocket({
    version,
    auth: state,
    logger: pino({ level: 'silent' }),
    printQRInTerminal: false,
    browser: ['AI Edge WhatsApp', 'Chrome', '124.0.0']
  });

  sock.ev.on('creds.update', saveCreds);

  // Pairing code if phone number is supplied
  if (PHONE_NUMBER && !sock.authState.creds.registered) {
    setTimeout(async () => {
      try {
        const code = await sock.requestPairingCode(PHONE_NUMBER.replace(/[^0-9]/g, ''));
        console.log(`\n🔑 CODE D'APPAIRAGE WHATSAPP : ${'$'}{code}\n`);
        await sendToApp('/api/event', {
          instanceId: INSTANCE_ID,
          event: 'pairing_code',
          pairingCode: code
        });
      } catch (err) {
        console.error('Erreur demande pairing code:', err);
      }
    }, 3000);
  }

  // Connection Updates
  sock.ev.on('connection.update', async (update) => {
    const { connection, lastDisconnect, qr } = update;

    if (qr) {
      console.log('\n📱 NOUVEAU QR CODE WHATSAPP :');
      qrcode.generate(qr, { small: true });
      await sendToApp('/api/event', {
        instanceId: INSTANCE_ID,
        event: 'qr',
        qr: qr
      });
    }

    if (connection === 'close') {
      const shouldReconnect = lastDisconnect?.error?.output?.statusCode !== DisconnectReason.loggedOut;
      console.log(`❌ Connexion WhatsApp fermée. Reconnexion : ${'$'}{shouldReconnect}`);
      await sendToApp('/api/event', {
        instanceId: INSTANCE_ID,
        event: 'connection.update',
        status: 'DISCONNECTED'
      });
      if (shouldReconnect) {
        setTimeout(startBaileys, 3000);
      }
    } else if (connection === 'open') {
      console.log('\n✅ [SUCCÈS] WhatsApp connecté avec succès ! Prêt à écouter et transférer les messages.');
      await sendToApp('/api/event', {
        instanceId: INSTANCE_ID,
        event: 'connection.update',
        status: 'CONNECTED'
      });
    }
  });

  // Handle incoming messages
  sock.ev.on('messages.upsert', async (m) => {
    if (m.type !== 'notify') return;

    for (const msg of m.messages) {
      if (msg.key.fromMe) continue;

      const remoteJid = msg.key.remoteJid;
      const senderName = msg.pushName || 'Client WhatsApp';
      const text =
        msg.message?.conversation ||
        msg.message?.extendedTextMessage?.text ||
        msg.message?.imageMessage?.caption ||
        '';

      if (!text.trim()) {
        console.log(`ℹ️ [MÉDIA/NON-TEXTUEL] Reçu de ${'$'}{remoteJid}`);
        continue;
      }

      console.log('--------------------------------------------------');
      console.log(`[NOUVEAU MESSAGE REÇU]`);
      console.log(`De      : ${'$'}{senderName} (${'$'}{remoteJid})`);
      console.log(`Message : "${'$'}{text}"`);
      console.log(`📡 [ENVOI APP] Transfert vers l'application Android AI Edge...`);

      // Forward to Android App
      const response = await sendToApp('/api/message', {
        instanceId: INSTANCE_ID,
        remoteJid: remoteJid,
        senderName: senderName,
        text: text
      });

      if (response && response.replyText) {
        console.log(`🤖 [RÉPONSE IA - ${'$'}{response.agentName} | ${'$'}{response.latencyMs}ms] : "${'$'}{response.replyText}"`);
        await sock.sendMessage(remoteJid, { text: response.replyText }, { quoted: msg });
        console.log(`🚀 [WHATSAPP] Réponse envoyée avec succès sur WhatsApp !`);
      } else {
        console.log(`⚠️ Aucune réponse automatique retournée ou serveur app inaccessible.`);
      }
      console.log('--------------------------------------------------');
    }
  });
}

startBaileys().catch(console.error);
"""

    /**
     * 1-line Termux copy-paste command that creates bridge.js AND server.js, installs dependencies, and runs
     */
    const val TERMUX_ONE_LINER = """pkg update -y && pkg install -y nodejs git curl && mkdir -p ~/wa-bridge && cd ~/wa-bridge && cat << 'EOF' > bridge.js
const { default: makeWASocket, useMultiFileAuthState, DisconnectReason, fetchLatestBaileysVersion } = require('@whiskeysockets/baileys');
const pino = require('pino');
const qrcode = require('qrcode-terminal');

const APP_URLS = ['http://127.0.0.1:8080', 'http://127.0.0.1:8081', 'http://localhost:8080'];
const INSTANCE_ID = process.env.INSTANCE_ID || 'inst-main';

async function sendToApp(endpoint, payload) {
  for (const u of APP_URLS) {
    try {
      const res = await fetch(u + endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (res.ok) return await res.json();
    } catch (e) {}
  }
  return null;
}

async function start() {
  console.log('🤖 Pont Baileys WhatsApp démarré...');
  const { state, saveCreds } = await useMultiFileAuthState('auth_session');
  const { version } = await fetchLatestBaileysVersion();
  const sock = makeWASocket({ version, auth: state, logger: pino({ level: 'silent' }), printQRInTerminal: false });
  sock.ev.on('creds.update', saveCreds);

  sock.ev.on('connection.update', async ({ connection, lastDisconnect, qr }) => {
    if (qr) {
      console.log('📱 QR Code WhatsApp reçu :');
      qrcode.generate(qr, { small: true });
      await sendToApp('/api/event', { instanceId: INSTANCE_ID, event: 'qr', qr });
    }
    if (connection === 'close') {
      const reconnect = lastDisconnect?.error?.output?.statusCode !== DisconnectReason.loggedOut;
      await sendToApp('/api/event', { instanceId: INSTANCE_ID, event: 'connection.update', status: 'DISCONNECTED' });
      if (reconnect) setTimeout(start, 3000);
    } else if (connection === 'open') {
      console.log('✅ Connecté à WhatsApp ! Prêt à transférer les messages.');
      await sendToApp('/api/event', { instanceId: INSTANCE_ID, event: 'connection.update', status: 'CONNECTED' });
    }
  });

  sock.ev.on('messages.upsert', async (m) => {
    if (m.type !== 'notify') return;
    for (const msg of m.messages) {
      if (msg.key.fromMe) continue;
      const remoteJid = msg.key.remoteJid;
      const text = msg.message?.conversation || msg.message?.extendedTextMessage?.text || '';
      if (!text) continue;
      console.log('---------------------------');
      console.log(`[NOUVEAU MESSAGE REÇU]\nDe : ${'$'}{msg.pushName || 'Client'} (${'$'}{remoteJid})\nMessage : ${'$'}{text}`);
      console.log('📡 Transfert vers l\'application Android...');
      const res = await sendToApp('/api/message', { instanceId: INSTANCE_ID, remoteJid, senderName: msg.pushName || 'Client', text });
      if (res && res.replyText) {
        await sock.sendMessage(remoteJid, { text: res.replyText }, { quoted: msg });
        console.log(`🤖 [RÉPONSE IA]: ${'$'}{res.replyText}`);
        console.log('🚀 Envoyé sur WhatsApp !');
      }
      console.log('---------------------------');
    }
  });
}
start().catch(console.error);
EOF
cp bridge.js server.js
npm install --no-audit --no-fund @whiskeysockets/baileys pino qrcode-terminal
node server.js
"""

    /**
     * Fast 1-line update command for existing Termux installation
     */
    const val FAST_UPDATE_COMMAND = "cd ~/wa-bridge && curl -s http://127.0.0.1:8080/bridge.js > bridge.js && cp bridge.js server.js && node server.js"
}

