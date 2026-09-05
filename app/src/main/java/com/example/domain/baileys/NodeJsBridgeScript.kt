package com.example.domain.baileys

object NodeJsBridgeScript {

    /**
     * Complete production-grade Node.js script for Termux using @whiskeysockets/baileys
     * Functions both as a Webhook sender to the Android App AND a local HTTP API server.
     */
    const val SCRIPT_CONTENT = """// =========================================================================
// AI Edge WhatsApp - Baileys Node.js Bridge for Termux / Local Server
// Multi-Device WhatsApp Pairing Code (8 Chiffres) & Synchronisation
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
const fs = require('fs');
const path = require('path');
const readline = require('readline');

// Configuration
const APP_URLS = [
  'http://127.0.0.1:8081',
  'http://127.0.0.1:8080',
  'http://127.0.0.1:8082',
  'http://localhost:8081',
  'http://localhost:8080'
];
const INSTANCE_ID = process.env.INSTANCE_ID || 'digitalsolverland';
const LOCAL_HTTP_PORT = process.env.PORT ? parseInt(process.env.PORT) : 8080;
const AUTH_DIR = path.join(__dirname, 'auth_info_baileys');
const PHONE_FILE = path.join(__dirname, 'phone.txt');

// Mode: Pairing code (8 digits) by default, QR terminal code only if QR_MODE is explicitly set to 'true'
const USE_PAIRING_CODE = process.env.QR_MODE !== 'true';

let sock = null;
let authStatus = 'DISCONNECTED';
let lastQr = '';
let lastPairingCode = '';
let isPairingRequested = false;
let configuredPhoneNumber = '';
const messageBuffer = [];

// Helper: Read saved phone number
function getSavedPhoneNumber() {
  if (process.argv[2] && process.argv[2].replace(/[^0-9]/g, '').length >= 7) {
    return process.argv[2].replace(/[^0-9]/g, '');
  }
  if (process.env.PHONE_NUMBER && process.env.PHONE_NUMBER.replace(/[^0-9]/g, '').length >= 7) {
    return process.env.PHONE_NUMBER.replace(/[^0-9]/g, '');
  }
  if (fs.existsSync(PHONE_FILE)) {
    try {
      const saved = fs.readFileSync(PHONE_FILE, 'utf8').trim().replace(/[^0-9]/g, '');
      if (saved.length >= 7) return saved;
    } catch (_) {}
  }
  return '';
}

// Helper: Save phone number to file
function savePhoneNumber(num) {
  try {
    const clean = num.replace(/[^0-9]/g, '');
    if (clean) {
      fs.writeFileSync(PHONE_FILE, clean, 'utf8');
      configuredPhoneNumber = clean;
    }
  } catch (_) {}
}

// Helper: Send event or message to Android App
async function sendToApp(endpoint, payload) {
  for (const baseUrl of APP_URLS) {
    try {
      const res = await fetch(`${'$'}{baseUrl}${'$'}{endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
        signal: AbortSignal.timeout(5000)
      });
      if (res.ok) return await res.json();
    } catch (_) {}
  }
  return null;
}

// Helper: Fetch config from Android App
async function fetchConfigFromApp() {
  for (const baseUrl of APP_URLS) {
    try {
      const res = await fetch(`${'$'}{baseUrl}/api/config?instanceId=${'$'}{INSTANCE_ID}`, {
        signal: AbortSignal.timeout(3000)
      });
      if (res.ok) {
        const data = await res.json();
        if (data && data.phoneNumber) return data.phoneNumber;
      }
    } catch (_) {}
  }
  return null;
}

// Trigger Pairing Code from Baileys
async function triggerPairingCode(phoneToUse) {
  if (isPairingRequested || (sock && sock.authState && sock.authState.creds && sock.authState.creds.registered)) {
    return;
  }

  const cleanPhone = (phoneToUse || configuredPhoneNumber || '').replace(/[^0-9]/g, '');
  if (!cleanPhone || cleanPhone.length < 7) {
    console.log('\n⚠️ Numéro WhatsApp non configuré pour le code d\'appairage.');
    console.log('👉 Entrez votre numéro ci-dessous ou relancez avec : node server.js <numéro>');
    console.log('   Exemple : node server.js 33745891230\n');
    promptUserForPhone();
    return;
  }

  isPairingRequested = true;
  savePhoneNumber(cleanPhone);

  console.log(`\n⏳ Demande du code d'appairage à WhatsApp pour le numéro : +${'$'}{cleanPhone}...`);

  try {
    // Wait a brief delay for socket initialization
    await new Promise(resolve => setTimeout(resolve, 2200));
    if (!sock) return;

    const rawCode = await sock.requestPairingCode(cleanPhone);
    const formattedCode = rawCode && rawCode.length === 8
      ? `${'$'}{rawCode.slice(0, 4)}-${'$'}{rawCode.slice(4)}`
      : (rawCode || '');

    lastPairingCode = formattedCode;

    console.log('\n╔══════════════════════════════════════════════════════════════════╗');
    console.log('║                                                                  ║');
    console.log('║         🔑 VOTRE CODE D\'APPAIRAGE WHATSAPP (8 CHIFFRES) :        ║');
    console.log('║                                                                  ║');
    console.log(`║                    👉   ${'$'}{formattedCode.padEnd(10)}   👈                    ║`);
    console.log('║                                                                  ║');
    console.log('╚══════════════════════════════════════════════════════════════════╝');
    console.log('\n📲 COMMENT L\'UTILISER SUR VOTRE SMARTPHONE :');
    console.log(' 1. Ouvrez WhatsApp sur votre téléphone');
    console.log(' 2. Touchez les 3 points ⋮ (ou Réglages) > Appareils connectés');
    console.log(' 3. Touchez "Connecter un appareil"');
    console.log(' 4. En bas de l\'écran du scanner, touchez :');
    console.log('    👉 "Lier avec un numéro de téléphone"');
    console.log(` 5. Saisissez ce code à 8 chiffres : ${'$'}{formattedCode}`);
    console.log('==================================================================\n');

    await sendToApp('/api/event', {
      instanceId: INSTANCE_ID,
      event: 'pairing_code',
      pairingCode: formattedCode
    });
  } catch (err) {
    isPairingRequested = false;
    console.error('❌ Impossible d\'obtenir le code d\'appairage :', err.message);
    console.log('💡 Astuce: Vérifiez que le numéro commence bien par l\'indicatif sans le signe + (ex: 33 pour la France, 225 pour la Côte d\'Ivoire, etc.).');
  }
}

function promptUserForPhone() {
  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
  });
  rl.question('👉 Entrez votre numéro WhatsApp avec indicatif (ex: 33745891230) : ', async (answer) => {
    rl.close();
    const clean = answer.trim().replace(/[^0-9]/g, '');
    if (clean.length >= 7) {
      configuredPhoneNumber = clean;
      savePhoneNumber(clean);
      await triggerPairingCode(clean);
    } else {
      console.log('⚠️ Numéro trop court. Relancez avec : node server.js <numéro>');
    }
  });
}

// Start mini HTTP server in Termux
function startHttpServer(portToTry) {
  const server = http.createServer(async (req, res) => {
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
        phone: configuredPhoneNumber || sock?.user?.id || '',
        pairingCode: lastPairingCode,
        messageCount: messageBuffer.length
      }));
      return;
    }

    // GET /pairing-code
    if (req.method === 'GET' && req.url === '/pairing-code') {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({
        pairingCode: lastPairingCode,
        phone: configuredPhoneNumber
      }));
      return;
    }

    // POST /pairing-code (Trigger pairing code with a new phone number from Android App)
    if (req.method === 'POST' && req.url === '/pairing-code') {
      let body = '';
      req.on('data', chunk => { body += chunk; });
      req.on('end', async () => {
        try {
          const data = JSON.parse(body);
          if (data && data.phoneNumber) {
            isPairingRequested = false;
            configuredPhoneNumber = data.phoneNumber.replace(/[^0-9]/g, '');
            savePhoneNumber(configuredPhoneNumber);
            triggerPairingCode(configuredPhoneNumber);
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: true, phone: configuredPhoneNumber }));
          } else {
            res.writeHead(400, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ error: 'Numéro de téléphone requis' }));
          }
        } catch (e) {
          res.writeHead(500, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: e.message }));
        }
      });
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
  console.log(`🔑 Mode : Code d'Appairage à 8 Chiffres (QR Masqué)`);
  console.log(`📡 Passerelle App : ${'$'}{APP_URLS[0]}`);
  console.log(`======================================================\n`);

  // Load configured phone number
  configuredPhoneNumber = getSavedPhoneNumber();
  if (!configuredPhoneNumber) {
    configuredPhoneNumber = (await fetchConfigFromApp()) || '';
  }
  if (configuredPhoneNumber) {
    savePhoneNumber(configuredPhoneNumber);
    console.log(`📞 Numéro WhatsApp configuré : +${'$'}{configuredPhoneNumber}`);
  }

  const { state, saveCreds } = await useMultiFileAuthState(AUTH_DIR);
  const { version } = await fetchLatestBaileysVersion();

  sock = makeWASocket({
    version,
    auth: state,
    logger: pino({ level: 'silent' }),
    printQRInTerminal: false,
    browser: ['AI Edge WhatsApp', 'Chrome', '124.0.0']
  });

  sock.ev.on('creds.update', saveCreds);

  // If phone number is available, request pairing code
  if (configuredPhoneNumber && !sock.authState.creds.registered) {
    setTimeout(() => {
      triggerPairingCode(configuredPhoneNumber);
    }, 1500);
  } else if (!configuredPhoneNumber && !sock.authState.creds.registered) {
    setTimeout(promptUserForPhone, 1200);
  }

  // Connection Updates
  sock.ev.on('connection.update', async (update) => {
    const { connection, lastDisconnect, qr } = update;

    if (qr) {
      lastQr = qr;
      if (!USE_PAIRING_CODE) {
        console.log('\n📱 NOUVEAU QR CODE WHATSAPP :');
        qrcode.generate(qr, { small: true });
      } else {
        // Pairing code mode is active: DO NOT print the QR code in terminal!
        console.log('ℹ️ Code d\'appairage à 8 chiffres demandé (QR Code terminal masqué)...');
        if (!isPairingRequested) {
          triggerPairingCode(configuredPhoneNumber);
        }
      }
      await sendToApp('/api/event', {
        instanceId: INSTANCE_ID,
        event: 'qr',
        qr: qr
      });
    }

    if (connection === 'close') {
      authStatus = 'DISCONNECTED';
      isPairingRequested = false;
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
      console.log('\n╔════════════════════════════════════════════════════════╗');
      console.log('║                                                        ║');
      console.log('║    ✅ [SUCCÈS] WHATSAPP CONNECTÉ ET AUTHENTIFIÉ !      ║');
      console.log('║                                                        ║');
      console.log('╚════════════════════════════════════════════════════════╝');
      console.log('🤖 Les messages reçus seront automatiquement traités par vos agents IA.\n');
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
     * Termux command to reset auth and request 8-digit pairing code
     */
    const val PAIRING_CODE_COMMAND = "cd ~/wa-bridge && curl -s http://127.0.0.1:8081/server.js > server.js 2>/dev/null || curl -s http://127.0.0.1:8080/server.js > server.js ; rm -rf auth_info_baileys auth_* && node server.js"

    fun buildPairingCommand(phoneNumber: String = ""): String {
        val clean = phoneNumber.replace(Regex("[^0-9]"), "")
        val phoneArg = if (clean.isNotBlank()) " $clean" else ""
        return "cd ~/wa-bridge && curl -s http://127.0.0.1:8081/server.js > server.js 2>/dev/null || curl -s http://127.0.0.1:8080/server.js > server.js ; rm -rf auth_info_baileys auth_* && node server.js$phoneArg"
    }

    /**
     * 1-line Termux complete initialization command
     */
    const val TERMUX_ONE_LINER = "pkg update -y && pkg install -y nodejs curl && mkdir -p ~/wa-bridge && cd ~/wa-bridge && curl -s http://127.0.0.1:8081/server.js > server.js && npm install --no-audit @whiskeysockets/baileys pino qrcode-terminal && node server.js"
}

