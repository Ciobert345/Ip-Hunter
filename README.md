# 🎮 IP-Hunter Game Server

**IP-Hunter** è un server Java che gestisce un quiz sugli indirizzi IP.  
I giocatori si collegano tramite browser alla pagina *client* per rispondere alle domande, mentre la pagina *scoreboard* mostra la classifica aggiornata.


<img width="866" height="404" alt="immagine" src="https://github.com/user-attachments/assets/c04df7fc-3072-4c16-84d3-7661079470de" />

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


<img width="1350" height="646" alt="immagine" src="https://github.com/user-attachments/assets/90f70f79-324f-45dd-8e12-3fb7b9e76b05" />


<img width="1829" height="1220" alt="immagine" src="https://github.com/user-attachments/assets/e3d7dc16-7699-498c-a188-ba14119c335a" />


