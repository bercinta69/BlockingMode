import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class BlockingClient {
    public static void main(String[] args) {
        String serverAddress = "localhost"; // Alamat server
        int port = 8888; // Port server

        try {
            Socket socket = new Socket(serverAddress, port); // Membuat socket untuk terhubung ke server
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Input dari server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // Output ke server

            // Thread untuk membaca pesan dari server
            new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage); // Menampilkan pesan dari server
                    }
                } catch (IOException e) {
                    System.out.println("Terputus dari server"); // Pesan jika terputus dari server
                }
            }).start();

            // Mengirim pesan ke server
            Scanner scanner = new Scanner(System.in);
            String message;
            while (true) {
                message = scanner.nextLine(); // Membaca pesan dari pengguna
                out.println(message); // Mengirim pesan ke server
                if (message.equalsIgnoreCase("exit")) { // Jika pesan 'exit' dikirim, loop berakhir
                    break;
                }
            }

            in.close(); // Menutup input stream
            out.close(); // Menutup output stream
            socket.close(); // Menutup koneksi ke server
        } catch (IOException e) {
            System.out.println("Tidak dapat terhubung ke server"); // Pesan jika tidak dapat terhubung ke server
        }
    }
}
