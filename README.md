# 🏛️ Hermes & Chatty

## 📌 Présentation

**Hermes** est un projet d'apprentissage de programmation réseau et de cryptographie appliquée en Java. Il se décompose en deux versions distinctes :

1. **Chatty (Bêta)** : Un chat sécurisé en ligne de commande, limité à deux utilisateurs (architecture Client/Serveur stricte) utilisant le chiffrement asymétrique RSA.
2. **Hermes (Finale)** : Un client de messagerie instantanée multi-threadé avec routage dynamique et chiffrement hybride (RSA + AES-256). Il intègre également une version avec interface graphique (Swing) et une console d'administration pour le serveur.

---

# 💬 Chatty (Version Bêta)

### Principe & Sockets
Chatty établit une liaison socket directe entre un serveur (`ServerChatty`) et un client (`ClientChatty`). 

L'intégralité du flux réseau transitant sur le socket étant par défaut exposé en clair, l'application sécurise la connexion en instanciant un chiffrement asymétrique **RSA** directement à l'initialisation. Les deux instances s'échangent leurs clés publiques respectives afin de garantir la confidentialité des messages dès l'établissement de la liaison.

*Note : À des fins de validation de concept (PoC), cette version bêta n'intègre pas encore de chiffrement symétrique AES.*

---

# 🚀 Hermes (Version Multi-threadée)

Hermes passe à l'échelle supérieure avec une architecture multi-threadée capable de gérer plusieurs connexions client simultanées grâce à un routage de paquets centralisé.

```
[ Client A ] <--- (Chiffré AES) ---> [ Serveur Hermes ] <--- (Chiffré AES) ---> [ Client B ]
```

## 🔒 Le Chiffrement Hybride (RSA + AES-256)

Pourquoi utiliser deux algorithmes de chiffrement ?
* **RSA** est un chiffrement asymétrique puissant mais **très lent** en raison des calculs arithmétiques complexes qu'il requiert. Il est inadapté pour du transfert de flux de données volumineux en temps réel.
* **AES-256** est un chiffrement symétrique basé sur des opérations de permutation et d'opérateurs logiques XOR. Il est **extrêmement rapide** et léger, mais nécessite que les deux parties partagent la même clé secrète.

### Fonctionnement de l'échange de clés :
1. Le client se connecte, génère sa paire de clés RSA et envoie sa clé publique au serveur.
2. Le serveur reçoit la clé publique et chiffre la clé secrète globale AES avec celle-ci (via la méthode `getAESCiphered()`).
3. Le serveur renvoie cette clé AES chiffrée au client.
4. Le client déchiffre le paquet reçu avec sa propre clé privée RSA (via `setAESCiphered()`) pour extraire la clé AES brute.
5. La discussion sécurisée commence : tous les futurs messages s'échangent via le chiffrement symétrique AES-256.

---

## 🛠️ Architecture du Code

### 1. Structure des données : Classe `Package`
La classe `Package` hérite de `Datagram` et encapsule la logique de chiffrement/déchiffrement. Elle structure les données brutes circulant sur le socket sous forme de matrices de bits (`byte[]`).

* **`cipherStringAES()` / `decipherToStringAES()`** : Utilisées à l'initialisation pour transmettre et valider le nom d'utilisateur du client de manière chiffrée.
* **`cipherMessageAES()`** : Côté émetteur, construit un paquet scellé contenant l'horodatage, l'expéditeur, le destinataire et le corps du message.
* **`decipherMessageAES()`** : Côté récepteur, extrait et structure les données du paquet après déchiffrement.

### 2. Le Serveur : `ServerHermes` & `ClientHandler`
* **`ServerHermes`** : Initialise le port d'écoute du socket principal et gère la boucle d'acceptation des connexions entrantes (`accept()`). Chaque nouvelle connexion engendre un thread indépendant.
* **`ClientHandler`** : Classe exécutable (implémentant `Runnable`) chargée de la gestion individuelle d'un client. Elle s'occupe de la négociation des clés de chiffrement au démarrage, de l'écoute du socket dédié et de la redirection des messages vers le bon destinataire ou en *broadcast*.

