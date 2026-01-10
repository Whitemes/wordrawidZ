# WordrawidZ

## Membres du projet

- RAMANANTSOA Raphaël
- RAMANANJATOVO Johnny

---

## Présentation du projet

WordrawidZ est un jeu éducatif Android **multijoueur local** alliant plateau de jeu, devinettes et mini-jeux d'adresse (labyrinthe, secouage), développé avec Kotlin et Jetpack Compose. Jusqu'à **4 joueurs** s'affrontent en tour par tour pour deviner un mot ou une image mystère à travers une progression sur le plateau, la collecte d'indices et la réussite de mini-jeux utilisant les capteurs de l'appareil.

---

## Objectif pédagogique et gameplay

Le projet vise à explorer la conception d'une application ludique et interactive tout en mettant en pratique :
- La gestion avancée d'état avec **architecture Repository pattern**
- La navigation entre écrans avec **Jetpack Navigation Compose**
- L'utilisation des **capteurs matériels** Android (accéléromètre, secouage)
- La **persistance robuste** résistant aux changements de configuration

**Gameplay :** Les joueurs lancent un dé à tour de rôle, se déplacent sur un plateau 5x5, découvrent des indices (mots sémantiques ou portions d'image), réussissent des mini-jeux pour révéler les indices et gagner des points, et tentent de deviner le mot mystère. Le premier à deviner remporte la partie !

---

## Architecture technique moderne

### **Stack Technique**
- **Kotlin + Jetpack Compose** pour l'interface utilisateur déclarative
- **Architecture Repository Pattern** avec stratégie de fallback intelligente
- **ViewModel + SavedStateHandle** pour persistance d'état robuste
- **Coroutines Kotlin** pour gestion asynchrone et animations
- **Kotlinx Serialization** pour parsing JSON backend
- **Sensors API** pour capteurs Android (accéléromètre, secouage)
- **Material 3** pour design system moderne

### **Architecture en Couches**
```
📱 UI Layer (Compose)
├── GameScreen.kt           # Interface principale
├── Mini-games Screens      # ShakeGame, AccelerometerMaze
├── Components              # DiceButton, GameBoard, StatusCard
└── AssetImageLoader.kt     # Chargement images assets/drawable

🧠 ViewModel Layer
├── GameViewModel.kt        # Logique métier centralisée
├── GameViewModelFactory    # Injection Repository
└── SavedStateHandle        # Persistance thread-safe

🗄️ Repository Layer  
├── MysteryRepository.kt    # Gestion données avec fallback
├── AssetLoader.kt         # Chargement JSON + images assets
├── SimpleMysteryLoader.kt # Fallback étendu (8 objets)
└── MysteryObjectData.kt   # Modèles backend compatibles

💾 Data Sources (Fallback Strategy)
├── 1. Backend JSON        # 20+ objets avec word2vec + images assets
├── 2. SimpleMysteryLoader # 8 objets hardcodés étendus
└── 3. Minimal Fallback   # 3 objets de base
```

### **Flux de Données**
```
GameViewModel → MysteryRepository → DataSource (JSON/Assets)
       ↓
GameState.setMysteryObject() → UI Update
       ↓
User Interaction → ViewModel → Repository → Persist
```

---

## Technologies utilisées

- **Kotlin** (langage principal)
- **Jetpack Compose** (UI déclarative)
- **Repository Pattern** (architecture données)
- **ViewModel + SavedStateHandle** (gestion d'état)
- **Kotlinx Serialization** (parsing JSON backend)
- **Navigation Compose** (navigation écrans)
- **Sensors API** (accéléromètre, secouage)
- **Coroutines** (async/await, timers)
- **Material 3** (design system)
- **AssetManager** (chargement dynamique images)

---

## Exemple de scénario d'utilisation

1. **Démarrage** : L'app charge automatiquement 20+ objets mystères via Repository JSON
2. **Sélection joueurs** : 2 à 4 joueurs avec pions colorés (Rouge, Bleu, Vert, Orange)
3. **Tour de jeu** : Le joueur actuel lance le dé virtuel et son pion se déplace avec animation
4. **Mini-jeu déclenché** : Sur case non révélée, un défi apparaît :
   - **ShakeGame** : Secouer pour remplir une jauge avec combo/fever/malus
   - **AccelerometerMaze** : Guider une bille en inclinant l'appareil
5. **Révélation** : Succès → indice révélé + 10 points pour le joueur
6. **Tour suivant** : Passage automatique au joueur suivant
7. **Devinette** : À tout moment, n'importe quel joueur peut proposer le mot mystère
8. **Victoire** : Mot trouvé → écran de félicitations avec confettis et score final

---

## Fonctionnalités implémentées ✅

### **Core Gameplay**
- ✅ Plateau 5x5 dynamique avec pions animés
- ✅ Système de dé avec animations fluides
- ✅ Révélation conditionnelle d'indices (mini-jeu requis)
- ✅ Portions d'image découpées pixel-perfect avec Canvas
- ✅ Devinette de mot mystère avec validation

### **Multijoueur Local (Hotseat)**
- ✅ **Mode 2-4 joueurs** sur le même appareil
- ✅ **Pions colorés différenciés** (Rouge, Bleu, Vert, Orange)
- ✅ **Système de tours automatique** après chaque mini-jeu
- ✅ **Système de score** : +10 points par mini-jeu réussi
- ✅ **Indicateur visuel** du joueur actuel avec couleur
- ✅ **Affichage multiple** quand plusieurs joueurs sur même case

### **Mini-jeux**
- ✅ **ShakeGame complet** : Système de combo, fever, malus avancés
- ✅ **AccelerometerMaze** : Labyrinthe généré avec backtracking

### **Architecture & Persistance**
- ✅ **Repository Pattern** avec 3 niveaux de fallback
- ✅ **GameViewModel** avec persistance SavedStateHandle
- ✅ **Position pion sauvegardée** (plus de téléportation après mini-jeux)
- ✅ **État plateau persistant** lors navigation/rotation écran
- ✅ **20+ objets mystères** via Backend JSON + images assets
- ✅ **Chargement dynamique images** depuis assets/images/

### **Interface Utilisateur**
- ✅ Design Material 3 moderne et responsive
- ✅ Animations pion et effets visuels
- ✅ Navigation fluide entre écrans
- ✅ Cartes colorées par joueur
- ✅ Debug tools intégrés (stats, validation état)
- ✅ Support images haute résolution

---

## Fonctionnalités à implémenter 🔲

### **Priorité Haute (Prochaines itérations)**
- 🔲 **Écran de configuration** : saisie noms des joueurs avant la partie
- 🔲 **3ème mini-jeu** utilisant caméra/GPS/micro
- 🔲 **Écran paramètres** (son, difficulté, nombre de joueurs)
- 🔲 **Historique des scores** et statistiques avancées

### **Priorité Moyenne (Évolutions futures)**
- 🔲 **Mode multijoueur en ligne** avec backend serveur
- 🔲 **Base étendue d'objets** (50+ items)
- 🔲 **Mini-jeux additionnels** (puzzle, mémoire)
- 🔲 **Accessibilité** (lecteurs d'écran)

---

## Project Structure (Architecture finale)

```
wordrawidx/
├── app/src/main/java/fr/uge/wordrawidx/
│   ├── MainActivity.kt                    # Point d'entrée avec navigation
│   ├── data/                             # 🎯 Couche Repository
│   │   ├── model/
│   │   │   └── MysteryObjectData.kt      # Modèles backend JSON
│   │   ├── local/
│   │   │   ├── AssetLoader.kt            # Chargement assets + images
│   │   │   └── SimpleMysteryLoader.kt    # Fallback étendu
│   │   └── repository/
│   │       └── MysteryRepository.kt      # Gestion données centralisée
│   ├── viewmodel/                        # 🧠 Logique métier
│   │   └── GameViewModel.kt              # État + persistance
│   ├── model/                            # 🎮 Modèles UI
│   │   ├── GameState.kt                  # État plateau moderne
│   │   ├── MazeState.kt                  # État labyrinthe
│   │   └── MazeCell.kt                   # Cellules labyrinthe
│   ├── controller/                       # 🎮 Contrôleurs spécialisés
│   │   ├── NavigationController.kt       # Navigation écrans
│   │   ├── AccelerometerMazeController.kt # Labyrinthe
│   │   └── ShakeGameController.kt        # Jeu secouage
│   ├── view/                            # 🖼️ Interface utilisateur
│   │   ├── screens/
│   │   │   ├── GameScreen.kt            # Écran principal
│   │   │   ├── ShakeGameScreen.kt       # Mini-jeu shake
│   │   │   ├── AccelerometerMazeScreen.kt # Mini-jeu labyrinthe
│   │   │   ├── HomeScreen.kt            # Écran accueil
│   │   │   └── VictoryScreen.kt         # Écran victoire
│   │   └── components/
│   │       ├── GameBoard.kt             # Plateau de jeu
│   │       ├── DiceButton.kt            # Bouton dé animé
│   │       ├── GameStatusCard.kt        # Statut partie
│   │       └── AssetImageLoader.kt      # Chargement images assets
│   ├── navigation/
│   │   └── Screen.kt                    # Énumération écrans
│   ├── utils/
│   │   ├── MiniGameResultHolder.kt      # Communication mini-jeux
│   │   └── MazeGenerator.kt            # Génération labyrinthes
│   └── ui/theme/                       # Thème Material 3
│       ├── Color.kt, Shape.kt, Theme.kt, Type.kt
└── app/src/main/assets/                # 📁 Assets backend
    ├── offline_word_pack.json          # Données word2vec (20+ objets)
    └── images/                         # Images objets mystères
        ├── Peretinoin.png
        ├── Karukan 01.jpg
        └── ... (autres images backend)
```

---

## Modes de Jeu

### **🎮 Mode Multijoueur Local (Implémenté)**
- **Hotseat 2-4 joueurs** - Tour par tour sur le même appareil
- **Pions colorés** - Identification visuelle immédiate (Rouge, Bleu, Vert, Orange)
- **Système de score** - +10 points par mini-jeu réussi
- **Données locales** - Repository avec backend JSON + fallback intelligent
- **Aucun internet requis** - Fonctionne partout
- **20+ objets mystères** - Via Backend JSON avec images
- **Performance optimale** - Cache intelligent et chargement asynchrone

### **🌐 Mode En Ligne (Futur)**
- **Multijoueur distant** - 2-4 joueurs simultanés en ligne
- **Backend serveur** - Synchronisation temps réel
- **Internet requis** - Communication WebSocket/HTTP
- **Base étendue** - 50+ objets avec IA générative

---

## Instructions d'installation et de lancement

### **Prérequis**
- Android Studio Hedgehog ou ultérieur
- JDK 11+
- Appareil/émulateur Android **API 26+** (Android 8.0 Oreo ou supérieur)

### **Installation**
1. Cloner le dépôt :
   ```bash
   git clone https://github.com/Whitemes/wordrawidZ.git
   ```
2. Se placer sur la branche principale :
   ```bash
   git checkout viewModel
   ```
3. Ouvrir dans Android Studio
4. **Ajouter les dépendances** dans `app/build.gradle` :
   ```groovy
   plugins {
       alias(libs.plugins.android.application)
       alias(libs.plugins.jetbrains.kotlin.android)
       id 'kotlinx-serialization'  // ← REQUIS
   }
   
   dependencies {
       implementation "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0"
       implementation "androidx.lifecycle:lifecycle-viewmodel-savedstate:2.7.0"
       // ... autres dépendances existantes
   }
   ```
5. Sync Gradle files
6. Lancer sur appareil/émulateur

### **Configuration Backend Complète**
Les assets backend sont inclus dans le projet :
- ✅ `offline_word_pack.json` → 20+ objets mystères
- ✅ `assets/images/` → Images haute résolution
- ✅ L'app détecte et utilise automatiquement ces données

---

## Limitations techniques connues

- **API minimale** : Android API 26+ (Android 8.0 Oreo) requis
- **Capteurs** : Accéléromètre requis pour labyrinthe (quasi-universel sur smartphones)
- **Performance** : Chargement JSON initial 3-5s (puis cache)
- **Images** : Certaines images backend manquantes (fallback vers placeholder)
- **Multijoueur en ligne** : Non implémenté (mode local uniquement)

---

## Répartition du travail

### **Architecture & Core (Raphaël)**
- Repository Pattern et stratégie fallback
- GameViewModel avec persistance SavedStateHandle
- Résolution téléportation pion et persistance état
- GameState moderne avec support multijoueur
- Architecture modulaire et extensible
- Intégration backend JSON + images assets
- Système de tours et scores multijoueurs

### **Mini-jeux & Capteurs (Johnny)**
- AccelerometerMaze avec génération labyrinthes
- Découpage UI Board et gestion images
- Affichage multijoueur avec pions colorés
- Gestion capteurs multi-appareils
- Système de collisions et physique
- ShakeGame avec système avancé (combo/fever/malus)
- Compatibilité orientations écran

### **Commun**
- Interface utilisateur Material 3
- Navigation Compose et écrans
- Tests validation et debugging
- Documentation technique
- Optimisations performance

---

## État Actuel & Roadmap

### **📊 État : v1.0.0 - Production Ready**

#### **✅ Complété (100%)**
- ✅ Architecture Repository avec backend JSON complet
- ✅ GameViewModel + persistance robuste
- ✅ **Mode multijoueur local 2-4 joueurs (Hotseat)**
- ✅ **Système de tours et scores automatiques**
- ✅ **Pions colorés différenciés par joueur**
- ✅ 2 mini-jeux complets (ShakeGame + AccelerometerMaze)
- ✅ Interface utilisateur moderne et responsive
- ✅ Chargement dynamique images assets
- ✅ 20+ objets mystères avec 500+ mots sémantiques
- ✅ Compatibilité Android 8.0+ (API 26+)

#### **📋 Prochaines étapes**
1. **Écran de configuration** : saisie noms personnalisés des joueurs
2. **3ème mini-jeu** utilisant microphone ou caméra
3. **Écran paramètres** : volume, difficulté, nombre de joueurs
4. **Backend serveur** multijoueur en ligne
5. **Extensions futures** selon feedback utilisateurs

---

## Testing & Quality Assurance

### **Tests Implémentés**
- ✅ Architecture Repository (fallback strategy)
- ✅ Persistance état GameViewModel
- ✅ Navigation entre écrans
- ✅ Mini-jeux individuels
- ✅ Chargement images assets/drawable

### **Scénarios Critiques Validés**
- ✅ Rotation écran → État préservé (positions, scores, tours)
- ✅ Navigation mini-jeu → Retour position correcte
- ✅ Changement configuration → Plateau identique
- ✅ Passage de tours → Joueur suivant activé automatiquement
- ✅ Score multijoueur → Points attribués correctement
- ✅ Affichage pions → Plusieurs joueurs sur même case
- ✅ Fallback données → Toujours 3+ objets disponibles
- ✅ Backend JSON → 20+ objets chargés avec images

---

## Performances & Optimisations

### **Métriques Actuelles**
- **Temps démarrage** : ~3-5s (chargement backend initial)
- **Cache Repository** : < 100ms accès suivants
- **Mémoire** : ~35MB (images + état de jeu)
- **Animations** : 60fps sur appareils récents
- **Persistance** : < 100ms (SavedStateHandle)

### **Optimisations Implémentées**
- Cache intelligent Repository (singleton)
- Chargement asynchrone images avec coroutines
- Images optimisées et découpage Canvas
- État minimal dans SavedStateHandle
- Fallback strategy automatique

---

## Perspectives d'évolution

### **Court Terme (3 mois)**
- Écran de configuration des joueurs (noms personnalisés)
- Historique des parties et classements
- Mini-jeux additionnels (caméra, microphone, GPS)
- Écran paramètres avancé (son, difficulté)

### **Moyen Terme (6 mois)**
- Backend serveur multijoueur en ligne
- Classements globaux et profils utilisateur
- Sauvegarde cloud progression
- Support tablettes optimisé

### **Long Terme (1 an+)**
- Portage iOS/Web avec Compose Multiplatform
- IA générative pour création objets mystères
- Réalité augmentée pour mini-jeux
- Monétisation freemium avec contenu premium

---

## 📜 License

Ce projet est fourni à des **fins éducatives et pédagogiques**.  
Libre réutilisation, modification et adaptation pour apprentissage ou expérimentation personnelle.

---

## 🔗 Liens Utiles

- **Dépôt Git** : [https://github.com/Whitemes/wordrawidZ](https://github.com/Whitemes/wordrawidZ)
- **Branch principale** : `viewModel`
- **Backend Word2Vec** : Intégré dans `assets/offline_word_pack.json`
- **Issues & Bugs** : GitHub Issues

---

*Dernière mise à jour : Janvier 2026 - Version Production v1.0.0 (Multijoueur)*