import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.InetAddress;

public class GameServer {

    final String RESET = "\u001B[0m";
    final String CYAN = "\u001B[36m";
    final String GREEN = "\u001B[32m";
    final String YELLOW = "\u001B[33m";
    final String PURPLE = "\u001B[35m";
    // In sviluppo tieni i file in src/resources; in jar saranno comunque nella classpath sotto /resources
    public static final String WEB_ROOT = "src/resources";
    public static final int PORT = 8090;
    public static final int QUIZ_DURATION_SECONDS = 110; // Tempo per rispondere

    // Mappa dei punteggi per utente
    public static Map<String, Integer> userScores = new HashMap<>();

    // Set di tutti gli utenti connessi (anche con punteggio 0)
    public static Set<String> connectedUsers = new HashSet<>();

    // Stato della partita
    public static boolean gameActive = false;

    // Quiz correnti per ogni utente (user -> QuizData)
    public static Map<String, QuizData> activeQuizzes = new HashMap<>();

    // Timestamp dell'ultimo reset
    public static long lastResetTimestamp = 0;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            // Per usare i colori non statici
            GameServer gs = new GameServer();

            // DATA E ORA
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            // IP LOCALE
            String localIp = "localhost";
            try {
                localIp = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception ignored) {}

            // BANNER STILOSO
            System.out.println(gs.CYAN +
                    "\n=====================================================");
            System.out.println("                IP-HUNTER GAME SERVER");
            System.out.println("=====================================================" + gs.RESET);

            System.out.println(gs.PURPLE + " Avvio: " + gs.RESET + now);
            System.out.println(gs.PURPLE + " Porta: " + gs.RESET + PORT);

            System.out.println(gs.PURPLE + " URL disponibili:" + gs.RESET);
            System.out.println("   ➤ " + gs.GREEN + "Client: " + gs.RESET + "http://localhost:" + PORT + "/client.html");
            System.out.println("   ➤ " + gs.GREEN + "Score:  " + gs.RESET + "http://localhost:" + PORT + "/score.html");

            System.out.println(gs.PURPLE + " URL in LAN:" + gs.RESET);
            System.out.println("   ➤ " + gs.YELLOW + "Client: " + gs.RESET + "http://" + localIp + ":" + PORT + "/client.html");
            System.out.println("   ➤ " + gs.YELLOW + "Score:  " + gs.RESET + "http://" + localIp + ":" + PORT + "/score.html");

            System.out.println(gs.CYAN +
                    "=====================================================\n" + gs.RESET);

