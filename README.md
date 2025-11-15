# 🎮 IP-Hunter Game Server

**IP-Hunter** is a Java server that runs an IP address quiz.  

Players connect through their browser to the *client* page to answer questions, while the *scoreboard* page shows the live ranking.  

<img width="866" height="404" alt="immagine" src="https://github.com/user-attachments/assets/c04df7fc-3072-4c16-84d3-7661079470de" />

---

## 📌 How It Works

- The server provides two web pages:

  - **client.html** → quiz interface  
  - **score.html** → players' leaderboard  

- HTML/CSS/JS files are served from the `src/` folder.  
- Each client connection is handled by a dedicated thread.  
- Quiz duration can be configured in the code.  
- Available URLs are displayed when the server starts.

---

## 🚀 Launching the JAR

``bash
java -jar Ip-Hunter.jar

⚙️ Configuration

Inside GameServer.java:

public static final int PORT = 8090;
public static final int QUIZ_DURATION_SECONDS = 30;
public static final String WEB_ROOT = "src";

    PORT → server port

    QUIZ_DURATION_SECONDS → time allowed to answer

    WEB_ROOT → folder containing web files

Images:

<img width="1350" height="646" alt="immagine" src="https://github.com/user-attachments/assets/90f70f79-324f-45dd-8e12-3fb7b9e76b05" />


<img width="1829" height="1220" alt="immagine" src="https://github.com/user-attachments/assets/e3d7dc16-7699-498c-a188-ba14119c335a" />


