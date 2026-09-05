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

const APP_BRIDGE_URL = process.env.APP_BRIDGE_URL || 'http://127.0.0.1:8080';
const INSTANCE_ID = process.env.INSTANCE_ID || 'inst-main';
const PHONE_NUMBER = process.env.PHONE_NUMBER || ''; // Set to enable pairing code mode

async function sendToApp(endpoint, payload) {
  try {
    const res = await fetch(`${'$'}{APP_BRIDGE_URL}${'$'}{endpoint}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      console.error(`[App Bridge] Error status ${'$'}{res.status} on ${'$'}{endpoint}`);
      return null;
    }
    return await res.json();
  } catch (err) {
    console.error(`[App Bridge] Impossible de joindre l'application sur ${'$'}{APP_BRIDGE_URL} (${'$'}{err.message})`);
    return null;
  }
}

async function startBaileys() {
  console.log(`\n======================================================`);
  console.log(`🤖 Démarrage du pont Baileys WhatsApp Multi-Device`);
  console.log(`📡 URL Passerelle App : ${'$'}{APP_BRIDGE_URL}`);
  console.log(`🆔 Instance ID        : ${'$'}{INSTANCE_ID}`);
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

  // Pairing code if phone number is supplied and not already registered
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

  // Connection Updates (QR Code & Status)
  sock.ev.on('connection.update', async (update) => {
    const { connection, lastDisconnect, qr } = update;

    if (qr) {
      console.log('\n📱 NOUVEAU QR CODE REÇU DE WHATSAPP (Scannez dans WhatsApp):');
      qrcode.generate(qr, { small: true });
      await sendToApp('/api/event', {
        instanceId: INSTANCE_ID,
        event: 'qr',
        qr: qr
      });
    }

    if (connection === 'close') {
      const shouldReconnect = lastDisconnect?.error?.output?.statusCode !== DisconnectReason.loggedOut;
      console.log(`❌ Connexion WhatsApp fermée. Reconnexion automatique : ${'$'}{shouldReconnect}`);
      await sendToApp('/api/event', {
        instanceId: INSTANCE_ID,
        event: 'connection.update',
        status: 'DISCONNECTED'
      });
      if (shouldReconnect) {
        setTimeout(startBaileys, 3000);
      }
    } else if (connection === 'open') {
      console.log('✅ WhatsApp Connecté avec succès ! Agents IA prêts à répondre.');
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
      if (msg.key.fromMe) continue; // Ignore messages sent by ourselves

      const remoteJid = msg.key.remoteJid;
      const senderName = msg.pushName || 'Client WhatsApp';
      const text =
        msg.message?.conversation ||
        msg.message?.extendedTextMessage?.text ||
        msg.message?.imageMessage?.caption ||
        '';

      if (!text.trim()) continue;

      console.log(`📩 Reçu de [${'$'}{senderName}] (${'$'}{remoteJid}) : "${'$'}{text}"`);

      // Forward to Android AI App
      const response = await sendToApp('/api/message', {
        instanceId: INSTANCE_ID,
        remoteJid: remoteJid,
        senderName: senderName,
        text: text
      });

      if (response && response.replyText) {
        console.log(`🤖 Réponse IA [${'$'}{response.agentName} - ${'$'}{response.latencyMs}ms] : "${'$'}{response.replyText}"`);
        await sock.sendMessage(remoteJid, { text: response.replyText }, { quoted: msg });
        console.log(`🚀 Message WhatsApp envoyé avec succès !`);
      }
    }
  });
}

startBaileys().catch(console.error);
"""

    /**
     * 1-line Termux copy-paste command that installs packages, sets up bridge.js, and runs it
     */
    const val TERMUX_ONE_LINER = """pkg update -y && pkg install -y nodejs git && mkdir -p ~/wa-bridge && cd ~/wa-bridge && cat << 'EOF' > bridge.js
const { default: makeWASocket, useMultiFileAuthState, DisconnectReason, fetchLatestBaileysVersion } = require('@whiskeysockets/baileys');
const pino = require('pino');
const qrcode = require('qrcode-terminal');

const APP_URL = process.env.APP_BRIDGE_URL || 'http://127.0.0.1:8080';
const INSTANCE_ID = process.env.INSTANCE_ID || 'inst-main';

async function sendToApp(endpoint, payload) {
  try {
    const res = await fetch(`${'$'}{APP_URL}${'$'}{endpoint}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    return await res.json();
  } catch (err) {
    return null;
  }
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
      console.log('✅ Connecté à WhatsApp !');
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
      console.log(`📩 Reçu: ${'$'}{text}`);
      const res = await sendToApp('/api/message', { instanceId: INSTANCE_ID, remoteJid, senderName: msg.pushName || 'Client', text });
      if (res && res.replyText) {
        await sock.sendMessage(remoteJid, { text: res.replyText }, { quoted: msg });
        console.log(`🤖 Réponse envoyée: ${'$'}{res.replyText}`);
      }
    }
  });
}
start().catch(console.error);
EOF
npm install @whiskeysockets/baileys pino qrcode-terminal
node bridge.js
"""
}
