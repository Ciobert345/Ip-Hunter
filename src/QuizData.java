public class QuizData {

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
