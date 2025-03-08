
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            int port = 9889;

            TaskServer server = new TaskServer("localhost", port);
            server.start();

            System.out.println("Сервер запущен на http://localhost:" + port + "/");
        } catch (IOException e) {
            System.err.println("Ошибка при запуске сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }
}