            // LOOP SERVER
            while (true) {
                Socket client = serverSocket.accept();
                new Thread(() -> handleClient(client)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void handleClient(Socket client) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                OutputStream out = client.getOutputStream()
        ) {
            String req = in.readLine();
            if (req == null) {
                client.close();
                return;
            }

            String[] parts = req.split(" ");
            if (parts.length < 2) {
                send404(out);
                client.close();
                return;
            }
            String path = parts[1];

            // ---------- API: genera nuovo quiz per un utente specifico ----------
            if (path.startsWith("/api/new")) {
                String query = path.contains("?") ? path.split("\\?")[1] : "";
                Map<String, String> params = parseQuery(query);

                String user = params.getOrDefault("user", "guest");

                // Registra l'utente come connesso
                connectedUsers.add(user);
                if (!userScores.containsKey(user)) {
                    userScores.put(user, 0);
                }

                if (!gameActive) {
                    String json = "{\"error\":\"Partita non ancora avviata\",\"gameActive\":false}";
                    sendJSON(out, json);
                } else {
                    QuizData quiz = generateQuiz();
                    activeQuizzes.put(user, quiz);

                    String json = String.format(
                            "{\"ip\":\"%s\",\"mask\":\"%s\",\"gameActive\":true,\"timeLimit\":%d,\"quizId\":\"%s\"}",
                            quiz.ip, quiz.mask, QUIZ_DURATION_SECONDS, quiz.id
                    );
                    sendJSON(out, json);
                }
                client.close();
                return;
            }

            // ---------- API: verifica risposte ----------
            if (path.startsWith("/api/check")) {
                if (!gameActive) {
                    String json = "{\"error\":\"Partita terminata\",\"gameActive\":false}";
                    sendJSON(out, json);
                    client.close();
                    return;
                }

                String query = path.contains("?") ? path.split("\\?")[1] : "";
                Map<String, String> params = parseQuery(query);

                String user = params.getOrDefault("user", "guest");
                String quizId = params.getOrDefault("quizId", "");
                String userClass = params.getOrDefault("class", "");
                String userNetwork = params.getOrDefault("network", "");
                String userBroadcast = params.getOrDefault("broadcast", "");

                QuizData quiz = activeQuizzes.get(user);
                boolean timeExpired = false;
                boolean ok = false;

                if (quiz != null && quiz.id.equals(quizId)) {
                    long now = System.currentTimeMillis();
                    if (now > quiz.expiryTime) {
                        timeExpired = true;
                    } else {
                        String correctClass = getClassOfIP(quiz.ip);
                        String correctBroadcast = getBroadcast(quiz.ip, quiz.mask);
                        String correctNetwork = getNetwork(quiz.ip, quiz.mask);

                        ok = userClass.equalsIgnoreCase(correctClass)
                                && userBroadcast.equals(correctBroadcast)
                                && userNetwork.equals(correctNetwork);

                        if (ok) {
                            userScores.put(user, userScores.getOrDefault(user, 0) + 1);
                        }
                    }

                    // Rimuovi quiz usato
                    activeQuizzes.remove(user);
                }

                String json = String.format(
                        "{\"ok\":%b,\"timeExpired\":%b,\"score\":%d,\"gameActive\":true}",
                        ok, timeExpired, userScores.getOrDefault(user, 0)
                );
                sendJSON(out, json);
                client.close();
                return;
            }

            // ---------- API: restituisce tutti i punteggi (solo per admin) ----------
            if (path.equals("/api/scores")) {
                StringBuilder sb = new StringBuilder();
                sb.append("{\"scores\":{");
                boolean first = true;

                for (String user : connectedUsers) {
                    if (!first) sb.append(",");
                    sb.append("\"").append(user).append("\":").append(userScores.getOrDefault(user, 0));
                    first = false;
                }

                sb.append("},\"gameActive\":").append(gameActive);
                sb.append(",\"activeUsers\":").append(activeQuizzes.size());
                sb.append(",\"resetTimestamp\":").append(lastResetTimestamp);
                sb.append("}");
                sendJSON(out, sb.toString());
                client.close();
                return;
            }

            // ---------- API: avvia partita (admin) ----------
            if (path.equals("/api/admin/start")) {
                gameActive = true;
                String json = "{\"success\":true,\"message\":\"Partita avviata!\",\"gameActive\":true}";
                sendJSON(out, json);
                client.close();
                return;
            }

            // ---------- API: termina partita (admin) ----------
            if (path.equals("/api/admin/stop")) {
                gameActive = false;
                activeQuizzes.clear();
                String json = "{\"success\":true,\"message\":\"Partita terminata!\",\"gameActive\":false}";
                sendJSON(out, json);
                client.close();
                return;
            }

            // ---------- API: resetta punteggi (admin) ----------
            if (path.equals("/api/admin/reset")) {
                userScores.clear();
                connectedUsers.clear();
                activeQuizzes.clear();
                lastResetTimestamp = System.currentTimeMillis();
                String json = "{\"success\":true,\"message\":\"Punteggi resettati!\",\"resetTimestamp\":" + lastResetTimestamp + "}";
                sendJSON(out, json);
                client.close();
                return;
            }

            // ---------- API: stato partita ----------
            if (path.equals("/api/status")) {
                String json = "{\"gameActive\":" + gameActive + ",\"resetTimestamp\":" + lastResetTimestamp + "}";
                sendJSON(out, json);
                client.close();
                return;
            }

            // ---------- Serve file statici (dal filesystem o dal JAR/classpath) ----------
            // Prendi solo il percorso (senza query string)
            String rawPath = path.contains("?") ? path.split("\\?")[0] : path;
            if (rawPath.equals("/")) rawPath = "/client.html";

            // Previeni directory traversal
            if (rawPath.contains("..")) {
                send404(out);
                client.close();
                return;
            }

            File file = new File(WEB_ROOT + rawPath);
            byte[] content = null;

            // Prova filesystem (utile in sviluppo / IDE)
            if (file.exists() && file.isFile()) {
                content = Files.readAllBytes(file.toPath());
            } else {
                // Prova a leggere dalla classpath dentro il JAR: /resources + rawPath
                try (InputStream is = GameServer.class.getResourceAsStream("/resources" + rawPath)) {
                    if (is != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[4096];
                        int n;
                        while ((n = is.read(buffer)) > 0) baos.write(buffer, 0, n);
                        content = baos.toByteArray();
                    }
                }
            }

            if (content != null) {
                // Determina il content type
                String contentType = "text/html; charset=UTF-8";
                if (rawPath.endsWith(".css")) contentType = "text/css";
                else if (rawPath.endsWith(".js")) contentType = "application/javascript";
                else if (rawPath.endsWith(".json")) contentType = "application/json";
                else if (rawPath.endsWith(".png")) contentType = "image/png";
                else if (rawPath.endsWith(".jpg") || rawPath.endsWith(".jpeg")) contentType = "image/jpeg";
                else if (rawPath.endsWith(".svg")) contentType = "image/svg+xml";

                out.write("HTTP/1.1 200 OK\r\n".getBytes());
                out.write(("Content-Type: " + contentType + "\r\n").getBytes());
                out.write(("Content-Length: " + content.length + "\r\n").getBytes());
                out.write("Cache-Control: no-cache\r\n".getBytes());
                out.write("\r\n".getBytes());
                out.write(content);
            } else {
                send404(out);
            }

            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> parseQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        for (String p : query.split("&")) {
            if (p.isEmpty()) continue;
            String[] kv = p.split("=", 2);
            if (kv.length == 2) {
                params.put(kv[0], URLDecoder.decode(kv[1], "UTF-8"));
            } else {
                params.put(kv[0], "");
            }
        }
        return params;
    }

    private static void sendJSON(OutputStream out, String json) throws Exception {
        byte[] bytes = json.getBytes("UTF-8");
        out.write("HTTP/1.1 200 OK\r\n".getBytes());
        out.write("Content-Type: application/json; charset=UTF-8\r\n".getBytes());
        out.write(("Content-Length: " + bytes.length + "\r\n\r\n").getBytes());
        out.write(bytes);
    }

    private static void send404(OutputStream out) throws Exception {
        String body = "404";
        byte[] bytes = body.getBytes("UTF-8");
        out.write("HTTP/1.1 404 NOT FOUND\r\n".getBytes());
        out.write("Content-Type: text/plain; charset=UTF-8\r\n".getBytes());
        out.write(("Content-Length: " + bytes.length + "\r\n\r\n").getBytes());
        out.write(bytes);
    }

    // === LOGICA DEL GIOCO ===
    public static QuizData generateQuiz() {
        Random r = new Random();
        String ip = (r.nextInt(223) + 1) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);

        // Array di possibili subnet mask
        String[] possibleMasks = {
                "255.0.0.0",       // /8
                "255.128.0.0",     // /9
                "255.192.0.0",     // /10
                "255.224.0.0",     // /11
                "255.240.0.0",     // /12
                "255.248.0.0",     // /13
                "255.252.0.0",     // /14
                "255.254.0.0",     // /15
                "255.255.0.0",     // /16
                "255.255.128.0",   // /17
                "255.255.192.0",   // /18
                "255.255.224.0",   // /19
                "255.255.240.0",   // /20
                "255.255.248.0",   // /21
                "255.255.252.0",   // /22
                "255.255.254.0",   // /23
                "255.255.255.0",   // /24
                "255.255.255.128", // /25
                "255.255.255.192", // /26
                "255.255.255.224", // /27
                "255.255.255.240", // /28
                "255.255.255.248", // /29
                "255.255.255.252"  // /30
        };

        String mask = possibleMasks[r.nextInt(possibleMasks.length)];
        long expiryTime = System.currentTimeMillis() + (QUIZ_DURATION_SECONDS * 1000);
        String id = UUID.randomUUID().toString();
        return new QuizData(ip, mask, expiryTime, id);
    }

