# 🎯 IP Hunter

![Version](https://img.shields.io/badge/version-1.2.0-blue.svg)
![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=java&logoColor=white)
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=flat&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=flat&logo=css3&logoColor=white)

**IP Hunter** è un gioco educativo interattivo progettato per aiutare gli studenti e gli appassionati di networking a padroneggiare il **subnetting IP**. Sfida te stesso e i tuoi amici a calcolare classi, indirizzi di rete e broadcast nel minor tempo possibile!

---

## ✨ Caratteristiche Principali

-   **📖 Manuale Integrato**: Una guida passo-passo accessibile direttamente in-game che spiega le classi IP, la "Formula Magica" del salto e include esempi pratici (dai livelli Medi a quelli Elite).
-   **⚡ Quiz Dinamici**: Generazione automatica di indirizzi IP e maschere con vari livelli di difficoltà.
-   **🏆 Sistema di Punteggio**: Guadagna punti per ogni risposta corretta e scala la classifica in tempo reale.
-   **⏱️ Sfida a Tempo**: Barra di progresso dinamica per aggiungere pressione e divertimento.
-   **🔌 Architettura Client-Server**: Un Admin controlla l'inizio della partita, mentre i Client si sfidano simultaneamente.
-   **📱 UI Moderna & Responsive**: Interfaccia curata, scura e professionale, ottimizzata per la leggibilità.

---

## 🚀 Come Iniziare

### Admin (Server)
1. Compila ed esegui `GameServer.java`.
2. Accedi alla dashboard admin per gestire la partita e monitorare i punteggi.

### Giocatori (Client)
1. Collegati all'indirizzo IP del server (es. `http://[IP-SERVER]:8090`).
2. Inserisci il tuo nome.
3. Attendi che l'Admin dia il via alla partita!

---

## 🛠️ Tecnologie Utilizzate

-   **Backend**: Java (Standard Library) per la gestione del server HTTP e la logica dei quiz.
-   **Frontend**: HTML5, Vanilla CSS3 (Glassmorphism, Flexbox/Grid), JavaScript moderno.
-   **Versione**: 1.2.0

---

## 🎮 Game Rules
1. Identifica la **Classe** dell'IP (A, B o C).
2. Calcola l'indirizzo di **Rete** (tutti i bit host a 0).
3. Calcola l'indirizzo di **Broadcast** (tutti i bit host a 1).
4. Rispondi prima che scada il tempo!

---

*Sviluppato con ❤️ per l'apprendimento del networking.*
