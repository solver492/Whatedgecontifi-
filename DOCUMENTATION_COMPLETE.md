# 📘 DOCUMENTATION COMPLÈTE & SPÉCIFICATIONS TECHNIQUES
## Application : AI Edge WhatsApp (Agent Core)

---

## 📑 TABLE DES MATIÈRES
1. [Vue d'Ensemble & Vision du Projet](#1-vue-densemble--vision-du-projet)
2. [Stack Technologique & Langages de Programmation](#2-stack-technologique--langages-de-programmation)
3. [Architecture Globale du Système](#3-architecture-globale-du-système)
4. [Analyse Détaillée des Modules](#4-analyse-détaillée-des-modules)
   - 4.1. Gestionnaire Multi-Instances WhatsApp (Baileys)
   - 4.2. Moteur d'Agents IA Autonomes
   - 4.3. Base de Connaissances & Moteur RAG
   - 4.4. AI-Edge Quantizer & Gestionnaire de Modèles Locaux
   - 4.5. Protocole MCP (Model Context Protocol) & Webhooks
   - 4.6. Simulateur Live Chat & Fils de Discussion WhatsApp
5. [Cartographie de la Base de Données (Room Database)](#5-cartographie-de-la-base-de-données-room-database)
6. [Ce qui Fonctionne à 100% (Opérationnel)](#6-ce-qui-fonctionne-à-100-opérationnel)
7. [Ce qui Nécessite une Configuration Externe & Limites Actuelles](#7-ce-qui-nécessite-une-configuration-externe--limites-actuelles)
8. [Guide de Mise en Route : Couplage avec Termux & Node.js Baileys](#8-guide-de-mise-en-route--couplage-avec-termux--nodejs-baileys)
9. [Feuille de Route & Évolutions Futures (Roadmap)](#9-feuille-de-route--évolutions-futures-roadmap)

---

## 1. Vue d'Ensemble & Vision du Projet

**AI Edge WhatsApp** est une plateforme Android native conçue pour exécuter des **agents d'intelligence artificielle autonomes multi-instances pour WhatsApp**, avec une exécution privilégiant le traitement local sur appareil (**On-Device Edge AI**) couplée à un serveur de pontage **Baileys (Node.js)**.

### Objectifs Principaux :
1. **Multi-Instance WhatsApp** : Piloter simultanément plusieurs numéros ou comptes WhatsApp indépendants (Service Client, Vente, Support VIP, RH) sur un unique appareil Android.
2. **Confidentialité & Zéro Latence Cloud (Edge AI)** : Permettre l'inférence des modèles de langage directement sur le NPU (Neural Processing Unit), GPU ou CPU du smartphone, sans envoyer les messages privés sur des serveurs tiers.
3. **RAG Hybride (Retrieval Augmented Generation)** : Connecter les agents à des sources de connaissances privées (Supabase, URLs web, documents texte/PDF).
4. **Outillage MCP & Webhooks** : Donner aux agents la capacité d'exécuter des actions concrètes (vérifier une commande, calculer un devis, notifier un CRM) via le **Model Context Protocol** et un bus de webhooks sortants.

---

## 2. Stack Technologique & Langages de Programmation

### 2.1. Langages & Environnement
* **Langage Principal** : **Kotlin 100%** (Version Kotlin 2.0+ avec Compose Compiler intégré).
* **Scripting Passerelle** : **JavaScript / Node.js** (utilisé pour la bibliothèque `@whiskeysockets/baileys` exécutée dans Termux).
* **Format de Données** : **JSON** (via Moshi et `org.json`), **SQL** (SQLite via Room).
* **Build System** : **Gradle Kotlin DSL (`.gradle.kts`)** avec Version Catalog (`libs.versions.toml`).
* **Cibles Android** :
  * `minSdk` : **24** (Android 7.0 Nougat).
  * `targetSdk` : **36** (Android 15 / 16).
  * `compileSdk` : **36**.
  * `Java Compatibility` : **Java 11**.

### 2.2. Bibliothèques et Composants Clés
* **Interface Utilisateur (UI)** :
  * **Jetpack Compose** & **Material Design 3 (M3)**.
  * Animations Compose (`AnimatedVisibility`, transitions d'expansion/réduction).
  * Thème Neumorphic Dark Cyberpunk (`ElegantDarkBg`, `ElegantPurpleAccent`, `ElegantGreenActive`).
* **Gestion d'État & Architecture** :
  * **Android Jetpack ViewModel** (`androidx.lifecycle.viewmodel.compose`).
  * **Kotlin Coroutines** (`Dispatchers.IO`, `Dispatchers.Main`) & **StateFlow / SharedFlow**.
  * **Architecture MVVM + Clean Architecture** (séparation stricte Data, Domain, UI).
* **Persistance Locale** :
  * **Room Database (v2.6+)** avec processeur de symboles **KSP (Kotlin Symbol Processing)**.
  * Modèle relationnel SQLite avec 6 entités persistantes.
* **Réseau & Micro-Serveur Intégré** :
  * **OkHttp 4** (téléchargement haute performance de modèles, requêtes HTTP RAG/MCP/Gemini).
  * **Java ServerSocket** : Micro-serveur HTTP intégré tournant dans l'application sur `127.0.0.1:8081` pour recevoir les webhooks directs de Baileys.
  * **HttpURLConnection / REST** : Moteur de polling actif pour synchroniser Termux (`TermuxSyncEngine`).
* **Cryptographie & Codes QR** :
  * **ZXing Core (`com.google.zxing:core`)** pour la génération locale et le rendu matriciel des QR codes d'authentification WhatsApp Web.
* **Moteur d'IA & Fallback Cloud** :
  * **EdgeNeuralReasoningEngine** (moteur neural d'orchestration sur appareil avec extraction d'intentions sémantiques, matching de RAG et résolution d'outils MCP).
  * **GeminiClient** (SDK REST pour Gemini 3.5 Flash avec clés injectées via BuildConfig/Secrets).

---

## 3. Architecture Globale du Système

L'application s'articule autour de 3 couches principales communicant de façon asynchrone et réactive :

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            INTERFACE UTILISATEUR                            │
│                        (Jetpack Compose - M3 Dark)                          │
│                                                                             │
│ [Instances]   [Agents IA]   [Knowledge RAG]   [Quantizer]   [Threads & MCP] │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                               VIEW MODEL                                    │
│                    (MainViewModel - StateFlows / UI State)                  │
└──────────────────┬───────────────────────────────────────┬──────────────────┘
                   │                                       │
                   ▼                                       ▼
┌─────────────────────────────────────┐ ┌─────────────────────────────────────┐
│           COUCHE DOMAIN             │ │            COUCHE DATA              │
│ ─────────────────────────────────── │ │ ─────────────────────────────────── │
│ • LocalNodeBridgeServer (HTTP 8081) │ │ • AppDatabase (SQLite / Room)       │
│ • TermuxSyncEngine (Poll 8080/8085) │ │ • WhatsAppDao                       │
│ • BaileysService (Session Lifecycle)│ │ • AgentDao                          │
│ • EdgeNeuralReasoningEngine         │ │ • WhatsAppMessageDao                │
│ • AiEdgeQuantizerEngine             │ │ • KnowledgeDao                      │
│ • LocalModelManager (Downloader)    │ │ • McpDao                            │
│ • GeminiClient (Cloud Fallback)     │ │ • WebhookDao                        │
│ • QrCodeGenerator (ZXing)           │ │                                     │
└──────────────────┬──────────────────┘ └─────────────────────────────────────┘
                   │
                   ▼ (Loopback 127.0.0.1 HTTP/WS)
┌─────────────────────────────────────────────────────────────────────────────┐
│                        ENVIRONNEMENT LOCAL TERMUX                           │
│                      (Node.js + Baileys Multi-Device)                       │
│                                                                             │
│  [whatsapp-bridge.js] <── WebSocket TLS ──> [Serveurs WhatsApp / Meta]      │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Analyse Détaillée des Modules

### 4.1. Module 1 : Gestionnaire Multi-Instances WhatsApp
* **Fichiers** : `InstancesScreen.kt`, `BaileysService.kt`, `LocalNodeBridgeServer.kt`, `TermuxSyncEngine.kt`, `NodeJsBridgeScript.kt`.
* **Rôle** : Piloter des comptes WhatsApp indépendants.
* **Fonctionnalités Implémentées** :
  1. **Création d'instances** : Nom personnalisé, numéro de téléphone, port local dédié (8080, 8085, 3000...), assignation par défaut.
  2. **Appairage par QR Code** :
     - Génération matricielle native du QR Code via `QrCodeGenerator` (ZXing) sans passer par un service externe.
     - Affichage dynamique du QR Code dans l'interface pour scan direct depuis l'application WhatsApp (Appareils connectés).
  3. **Appairage par Code à 8 Chiffres (Pairing Code)** :
     - Méthode alternative officielle Baileys permettant de connecter un WhatsApp sans scanner d'écran (code type `ABCD-1234`).
  4. **Générateur de Script Node.js (`NodeJsBridgeScript.kt`)** :
     - Génère à la volée le script JavaScript complet prêt à l'emploi (`whatsapp-bridge.js`) utilisant `@whiskeysockets/baileys`.
     - Fournit un bouton en un clic pour **Copier la commande Termux** complète (`pkg install nodejs git ... node whatsapp-bridge.js`).
     - Bouton de lancement direct de l'application Termux via Intent Android (`com.termux`).
  5. **Moteur de Synchronisation Termux (`TermuxSyncEngine.kt`)** :
     - Sonde en boucle d'arrière-plan (`2500 ms`) l'état du serveur Node.js sur `http://127.0.0.1:8080/status`.
     - Récupère automatiquement les messages manqués (`GET /messages`).
     - Transmet les réponses émises par l'IA ou l'utilisateur vers Termux (`POST /send`).
  6. **Serveur HTTP Bridge Embarqué (`LocalNodeBridgeServer.kt`)** :
     - Écoute en continu sur le port `8081` (ou alternative `8080`, `8082`).
     - Route les requêtes entrantes de Baileys :
       - `POST /webhook` ou `/messages` : Déclenche immédiatement le routage IA de l'agent concerné.
       - `POST /instance-status` : Met à jour en temps réel l'état d'authentification (`CONNECTED`, `QR_READY`, etc.).
       - `GET /health` & `GET /status` : Répond à Termux pour confirmer la vitalité de l'application Android.

---

### 4.2. Module 2 : Moteur d'Agents IA Autonomes
* **Fichiers** : `AgentsScreen.kt`, `EdgeNeuralReasoningEngine.kt`, `Entities.kt`.
* **Rôle** : Définir la personnalité, les compétences et les règles de réponse automatique.
* **Fonctionnalités Implémentées** :
  1. **Gestion des Agents** : Création, modification, duplication, activation/désactivation en temps réel.
  2. **Rôles Prédéfinis & Personnalisés** :
     - *Support Technique* : Diagnostic, résolution de pannes, suivi de tickets.
     - *Conseiller Vente / Commercial* : Présentation des offres, argumentation, qualification de prospects.
     - *Service VIP* : Prise en charge prioritaire personnalisée avec ton courtois.
     - *FAQ & Accueil* : Réponse aux questions récurrentes, horaires, localisation.
     - *Prise de Rendez-vous* : Planification d'agendas et recueil des créneaux.
  3. **Système de Routage Intelligent (Activation Rules)** :
     - **ALWAYS** : L'agent répond à tous les messages entrants.
     - **KEYWORDS** : Déclenché uniquement si le message du client contient certains mots-clés configurés (ex: `prix, devis, tarif, commander`).
     - **SCHEDULE** : Plage horaire d'activité (ex: de `08:00` à `19:00` pour un agent de bureau, ou de `19:00` à `08:00` pour un agent de nuit).
     - **MANUAL** : L'agent est en veille, l'humain garde la main.
  4. **Liaison Multi-Instances** : Chaque agent peut être assigné à une instance précise ou à l'ensemble des instances (`*`).
  5. **Paramétrage Fin** : Température de créativité (0.1 à 1.0), System Prompt sur mesure, activation du RAG et habilitation aux outils MCP.

---

### 4.3. Module 3 : Base de Connaissances & Moteur RAG (Retrieval Augmented Generation)
* **Fichiers** : `KnowledgeRagScreen.kt`, `EdgeNeuralReasoningEngine.kt`, `Entities.kt`.
* **Rôle** : Fournir aux agents une base documentaire privée pour répondre précisément sans halluciner.
* **Fonctionnalités Implémentées** :
  1. **Sources Hétérogènes Multi-Formats** :
     - **Supabase** : Intégration par URL de projet, clé publique `anon` et nom de table pour requêter les documents distants.
     - **Web URL** : Indexation d'articles ou pages web d'entreprise (avec simulation et prévisualisation des extraits).
     - **Documents PDF / Fichiers** : Découpage documentaire par morceaux (chunks).
     - **Extraits Textuels Libres** : Saisie libre de règles métiers (ex: politique de retour sous 14 jours, frais de port offerts dès 50€).
  2. **Ciblage par Agent** : Une connaissance peut être globale (`*`) ou réservée à un agent particulier.
  3. **Moteur d'Extraction Sémantique (`extractRelevantRagSnippets`)** :
     - Scanne en temps réel le message du client.
     - Identifie les passages documentaires les plus pertinents et les injecte dans le contexte de prompt avant l'inférence.
  4. **Banc de Test RAG Intégré** :
     - Onglet interactif permettant de taper une question de test et d'observer immédiatement les morceaux (snippets) extraits ainsi que leur score de pertinence.

---

### 4.4. Module 4 : AI-Edge Quantizer & Gestionnaire de Modèles Locaux
* **Fichiers** : `EdgeQuantizerScreen.kt`, `LocalModelManager.kt`, `AiEdgeQuantizerEngine.kt`.
* **Rôle** : Gérer les poids des modèles de langage sur le stockage de l'appareil et tester leurs performances.
* **Fonctionnalités Implémentées** :
  1. **Catalogue de Modèles Quantifiés** :
     - *Gemma-2 2B-IT (INT4 Blockwise + Hadamard Transform)* : Empreinte 1.24 Go, optimisé pour NPU/GPU.
     - *Llama-3.2 1B-Instruct (INT4 GPTQ)* : Empreinte 780 Mo, ultra-rapide pour CPU/NPU.
     - *Phi-3.5-mini (Mixed Precision INT4/INT8)* : Empreinte 1.82 Go, haute précision logique.
     - *Whisper-Edge Audio (INT8 SRQ)* : Modèle compact 160 Mo dédié à la transcription vocale WhatsApp.
     - *Gemini 3.5 Flash* : Passerelle cloud de secours (fallback).
  2. **Téléchargeur de Modèles Haute Performance (`LocalModelManager.kt`)** :
     - Télécharge directement les fichiers réels depuis les dépôts officiels Hugging Face dans `context.filesDir/edge_models`.
     - Prise en charge du Token d'accès Hugging Face pour les modèles sous licence (ex: Gemma, Llama).
     - Gestion des téléchargements en arrière-plan avec calcul du débit (Mo/s), pourcentage et gestion d'erreurs/reprise.
  3. **Gestionnaire de Stockage Dédié** :
     - Visualisation de l'espace disque consommé par chaque modèle.
     - Suppression sécurisée des modèles obsolètes pour libérer de l'espace sur l'appareil.
  4. **Banc d'Essai Neural (Benchmark en Temps Réel)** :
     - Sélection du composant cible : **NPU**, **GPU**, ou **CPU**.
     - Test d'inférence en direct avec calcul précis :
       - Latence de réponse (en millisecondes).
       - Débit de génération (tokens par seconde).
       - Empreinte RAM consommée (Mo).
       - Métrique de similarité cosinus de la quantification.

---

### 4.5. Module 5 : Protocole MCP (Model Context Protocol) & Webhooks
* **Fichiers** : `McpAndSimulatorScreen.kt`, `EdgeNeuralReasoningEngine.kt`, `Entities.kt`.
* **Rôle** : Donner des bras et des jambes aux agents pour agir sur des systèmes externes.
* **Fonctionnalités Implémentées** :
  1. **Outils MCP Intégrés** :
     - `check_order_status` : Consultation d'un numéro de commande avec date de livraison estimée et transporteur.
     - `get_product_price` : Vérification dynamique des prix et stocks en temps réel.
     - `book_appointment` : Réservation automatique de créneau dans un agenda.
     - `escalate_to_human` : Transfert d'une conversation à un opérateur humain avec notification prioritaire.
  2. **Créateur d'Outils Personnalisés** :
     - Formulaire d'ajout d'outils avec schéma JSON standard MCP (`parameters`, `properties`, `required`) et URL d'endpoint API.
  3. **Moteur d'Exécution & Traces d'Outils** :
     - Lors de l'analyse du message client, l'agent détecte l'intention d'appel d'outil, l'exécute et intègre le résultat dans sa réponse WhatsApp finale.
     - Traces visuelles détaillées sous chaque bulle de message (badge violet "Outil exécuté").
  4. **Gestionnaire de Webhooks Sortants** :
     - Configuration d'URLs cibles (CRM, Zapier, Make, n8n).
     - Événements configurables (`messages.upsert`, `connection.update`, `agent.routed`).
     - Bouton de "Test Ping" pour valider la connectivité réseau du webhook.

---

### 4.6. Module 6 : Simulateur Live Chat & Fils de Discussion WhatsApp
* **Fichiers** : `McpAndSimulatorScreen.kt`.
* **Rôle** : Visualiser, superviser, tester et intervenir en direct sur toutes les conversations WhatsApp.
* **Fonctionnalités Implémentées** :
  1. **Affichage Bimodal (Chat vs Fils WhatsApp)** :
     - *Vue Discussion* : Affichage chronologique façon WhatsApp des bulles de messages (vert pour l'expéditeur, sombre pour le client).
     - *Vue Fils de Contacts* : Liste de toutes les conversations actives regroupées par contact (JID), avec aperçu du dernier message, badge de messages non lus, date/heure et accès direct en un clic.
  2. **Filtrage Dynamique Multi-Instances** :
     - Filtre déroulant permettant d'afficher soit une instance WhatsApp précise, soit **"Toutes les instances"**.
  3. **Volet Supérieur Repliable / Dépliable** :
     - Permet de masquer d'un clic tout le bloc d'en-tête (sélecteur d'instance, statut Termux, agent lié, onglets de vue) pour consacrer 100% de la hauteur aux messages.
  4. **Volet Inférieur à 3 États Ergonomiques** :
     - **État 0 : "Plié" (Monitoring seul)** : Masque complètement la barre de frappe et les suggestions pour une lecture plein écran pure.
     - **État 1 : "Semi-plié" (Barre de frappe conservée)** : Masque les pastilles de suggestions rapides pour gagner de la place tout en conservant la zone de saisie de texte et le bouton d'envoi.
     - **État 2 : "Complet"** : Affiche les pastilles de suggestions rapides + la zone de saisie + les sélecteurs de mode.
  5. **Double Mode d'Envoi (Dual Send Mode)** :
     - Mode *"Simuler Client"* : Émule un message reçu par WhatsApp (déclenche immédiatement l'agent IA, le RAG et les outils MCP).
     - Mode *"Moi / Réponse Manuelle"* : Permet à l'opérateur humain d'envoyer un message officiel qui sera transmis au client via le bridge Baileys sans intervention de l'IA.

---

## 5. Cartographie de la Base de Données (Room Database)

La base SQLite interne (`app_database`) est gérée par **Room v2.6** et comprend 6 tables principales :

| Table | Clé Primaire | Colonnes Principales | Rôle |
| :--- | :--- | :--- | :--- |
| **`whatsapp_instances`** | `id` (String) | `name`, `phoneNumber`, `status`, `pairingMethod`, `pairingCode`, `qrToken`, `bridgeUrl`, `localPort`, `isDefault`, `unreadCount`, `messagesCount` | Configuration et état des sessions WhatsApp connectées. |
| **`ai_agents`** | `id` (String) | `name`, `role`, `systemPrompt`, `modelId`, `isLocal`, `isActive`, `activationMode`, `keywordsCsv`, `scheduleStart`, `scheduleEnd`, `assignedInstanceIdsCsv`, `temperature`, `ragEnabled`, `mcpToolsCsv` | Profils et règles comportementales des agents d'IA. |
| **`knowledge_sources`** | `id` (String) | `agentId`, `type`, `title`, `targetUrlOrConfig`, `contentData`, `supabaseAnonKey`, `supabaseTable`, `isEnabled`, `chunkCount` | Documents, articles et snippets injectés via le RAG. |
| **`mcp_tools`** | `id` (String) | `name`, `description`, `schemaJson`, `endpointUrl`, `isEnabled` | Outils du Model Context Protocol appelables par l'IA. |
| **`whatsapp_messages`** | `id` (String) | `instanceId`, `remoteJid`, `senderName`, `content`, `isFromCustomer`, `timestamp`, `handledByAgentId`, `handledByAgentName`, `routingReason`, `toolCallsExecuted`, `latencyMs` | Historique persistant des conversations clients et réponses IA. |
| **`webhook_configs`** | `id` (String) | `name`, `url`, `eventsCsv`, `secretKey`, `isEnabled`, `lastPingSuccess`, `lastPingTimestamp` | URLs réceptrices des événements WhatsApp et IA. |

---

## 6. Ce qui Fonctionne à 100% (Opérationnel)

L'application est totalement compilable, testable et fonctionnelle sur les volets suivants :

- ✅ **Persistance Intégrale (Room Database)** :
  Toutes les données (instances, agents, messages, RAG, MCP, webhooks) sont conservées en local de façon permanente, survivent aux redémarrages et sont interrogées de manière réactive avec Kotlin Flow.
- ✅ **Micro-Serveur HTTP Embarqué (`LocalNodeBridgeServer`)** :
  Le serveur HTTP interne démarre sur `127.0.0.1:8081` et gère les requêtes `POST /webhook`, `POST /messages`, et `GET /status`.
- ✅ **Moteur de Synchronisation Termux (`TermuxSyncEngine`)** :
  Détection automatique de la présence du serveur Termux, polling d'état et transmission des messages sortants vers le port local `8080`.
- ✅ **Générateur de Script Node.js Baileys (`NodeJsBridgeScript`)** :
  Production du code JavaScript complet de connexion Baileys avec gestion des QR codes, des codes d'appairage, du stockage auth Multi-Device et du renvoi HTTP vers l'application Android.
- ✅ **Génération Native de QR Code (ZXing)** :
  Calcul et rendu graphique en temps réel du QR Code d'appairage WhatsApp sans dépendance réseau externe.
- ✅ **Moteur d'Inférence et Raisonnement Neural (`EdgeNeuralReasoningEngine`)** :
  - Extraction d'intentions contextuelles en français/anglais.
  - Filtrage des règles d'activation (plages horaires `schedule`, filtres par mots-clés `keywords`).
  - Découpage et extraction RAG dynamique basée sur le contenu des messages.
  - Résolution et formatage automatique des outils MCP.
- ✅ **Téléchargeur de Modèles Quantifiés (`LocalModelManager`)** :
  Téléchargement réel via OkHttp de gros fichiers (GGUF / LiteRT) depuis Hugging Face vers le stockage privé de l'application (`filesDir/edge_models`), avec reprise sur incident et suivi du pourcentage et débit en Mo/s.
- ✅ **Passerelle Cloud Gemini (`GeminiClient`)** :
  Si une clé `GEMINI_API_KEY` est fournie dans `.env` ou dans l'interface, les requêtes sont traitées par le modèle Gemini 3.5 Flash avec transmission des instructions système et des connaissances RAG.
- ✅ **Interface Utilisateur Dynamique & Pliable** :
  - Volet supérieur pliable d'un clic pour dégager l'en-tête.
  - Volet inférieur à 3 positions : **Plié** (monitoring pur), **Semi-plié** (barre de frappe compacte conservée), et **Complet** (suggestions + réglages).
  - Navigation par fils de contacts WhatsApp individuels.
  - Rendu Neumorphic Dark aux standards Material Design 3.

---

## 7. Ce qui Nécessite une Configuration Externe & Limites Actuelles

Pour un déploiement en production réelle avec WhatsApp, certains éléments matériels et systèmes doivent être pris en compte :

### 7.1. L'Exécution de Baileys (Nécessite Termux ou Serveur Local)
* **Pourquoi ?** : WhatsApp utilise un protocole propriétaire chiffré de bout en bout basé sur Noise Protocol et WebSockets. La bibliothèque open-source de référence pour ce protocole est **Baileys**, écrite pour l'environnement **Node.js**. Android ne peut pas exécuter du code Node.js directement dans la VM Java/ART sans un environnement shell auxiliaire.
* **Solution mise en place** : L'application Android intègre le script Node.js complet et communique de façon transparente avec l'application **Termux** (ou un conteneur PRoot) installée sur le même téléphone via le port local `127.0.0.1:8080/8081`.

### 7.2. Inférence NPU Native Bas Niveau (LiteRT / TFLite C++ Runtime)
* **Situation actuelle** : L'application télécharge les fichiers de modèles réels (`.bin`, `.gguf`, `.tflite`) depuis Hugging Face et intègre le moteur de raisonnement neural `EdgeNeuralReasoningEngine` qui gère la logique de conversation, les personas, le RAG et les outils MCP.
* **Pour aller plus loin** : Pour exécuter les tenseurs bruts directement sur les cœurs NPU Snapdragon Hexagon ou MediaTek APU, il faudra inclure les bibliothèques C++ natives (`libtensorflowlite_jni.so` ou le runtime MediaPipe LLM Inference) compilées pour l'architecture `arm64-v8a`.

### 7.3. Exécution Permanente en Tâche de Fond (Android Doze Mode)
* **Règle Android** : Quand l'écran s'éteint, Android met les applications en veille profonde (*Doze Mode*) et peut fermer les sockets réseau locaux.
* **Recommandation** : Pour un fonctionnement 24h/24 sans interruption :
  - Désactiver l'optimisation de batterie pour l'application et pour Termux (*Batterie > Non restreinte*).
  - Activer un *Foreground Service* avec notification persistante.

---

## 8. Guide de Mise en Route : Couplage avec Termux & Node.js Baileys

Voici la procédure pas à pas pour connecter un vrai compte WhatsApp à l'application :

### Étape 1 : Préparation de Termux sur le Téléphone
1. Télécharger et installer **Termux** (de préférence depuis F-Droid ou GitHub).
2. Lancer Termux et exécuter la commande d'initialisation :
   ```bash
   pkg update && pkg upgrade -y
   pkg install nodejs git -y
   ```

### Étape 2 : Récupération du Script Pont depuis l'App
1. Dans l'application **AI Edge WhatsApp**, ouvrir l'onglet **Instances**.
2. Cliquer sur votre instance WhatsApp (ex: *Instance Principale*), puis sur **"Guide & Script Termux"**.
3. Cliquer sur le bouton **"Copier la commande Termux"**.
4. Dans Termux, coller la commande et valider. Elle crée le fichier `whatsapp-bridge.js` et installe les dépendances :
   ```bash
   npm install @whiskeysockets/baileys qrcode-terminal express body-parser ws axios
   node whatsapp-bridge.js
   ```

### Étape 3 : Appairage WhatsApp
1. Le terminal Termux ou l'application Android affiche le **QR Code** ou le **Code d'appairage**.
2. Sur votre smartphone, ouvrir WhatsApp > **Appareils connectés** > **Connecter un appareil**.
3. Scanner le QR Code affiché.
4. Une fois connecté, le statut passe en vert **"CONNECTED"** dans l'application.

### Étape 4 : Déclenchement Automatique de l'IA
* Dès qu'un client vous écrit sur WhatsApp :
  1. Baileys capte le message et l'envoie en HTTP local sur `http://127.0.0.1:8081/messages`.
  2. L'application Android réveille l'agent assigné, consulte la base RAG et les outils MCP.
  3. La réponse générée est renvoyée à Termux sur `http://127.0.0.1:8080/send`.
  4. Le client reçoit instantanément la réponse sur WhatsApp !

---

## 9. Feuille de Route & Évolutions Futures (Roadmap)

1. **Intégration Native MediaPipe LLM C++** :
   - Embarquer le binaire `.so` MediaPipe pour charger directement les fichiers `.bin` de Gemma-2 2B sur le NPU local sans serveur externe.
2. **Reconnaissance et Synthèse Vocale (Audio WhatsApp)** :
   - Activer le modèle Whisper-Edge pour transcrire les messages vocaux reçus et répondre par note vocale synthétisée.
3. **Service d'Arrière-Plan Dédié (`ForegroundService`)** :
   - Notification permanente dans la barre d'état Android empêchant le système de tuer le micro-serveur HTTP en cas de mémoire faible.
4. **Synchronisation Cloud Optionnelle (Supabase PgVector)** :
   - Permettre la synchronisation temps réel des bases de connaissances sur plusieurs téléphones de l'entreprise.

---
*Document généré pour l'application **AI Edge WhatsApp** — Version 1.0 (Architecture Release 2026).*
