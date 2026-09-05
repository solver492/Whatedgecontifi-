package com.example.domain.baileys

object NodeJsBridgeScript {

    /**
     * Complete production-grade Node.js script for Termux using @whiskeysockets/baileys
     * Functions both as a Webhook sender to the Android App AND a local HTTP API server.
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
const http = require('http');

// Configuration
const APP_URLS = [
  'http://127.0.0.1:8081',
  'http://127.0.0.1:8080',
  'http://127.0.0.1:8082',
  'http://localhost:8081',
  'http://localhost:8080'
];
const INSTANCE_ID = process.env.INSTANCE_ID || 'digitalsolverland';
const PHONE_NUMBER = process.env.PHONE_NUMBER || '';
const LOCAL_HTTP_PORT = process.env.PORT ? parseInt(process.env.PORT) : 8080;

let sock = null;
let authStatus = 'DISCONNECTED';
let lastQr = '';
let lastPairingCode = '';
const messageBuffer = [];

// Helper: Send event or message to Android App
async function sendToApp(endpoint, payload) {
  for (const baseUrl of APP_URLS) {
    try {
      const res = await fetch(`${'$'}{baseUrl}${'$'}{endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
        signal: AbortSignal.timeout(6000)
      });
      if (res.ok) {
        return await res.json();
      }
    } catch (err) {
      // Try next url
    }
  }
  return null;
}

// Start mini HTTP server in Termux to allow Android App to poll or trigger actions
function startHttpServer(portToTry) {
  const server = http.createServer(async (req, res) => {
    // CORS headers
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

    if (req.method === 'OPTIONS') {
      res.writeHead(204);
      res.end();
      return;
    }

    // GET /status
    if (req.method === 'GET' && req.url === '/status') {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({
        status: authStatus === 'CONNECTED' ? 'online' : 'waiting_pairing',
        authStatus: authStatus,
        instanceId: INSTANCE_ID,
        phone: sock?.user?.id || '',
        messageCount: messageBuffer.length
      }));
      return;
    }

    // GET /messages
    if (req.method === 'GET' && req.url === '/messages') {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify(messageBuffer.slice(-50)));
      return;
    }

    // POST /send (Outgoing WhatsApp message from Android App)
    if (req.method === 'POST' && req.url === '/send') {
      let body = '';
      req.on('data', chunk => { body += chunk; });
      req.on('end', async () => {
        try {
          const data = JSON.parse(body);
          if (sock && data.remoteJid && data.text) {
            await sock.sendMessage(data.remoteJid, { text: data.text });
            console.log(`📤 [ENVOI WHATSAPP] Vers : ${'$'}{data.remoteJid} | Texte : "${'$'}{data.text}"`);
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: true }));
          } else {
            res.writeHead(400, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ error: 'Socket non prêt ou paramètres manquants' }));
          }
        } catch (e) {
          res.writeHead(500, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: e.message }));
        }
      });
      return;
    }

    res.writeHead(404, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ error: 'Route introuvable' }));
  });

  server.on('error', (err) => {
    if (err.code === 'EADDRINUSE') {
      console.log(`ℹ️ Port ${'$'}{portToTry} déjà utilisé, tentative sur le port ${'$'}{portToTry + 5}...`);
      startHttpServer(portToTry + 5);
    } else {
      console.error('Erreur serveur HTTP local :', err.message);
    }
  });

  server.listen(portToTry, '0.0.0.0', () => {
    console.log(`🌐 Serveur API Termux prêt sur http://0.0.0.0:${'$'}{portToTry}`);
  });
}

async function startBaileys() {
  console.log(`\n======================================================`);
  console.log(`🤖 Pont Baileys WhatsApp Multi-Device pour AI Edge`);
  console.log(`🆔 Instance : ${'$'}{INSTANCE_ID}`);
  console.log(`📡 Passerelle App : ${'$'}{APP_URLS[0]}`);
  console.log(`======================================================\n`);

  const { state, saveCreds } = await useMultiFileAuthState(`auth_${'$'}{INSTANCE_ID}`);
  const { version } = await fetchLatestBaileysVersion();

  sock = makeWASocket({
    version,
    auth: state,
    logger: pino({ level: 'silent' }),
    printQRInTerminal: false,
    browser: ['AI Edge WhatsApp', 'Chrome', '124.0.0']
  });

  sock.ev.on('creds.update', saveCreds);

  // Pairing code request if phone number is provided
  if (PHONE_NUMBER && !sock.authState.creds.registered) {
    setTimeout(async () => {
      try {
        const code = await sock.requestPairingCode(PHONE_NUMBER.replace(/[^0-9]/g, ''));
        lastPairingCode = code;
        console.log(`\n🔑 CODE D'APPAIRAGE WHATSAPP : ${'$'}{code}\n`);
        await sendToApp('/api/event', {
          instanceId: INSTANCE_ID,
          event: 'pairing_code',
          pairingCode: code
        });
      } catch (err) {
        console.error('Erreur demande pairing code:', err.message);
      }
    }, 3000);
  }

  // Connection Updates
  sock.ev.on('connection.update', async (update) => {
    const { connection, lastDisconnect, qr } = update;

    if (qr) {
      lastQr = qr;
      console.log('\n📱 NOUVEAU QR CODE WHATSAPP :');
      qrcode.generate(qr, { small: true });
      await sendToApp('/api/event', {
        instanceId: INSTANCE_ID,
        event: 'qr',
        qr: qr
      });
    }

    if (connection === 'close') {
      authStatus = 'DISCONNECTED';
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
      authStatus = 'CONNECTED';
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

      if (!text.trim()) continue;

      console.log('--------------------------------------------------');
      console.log('[NOUVEAU MESSAGE REÇU]');
      console.log(`De      : ${'$'}{senderName} (${'$'}{remoteJid})`);
      console.log(`Message : "${'$'}{text}"`);

      // Store in buffer for pulling
      messageBuffer.push({
        remoteJid,
        senderName,
        text,
        timestamp: Date.now()
      });
      if (messageBuffer.length > 100) messageBuffer.shift();

      console.log(`📡 [ENVOI APP] Transfert vers l'application Android AI Edge...`);

      // Forward to Android App via Webhook
      const response = await sendToApp('/api/message', {
        instanceId: INSTANCE_ID,
        remoteJid: remoteJid,
        senderName: senderName,
        text: text
      });

      if (response && response.replyText) {
        console.log(`🤖 [RÉPONSE IA - ${'$'}{response.agentName || 'Agent'} | ${'$'}{response.latencyMs || 0}ms] : "${'$'}{response.replyText}"`);
        await sock.sendMessage(remoteJid, { text: response.replyText }, { quoted: msg });
        console.log(`🚀 [WHATSAPP] Réponse envoyée avec succès sur WhatsApp !`);
      } else {
        console.log(`ℹ️ Message enregistré. En attente de l'application.`);
      }
      console.log('--------------------------------------------------');
    }
  });
}

// Start HTTP server and Baileys
startHttpServer(LOCAL_HTTP_PORT);
startBaileys().catch(console.error);
"""

    /**
     * 1-line Termux fast update command that replaces server.js with the latest version and starts it
     */
    const val FAST_UPDATE_COMMAND = "cd ~/wa-bridge && curl -s http://127.0.0.1:8081/server.js > server.js 2>/dev/null || curl -s http://127.0.0.1:8080/server.js > server.js ; node server.js"

    /**
     * 1-line Termux complete initialization command
     */
    const val TERMUX_ONE_LINER = "pkg update -y && pkg install -y nodejs curl && mkdir -p ~/wa-bridge && cd ~/wa-bridge && curl -s http://127.0.0.1:8081/server.js > server.js && npm install --no-audit @whiskeysockets/baileys pino qrcode-terminal && node server.js"
}

