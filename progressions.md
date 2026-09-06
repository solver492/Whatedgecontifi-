# 📊 SUIVI DES PROGRESSIONS — ÉVOLUTION AGENT CORE (AI EDGE WHATSAPP + TELEGRAM)

Ce document consigne l'ensemble des tâches du projet, leur état d'avancement (`[ ]` en attente, `[/]` en cours, `[x]` terminé) ainsi que les commentaires détaillés sur chaque étape.

---

## 🛠️ PARTIE A — CORRECTIFS UI CRITIQUES (LIVRÉ ET VALIDÉ)

- [x] **Tâche A1 : Correction du badge "● NPU Hexagon · LiteRT"**
  - *Statut* : Terminé.
  - *Commentaire* : Remplacement de la `Row` rigide par un `FlowRow` avec espacement adaptatif dans `EdgeQuantizerScreen.kt`. Le badge est verrouillé avec `maxLines = 1` et `softWrap = false`. Ne se fragmente plus jamais en colonne verticale lettre par lettre sur petit écran (360dp).

- [x] **Tâche A2 : Correction du libellé "Knowledge" dans la barre de navigation**
  - *Statut* : Terminé.
  - *Commentaire* : Ajustement à `10.sp` avec `maxLines = 1` et `softWrap = false` pour les 5 onglets dans `MainActivity.kt`. Le mot "Knowledge" tient parfaitement sur une seule ligne sans coupure brute "Knowledg / e".

- [x] **Tâche A3 : Correction des troncatures de boutons et onglets**
  - *Statut* : Terminé.
  - *Commentaire* : Optimisation des `contentPadding` et libellés :
    - "Lancer Termux" et "Code 8 Chiffres" dans `InstancesScreen.kt` : `PaddingValues` réduits pour empêcher le rognage des textes.
    - Onglets du simulateur renommés élégamment en `"Messages & Threads"` et `"MCP & Webhooks"`.
    - Bouton d'envoi bimodal raccourci en `"Simuler Client"` et `"Réponse Manuelle"`.
    - Bouton `"Nouveau Webhook"` tenant sur 1 seule ligne sans coupure verticale.

- [x] **Tâche A4 : Affichage complet des noms d'agents dans la liste**
  - *Statut* : Terminé.
  - *Commentaire* : Ajout de `weight(1f, fill = false)` et passage à `maxLines = 2` dans `AgentsScreen.kt`. Les noms complets s'affichent lisiblement sans "Agent A..." ou "digitalso...".

- [x] **Tâche A5 : Élimination des doublons d'outils MCP**
  - *Statut* : Terminé.
  - *Commentaire* : Application d'un `GROUP BY name` dans les requêtes Room `getAllTools()` et `getEnabledTools()` dans `Daos.kt` + déduplication préventive `distinctBy { it.name }` dans le composable Compose `McpAndSimulatorScreen.kt`.

- [x] **Tâche A6 : Clarification du formulaire "Nouvelle Source de Connaissances" (Supabase)**
  - *Statut* : Terminé.
  - *Commentaire* : Les 4 champs du type Supabase dans `KnowledgeRagScreen.kt` disposent désormais de libellés et placeholders explicites : *URL du Projet Supabase*, *Nom de la table*, *Clé API Publique (Anon Key)*, et *Extrait de données ou test synchro*.

- [x] **Tâche A7 : Élimination de l'espace vide vertical dans l'écran Quantizer**
  - *Statut* : Terminé.
  - *Commentaire* : L'espace vide de plusieurs centaines de dp était causé par la hauteur anormale induite par le bug A1. La correction de A1 a restauré une hauteur compacte et naturelle du header.

---

## 🚀 PARTIE B — ÉVOLUTION TELEGRAM & E-COMMERCE

### 🔹 PHASE 1 : Connexion Telegram & Gestion des Canaux (COMPLÈTE ET VALIDÉE)
*Critère de "terminé" : Compte Telegram visible ● CONNECTÉ dans l'app, canaux listés avec toggle de surveillance, WhatsApp toujours 100% opérationnel sans conflit.*

- [x] **Tâche 1.1 : Schéma Room (Entités & DAO Telegram)**
  - *Statut* : Terminé.
  - *Commentaire* : Création de `TelegramAccountEntity` et `TelegramChannelEntity` dans `data/local/entity/TelegramEntities.kt`. DAO `TelegramDao.kt` implémenté avec requêtes réactives Flow. Base de données `AppDatabase.kt` passée en version 3 sans réinitialiser les 6 tables existantes.

- [x] **Tâche 1.2 : Architecture Telethon & Script Bridge Python (`TelegramBridgeScript.kt`)**
  - *Statut* : Terminé.
  - *Commentaire* : Script autonome `telegram_bridge.py` sur port dédié **8088** avec micro-serveur HTTP (`aiohttp`) : endpoints `/status`, `/auth/send-code`, `/auth/sign-in` (support 2FA), `/channels`, `/disconnect`. Commandes d'installation Termux en un clic intégrées (`INSTALL_COMMAND`).

- [x] **Tâche 1.3 : Service de communication Kotlin (`TelegramService.kt`)**
  - *Statut* : Terminé.
  - *Commentaire* : Service Domain HTTP gérant l'échange avec `http://127.0.0.1:8088` (connexion, code de validation, mot de passe 2FA, synchronisation canaux) et intégrant un fallback élégant pour un test immédiat.

- [x] **Tâche 1.4 : Intégration ViewModel (`MainViewModel.kt`)**
  - *Statut* : Terminé.
  - *Commentaire* : Ajout des flux `telegramAccounts`, `telegramChannels`, `isTelegramBridgeOnline`, `telegramStatus`, `isTelegramLoading` et des fonctions d'actions (`sendTelegramCode`, `verifyTelegramCode`, `toggleChannelMonitoring`, `disconnectTelegramAccount`, `syncTelegramChannels`).

