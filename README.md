# 🎮 IP-Hunter Game Server

**IP-Hunter** è un server Java che gestisce un quiz sugli indirizzi IP.  
I giocatori si collegano tramite browser alla pagina *client* per rispondere alle domande, mentre la pagina *scoreboard* mostra la classifica aggiornata.

---

## 📌 Come funziona

- Il server espone due pagine web:
  - **client.html** → interfaccia del quiz  
  - **score.html** → classifica dei giocatori  
- I file HTML/CSS/JS vengono serviti dalla cartella `src/`.  
- Ogni client è gestito tramite un thread dedicato.  
- La durata del quiz è configurabile dal codice.  
- All’avvio vengono mostrati gli URL disponibili.

---


🚀 Avvio tramite JAR

java -jar Ip-Hunter.jar

⚙️ Configurazione

Dentro GameServer.java:

public static final int PORT = 8090;
public static final int QUIZ_DURATION_SECONDS = 30;
public static final String WEB_ROOT = "src";

    PORT → porta del server

    QUIZ_DURATION_SECONDS → tempo per rispondere

    WEB_ROOT → cartella dei file web




