import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockingServer {
    private ServerSocket serverSocket;
    private ExecutorService clientThreadPool;
    private List<ClientHandler> clients;

    // Konstruktor untuk inisialisasi server
    public BlockingServer(int port) {
        try {
            serverSocket = new ServerSocket(port); // Membuat server socket
            clientThreadPool = Executors.newFixedThreadPool(10); // Thread pool untuk klien
            clients = new ArrayList<>(); // List untuk menyimpan handler klien
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Memulai server untuk menerima koneksi dari klien
    public void startServer() {
        System.out.println("Server Aktif. Menunggu koneksi dari Client...");

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept(); // Menerima koneksi dari klien
                System.out.println("Client Terhubung: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler); // Menambahkan handler klien ke daftar
                clientThreadPool.submit(clientHandler); // Menjalankan handler klien di thread pool
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Mengirim pesan ke semua klien kecuali pengirimnya
    public void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message); // Mengirim pesan ke klien
            }
        }
    }

    // Menghapus handler klien yang terputus
    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    // Method utama untuk menjalankan server
    public static void main(String[] args) {
        int port = 8888;
        BlockingServer server = new BlockingServer(port);
        server.startServer();
    }
}

// Class untuk menangani setiap klien yang terkoneksi
class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private BlockingServer server;

    // Konstruktor untuk setiap handler klien
    public ClientHandler(Socket socket, BlockingServer server) {
        this.clientSocket = socket;
        this.server = server;

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Membaca pesan dari klien
            out = new PrintWriter(clientSocket.getOutputStream(), true); // Mengirim pesan ke klien
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Mengirim pesan ke klien
    public void sendMessage(String message) {
        out.println(message);
    }

    @Override
    public void run() {
        String clientName = "Client-" + Thread.currentThread().getId(); // ID unik untuk setiap klien

        try {
            out.println("Selamat datang di chat! Anda adalah " + clientName); // Pesan selamat datang ke klien

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(clientName + " mengatakan: " + inputLine); // Menampilkan pesan dari klien

                if (inputLine.toLowerCase().startsWith("server:")) {
                    out.println("Pesan diterima server"); // Merespon pesan yang dimulai dengan "server:"
                } else {
                    server.broadcastMessage(clientName + ": " + inputLine, this); // Mengirim pesan ke klien lain
                }

                if (inputLine.equalsIgnoreCase("exit")) {
                    break; // Keluar dari loop jika klien mengetik "exit"
                }
            }

            System.out.println(clientName + " terputus"); // Menampilkan pesan jika klien terputus
            out.println(clientName + " terputus"); // Memberi tahu klien bahwa mereka terputus
            in.close();
            out.close();
            clientSocket.close();
            server.removeClient(this); // Menghapus handler klien yang terputus
        } catch (IOException e) {
            System.out.println("Klien terputus secara tiba-tiba"); // Pesan jika klien terputus secara tiba-tiba
            server.removeClient(this); // Menghapus handler klien yang terputus
        }
    }
}