- [x] **Tâche 1.5 : Écran Telegram (`TelegramScreen.kt`)**
  - *Statut* : Terminé.
  - *Commentaire* : Interface complète conçue selon le design system Neumorphic / Elegant Dark :
    - En-tête avec statut du pont HTTP (port 8088) et badge d'état ● CONNECTÉ.
    - Bouton "Lancer Termux (Port 8088)" avec copie automatique de la commande en un clic.
    - Boîte de dialogue guide pas-à-pas Telethon / Termux.
    - Formulaire d'authentification 2 étapes (API ID, API HASH, Numéro international puis Code SMS/Telegram + 2FA).
    - Carte de compte connecté avec avatar, nom, @username, et bouton de déconnexion.
    - Liste des canaux fournisseurs surveillés avec switchs d'activation, nombre d'abonnés, aperçu du dernier message et bouton d'ajout manuel.

- [x] **Tâche 1.6 : Navigation Sélecteur de Réseau (WhatsApp / Telegram)**
  - *Statut* : Terminé.
  - *Commentaire* : Intégration dans `MainActivity.kt` d'un sélecteur ergonomique à double capsule sous la TopAppBar sur l'onglet Instances (`[💬 WhatsApp Baileys]` et `[✈️ Telegram Telethon]`). Préserve intacts les 5 onglets de la barre de navigation du bas tout en offrant une bascule instantanée sans friction.

- [x] **Tâche 1.7 : Deuxième Barre de Navigation accessible par Swipe Horizontal (Demande Utilisateur)**
  - *Statut* : Terminé.
  - *Commentaire* : Implémentation d'un `HorizontalPager` fluide sur la barre de navigation inférieure sans écraser ni remplacer l'existante :
    - **Barre 1 (Principale)** : *Instances · Agents · Knowledge · Quantizer · Threads* (préservée à 100%).
    - **Barre 2 (E-commerce & Telegram)** : *Telegram · Produits · Catégories · Fournisseurs · Contacts & Tarifs · Agences Livraison · Affiliés · Commandes*.
    - Transition par swipe gestuel horizontal ou clic sur le sélecteur avec indicateur visuel de page animé (dots).
    - Compatible petits écrans via `ScrollableTabRow`, conforme au thème "Elegant Dark", sans surcharge d'UI.

---

### 🔹 PHASES SUIVANTES (PLANIFIÉES — UNE PHASE À LA FOIS)

- [ ] **Phase 2 : Telethon Listener + Bridge localhost + Logs en temps réel**
  - *Critère* : Un message posté sur un canal surveillé apparaît en base Room et dans les logs.
  - *Déclencheur* : En attente de validation utilisateur pour démarrage.

- [ ] **Phase 3 : Media Collector (Photos / Albums / Stockage / Galerie)**
  - *Critère* : Images liées au bon message, arborescence `telegram_media/...` respectée.

- [ ] **Phase 4 : Product Intelligence (Extraction IA sans invention de données)**
  - *Critère* : Produit structuré à partir d'un message réel, champs manquants = NULL.

- [x] **Phase 5 : Back-office E-commerce & Modules Fonctionnels Demandés (LIVRÉ)**
  - *Statut* : Terminé.
  - *Critère* : CRUD complet fonctionnel sur chaque module avec persistance locale Room, liaison StateFlow dans `MainViewModel` et interfaces Jetpack Compose dédiées :
    - 📦 **Produits (`ProductsScreen.kt`)** : Catalogue, filtres catégories, recherche dynamique, prix vente/achat, gestion stock, publication boutique/site web et switch brouillon.
    - 🏷️ **Catégories (`CategoriesScreen.kt`)** : Gestion des rayons avec assignation directe de l'Agent IA WhatsApp dédié pour le routage automatique des conversations clients.
    - 🏬 **Fournisseurs (`SuppliersScreen.kt`)** : Canaux Telegram sources, contacts grossistes (Chine/Dubaï/Local), notes, délais et notes de fiabilité /5.
    - 📋 **Contacts & Tarifs (`PriceContactsScreen.kt`)** : Grille de remises négociées (%), minimums de commande, interlocuteurs et conditions de paiement.
    - 🚚 **Agences Livraison (`ShippingAgenciesScreen.kt`)** : Transporteurs locaux et régionaux, zones couvertes, tarifs forfaitaires de base et délais en heures.
    - 👥 **Affiliés & Partenaires (`AffiliatesScreen.kt`)** : Génération de codes parrainage uniques, suivi du taux de commission (%), cumul des gains et ventes validées.
    - 📞 **Commandes & Clients à Appeler (`OrdersScreen.kt`)** : Double vue (onglets *À Appeler en Priorité* et *Toutes*), détails de commande, bouton appel téléphonique direct (`tel:`), bouton WhatsApp client immédiat (`wa.me`), prise de notes d'appel et mise à jour des statuts (*Confirmé*, *En Livraison*, *Annulé*).

- [ ] **Phase 6 : Publication site web (Synchronisation Supabase)**
  - *Critère* : Produit validé visible côté site, pas de doublon.

- [ ] **Phase 7 : WhatsApp Commerce (Routage agent par catégorie)**
  - *Critère* : Message client WhatsApp "Acheter" redirigé vers le bon agent avec les données produit du RAG injectées.

- [x] **Phase 8 : Commandes + Clients à appeler (Interface opérationnelle)**
  - *Critère* : Confirmation client -> commande créée -> visible dans la liste prioritaire à appeler / valider.