    public static String getClassOfIP(String ip) {
        int first = Integer.parseInt(ip.split("\\.")[0]);
        if (first <= 126) return "A";
        if (first <= 191) return "B";
        return "C";
    }

    public static String getNetwork(String ip, String mask) {
        try {
            String[] ipParts = ip.split("\\.");
            String[] maskParts = mask.split("\\.");

            int[] ipInt = new int[4];
            int[] maskInt = new int[4];

            for (int i = 0; i < 4; i++) {
                ipInt[i] = Integer.parseInt(ipParts[i]);
                maskInt[i] = Integer.parseInt(maskParts[i]);
            }

            int[] network = new int[4];
            for (int i = 0; i < 4; i++) {
                network[i] = ipInt[i] & maskInt[i];
            }

            return network[0] + "." + network[1] + "." + network[2] + "." + network[3];
        } catch (Exception e) {
            return "0.0.0.0";
        }
    }

    public static String getBroadcast(String ip, String mask) {
        try {
            String[] ipParts = ip.split("\\.");
            String[] maskParts = mask.split("\\.");

            int[] ipInt = new int[4];
            int[] maskInt = new int[4];

            for (int i = 0; i < 4; i++) {
                ipInt[i] = Integer.parseInt(ipParts[i]);
                maskInt[i] = Integer.parseInt(maskParts[i]);
            }

            int[] broadcast = new int[4];
            for (int i = 0; i < 4; i++) {
                broadcast[i] = ipInt[i] | (~maskInt[i] & 0xFF);
            }

            return broadcast[0] + "." + broadcast[1] + "." + broadcast[2] + "." + broadcast[3];
        } catch (Exception e) {
            return "255.255.255.255";
        }
    }

    static class QuizData {
        String ip;
        String mask;
        long expiryTime;
        String id;

        QuizData(String ip, String mask, long expiryTime, String id) {
            this.ip = ip;
            this.mask = mask;
            this.expiryTime = expiryTime;
            this.id = id;
        }
    }
}