### 3. Les Clients : `ClientHermes` & `ClientHermesGX`
* **`ClientHermes`** : Client natif en ligne de commande (CLI). Utilise un thread asynchrone pour lire l'entrée standard clavier et un second thread d'écoute réseau pour rafraîchir l'affichage lors de la réception d'un paquet.
* **`ClientHermesGX`** : Version graphique développée sous Java Swing. Elle dispose de trois panneaux principaux : liste des utilisateurs connectés (mise à jour dynamiquement), historique de la discussion et zone de saisie.

---

## ⚡ Fonctionnalités Avancées

### Console d'administration du Serveur (`CommandListener`)
Le serveur de production n'est pas aveugle. À son exécution, un interpréteur de commandes interactif `Hermes-Server:/` est disponible en console pour l'administrateur :
* `/help` : Affiche l'aide et les commandes disponibles.
* `/broadcast [msg]` : Diffuse un message système prioritaire à l'ensemble des clients.
* `/killOne [user]` : Déconnecte de force un utilisateur spécifique.
* `/killAll` : Déconnecte l'intégralité des clients actifs.
* `/stop` : Arrête proprement le serveur en fermant l'ensemble des sockets ouverts.

### Commandes intégrées côté client
Dans le terminal du client `ClientHermes`, l'utilisateur peut également taper des commandes locales :
* `/listConnected` : Affiche manuellement la liste des personnes actuellement connectées au serveur.
* `/disconnect` : Ferme proprement les sockets et quitte l'application.

---

## 🛡️ Analyse de Sécurité & Améliorations

### 1. Vulnérabilité de l'usurpation d'identité ("Server Spoofing")
* **Problème** : Les paquets de contrôle du serveur (comme `/killOne`) sont interprétés par le client si le champ émetteur vaut `"Server"`. N'importe quel client malveillant peut s'attribuer le nom d'utilisateur "Server" lors de sa connexion et envoyer de fausses commandes d'extinction ou de déconnexion aux autres clients.
* **Solution envisagée** : 
  * Brider la validation du nom d'utilisateur lors de la connexion pour rejeter le pseudonyme "Server".
  * Imposer un filtrage strict côté serveur : si un client tente d'envoyer un paquet dont le champ expéditeur contient "Server", le paquet est détruit et le client est banni immédiatement.

### 2. Persistance de la clé AES unique
* **Problème** : La clé AES globale de chiffrement des messages est partagée par tous les clients et reste identique tant que le serveur tourne. Un attaquant écoutant le réseau n'a besoin de se connecter légitimement qu'une seule fois pour obtenir la clé et pouvoir déchiffrer toutes les conversations futures.
* **Solutions envisagées** :
  * **Renouvellement dynamique** : Générer et diffuser une nouvelle clé AES chiffrée en RSA à tous les clients actifs dès qu'un utilisateur se déconnecte.
  * **Canaux AES Dédiés** : Attribuer une clé AES unique par client. *Limite :* Cela oblige le serveur à déchiffrer puis à rechiffrer chaque message pour le réacheminer, exposant temporairement le texte en clair dans la mémoire RAM du serveur.

---

## ⚙️ Compilation et Exécution

*Configuration recommandée : macOS / Linux. Pour Windows, veuillez adapter les chemins ou utiliser l'environnement WSL.*

### Cloner le projet
```bash
git clone [https://github.com/winston2968/Hermes.git](https://github.com/winston2968/Hermes.git)
cd Hermes
```

### Commandes Makefile disponibles

Toutes les commandes doivent être exécutées à la racine du projet `Hermes/` :

| Commande | Action |
| :--- | :--- |
| `make clean` | Nettoie le dossier `/bin` des anciennes builds |
| `make compile` | Compile l'ensemble des sources Java vers `/bin` |
| `make run-hermes-server` | Lance le serveur principal Hermes |
| `make run-hermes-client` | Lance un client Hermes en ligne de commande (CLI) |
| `make run-hermes-clientGX` | Lance l'interface graphique du client (GUI) |
| `make run-chatty-server` | Lance le serveur expérimental Chatty |
| `make run-chatty-client` | Lance le client expérimental Chatty |
