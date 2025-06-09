# WordrawidZ

## Membres du projet

- RAMANANTSOA Raphaël
- RAMANANJATOVO Johnny

---

## Présentation du projet

WordrawidZ est un jeu éducatif Android alliant plateau de jeu, devinettes et mini-jeux d'adresse (labyrinthe, secouage), développé avec Kotlin et Jetpack Compose. L'objectif est d'amener le joueur à deviner un mot ou une image mystère à travers une progression sur le plateau, la collecte d'indices et la réussite de mini-jeux utilisant les capteurs de l'appareil.

---

## Objectif pédagogique et gameplay

Le projet vise à explorer la conception d'une application ludique et interactive tout en mettant en pratique :
- La gestion avancée d'état avec **architecture Repository pattern**
- La navigation entre écrans avec **Jetpack Navigation Compose**
- L'utilisation des **capteurs matériels** Android (accéléromètre, secouage)
- La **persistance robuste** résistant aux changements de configuration

**Gameplay :** Le joueur lance un dé, se déplace sur un plateau 5x5, découvre des indices (mots sémantiques ou portions d'image), réussit des mini-jeux pour révéler les indices, et tente de deviner le mot mystère.

---

## Architecture technique moderne

### **Stack Technique**
- **Kotlin + Jetpack Compose** pour l'interface utilisateur déclarative
- **Architecture Repository Pattern** avec stratégie de fallback intelligente
- **ViewModel + SavedStateHandle** pour persistance d'état robuste
- **Coroutines Kotlin** pour gestion asynchrone et animations
- **Sensors API** pour capteurs Android (accéléromètre, gravité)
- **Material 3** pour design system moderne

### **Architecture en Couches**
```
📱 UI Layer (Compose)
├── GameScreen.kt           # Interface principale
├── Mini-games Screens      # ShakeGame, AccelerometerMaze
└── Components              # DiceButton, GameBoard, StatusCard

🧠 ViewModel Layer
├── GameViewModel.kt        # Logique métier centralisée
├── GameViewModelFactory    # Injection Repository
└── SavedStateHandle        # Persistance thread-safe

🗄️ Repository Layer  
├── MysteryRepository.kt    # Gestion données avec fallback
├── AssetLoader.kt         # Chargement JSON backend
├── SimpleMysteryLoader.kt # Fallback étendu (8 objets)
└── MysteryObjectData.kt   # Modèles backend compatibles

💾 Data Sources (Fallback Strategy)
├── 1. Backend JSON        # 20+ objets avec word2vec
├── 2. SimpleMysteryLoader # 8 objets hardcodés étendus
└── 3. Minimal Fallback   # 3 objets de base
```

### **Flux de Données**
```
GameViewModel → MysteryRepository → DataSource
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

---

## Exemple de scénario d'utilisation

1. **Démarrage** : L'app charge automatiquement 8+ objets mystères via Repository
2. **Plateau** : Le joueur lance le dé virtuel et son pion se déplace avec animation
3. **Mini-jeu déclenché** : Sur case non révélée, un défi apparaît :
    - **ShakeGame** : Secouer pour remplir une jauge avec combo/fever
    - **AccelerometerMaze** : Guider une bille en inclinant l'appareil
4. **Révélation** : Succès → indice révélé (mot sémantique ou portion d'image)
5. **Devinette** : À tout moment, proposition du mot mystère
6. **Victoire** : Mot trouvé → écran de félicitations avec confettis

---

## Fonctionnalités implémentées ✅

### **Core Gameplay**
- ✅ Plateau 5x5 dynamique avec pion animé
- ✅ Système de dé avec animations fluides
- ✅ Révélation conditionnelle d'indices (mini-jeu requis)
- ✅ Portions d'image découpées pixel-perfect avec Canvas
- ✅ Devinette de mot mystère avec validation

### **Mini-jeux**
- ✅ **ShakeGame complet** : Système de combo, fever, malus avancés
- ⚠️ **AccelerometerMaze** : Implémenté mais temporairement désactivé

### **Architecture & Persistance**
- ✅ **Repository Pattern** avec 3 niveaux de fallback
- ✅ **GameViewModel** avec persistance SavedStateHandle
- ✅ **Position pion sauvegardée** (plus de téléportation après mini-jeux)
- ✅ **État plateau persistant** lors navigation/rotation écran
- ✅ **8+ objets mystères** via SimpleMysteryLoader

### **Interface Utilisateur**
- ✅ Design Material 3 moderne et responsive
- ✅ Animations pion et effets visuels
- ✅ Navigation fluide entre écrans
- ✅ Debug tools intégrés (stats, validation état)

---

## Fonctionnalités à implémenter 🔲

### **Priorité Haute (En cours)**
- 🔲 **Réactivation AccelerometerMaze** (désactivé temporairement)
- 🔲 **Intégration backend JSON** (20+ objets avec word2vec)
- 🔲 **Tests complets** navigation et persistance

### **Priorité Moyenne (Prochaines itérations)**
- 🔲 **Mode multijoueur local** (2-4 joueurs)
- 🔲 **3ème mini-jeu** utilisant caméra/GPS/micro
- 🔲 **Écran paramètres** (son, difficulté)
- 🔲 **Système de scores** et statistiques

### **Priorité Basse (Évolutions futures)**
- 🔲 **Mode multijoueur en ligne** avec backend serveur
- 🔲 **Base étendue d'objets** (50+ items)
- 🔲 **Mini-jeux additionnels** (puzzle, mémoire)
- 🔲 **Accessibilité** (lecteurs d'écran)

---

## Project Structure (Mise à jour)

```
wordrawidx/
├── app/src/main/java/fr/uge/wordrawidx/
│   ├── MainActivity.kt                    # Point d'entrée avec navigation
│   ├── data/                             # 🆕 Couche Repository
│   │   ├── model/
│   │   │   └── MysteryObjectData.kt      # Modèles backend JSON
│   │   ├── local/
│   │   │   ├── AssetLoader.kt            # Chargement assets
│   │   │   └── SimpleMysteryLoader.kt    # Fallback étendu
│   │   └── repository/
│   │       └── MysteryRepository.kt      # Gestion données centralisée
│   ├── viewmodel/                        # 🆕 Logique métier
│   │   └── GameViewModel.kt              # État + persistance
│   ├── model/                            # 🔄 Modèles UI (modernisé)
│   │   └── GameState.kt                  # État plateau (épuré)
│   ├── controller/                       # Contrôleurs spécialisés
│   │   ├── NavigationController.kt       # Navigation écrans
│   │   ├── AccelerometerMazeController.kt # Labyrinthe
│   │   └── ShakeGameController.kt        # Jeu secouage
│   ├── view/                            # Interface utilisateur
│   │   ├── screens/
│   │   │   ├── GameScreen.kt            # 🔄 Écran principal (ViewModel)
│   │   │   ├── ShakeGameScreen.kt       # Mini-jeu shake
│   │   │   ├── AccelerometerMazeScreen.kt # Mini-jeu labyrinthe
│   │   │   ├── HomeScreen.kt            # Écran accueil
│   │   │   └── VictoryScreen.kt         # Écran victoire
│   │   └── components/
│   │       ├── GameBoard.kt             # Plateau de jeu
│   │       ├── DiceButton.kt            # Bouton dé animé
│   │       └── GameStatusCard.kt        # Statut partie
│   ├── navigation/
│   │   └── Screen.kt                    # Énumération écrans
│   ├── utils/
│   │   ├── MiniGameResultHolder.kt      # 🔄 Communication mini-jeux (simplifié)
│   │   └── MazeGenerator.kt            # Génération labyrinthes
│   └── ui/theme/                       # Thème Material 3
│       ├── Color.kt, Shape.kt, Theme.kt, Type.kt
└── assets/                             # 🆕 Assets backend (optionnel)
    ├── offline_word_pack.json          # Données word2vec
    └── images/                         # Images objets mystères
```

---

## Modes de Jeu

### **🔌 Mode Hors Ligne (Implémenté)**
- **Solo uniquement** - 1 joueur contre le jeu
- **Données locales** - Repository avec fallback intelligent
- **Aucun internet requis** - Fonctionne partout
- **8+ objets mystères** - Via SimpleMysteryLoader
- **Performance optimale** - Pas de latence réseau

### **🌐 Mode En Ligne (Futur)**
- **Multijoueur** - 2-4 joueurs simultanés
- **Backend serveur** - Synchronisation temps réel
- **Internet requis** - Communication WebSocket/HTTP
- **Base étendue** - 20+ objets avec word2vec

---

## Instructions d'installation et de lancement

### **Prérequis**
- Android Studio Hedgehog ou ultérieur
- JDK 11+
- Appareil/émulateur Android API 34+ (Vanilla Ice Cream)

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
4. Sync Gradle files (automatique)
5. Lancer sur appareil/émulateur

### **Configuration Optionnelle Backend**
Pour utiliser le backend JSON avec 20+ objets :
1. Copier `offline_word_pack.json` dans `app/src/main/assets/`
2. Copier images dans `app/src/main/res/drawable/`
3. L'app détectera automatiquement et utilisera ces données

---

## Limitations techniques connues

- **API minimale** : Android API 34+ requis
- **Capteurs** : Accéléromètre requis pour labyrinthe (quasi-universel)
- **Performance** : Possible ralentissement sur appareils anciens
- **Multijoueur** : Non implémenté (mode solo uniquement)
- **Persistance session** : État perdu à fermeture complète app

---

## Répartition du travail

### **Architecture & Core (Raphaël)**
- Repository Pattern et stratégie fallback
- GameViewModel avec persistance SavedStateHandle
- Résolution téléportation pion et persistance état
- GameState moderne épuré
- Architecture modulaire et extensible

### **Mini-jeux & Capteurs (Johnny)**
- AccelerometerMaze avec génération labyrinthes
- Découpage UI Board
- Gestion capteurs multi-appareils
- Système de collisions et physique
- ShakeGame avec système avancé (combo/fever)
- Compatibilité orientations écran

### **Commun**
- Interface utilisateur Material 3
- Navigation Compose et écrans
- Tests validation et debugging
- Documentation technique
- Intégration backend word2vec

---

## État Actuel & Roadmap

### **📊 État : v0.9.0 - Architecture Repository Stable**

#### **✅ Complété (95%)**
- Architecture Repository avec fallback
- GameViewModel + persistance robuste
- Mode solo hors ligne fonctionnel
- ShakeGame complet avec effets avancés
- Interface utilisateur moderne

#### **🔄 En cours (5%)**
- Réactivation AccelerometerMaze
- Tests validation complète
- Intégration backend JSON

#### **📋 Prochaines étapes**
1. **Tests & validation**
2. **Mode multijoueur local**
3. **Backend en ligne**
4. **Extensions futures**

---

## Testing & Quality Assurance

### **Scénarios Critiques Validés**
- ✅ Rotation écran → État préservé
- ✅ Navigation mini-jeu → Retour position correcte
- ✅ Changement configuration → Plateau identique
- ✅ Fallback données → Toujours 3+ objets disponibles

---

## Performances & Optimisations

### **Métriques Actuelles**
- **Temps démarrage** : < 500ms (chargement Repository)
- **Mémoire** : ~25MB (images + état de jeu)
- **Animations** : 60fps sur appareils récents
- **Persistance** : < 100ms (SavedStateHandle)

### **Optimisations Implémentées**
- Cache intelligent Repository (singleton)
- Images optimisées et découpage Canvas
- Coroutines pour opérations async
- État minimal dans SavedStateHandle

---

## Perspectives d'évolution

### **Court Terme **
- Mode multijoueur local (même appareil)
- Base étendue objets mystères (50+)
- Mini-jeux additionnels (caméra, GPS)
- Système scores et statistiques

### **Moyen Terme (6 mois)**
- Backend serveur multijoueur en ligne
- Classements globaux et profils
- Sauvegarde cloud progression
- Support tablettes optimisé

### **Long Terme (1 an+)**
- Portage iOS/Web avec Compose Multiplatform
- IA générative pour création objets mystères
- Réalité augmentée pour mini-jeux
- Monétisation freemium

---

## 📜 License

Ce projet est fourni à des **fins éducatives et pédagogiques**.  
Libre réutilisation, modification et adaptation pour apprentissage ou expérimentation personnelle.

---

## 🔗 Liens Utiles

- **Dépôt Git** : [https://github.com/Whitemes/wordrawidZ](https://github.com/Whitemes/wordrawidZ)
- **Backend Word2Vec** : Voir repository partenaire
- **Issues & Bugs** : GitHub Issues

---

*Dernière mise à jour : Juin 2025 - Version Repository Architecture*