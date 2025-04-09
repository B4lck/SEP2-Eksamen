import mediator.ClientHandler;
import model.Model;
import model.ChatModel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    public static final int PORT = 42069;


    public static void main(String[] args) {


        Model model = new ChatModel();

        try {
            ServerSocket welcomeSocket = new ServerSocket(PORT);

            while (true) {
                Socket socket = welcomeSocket.accept();
                System.out.println("Ny forbindelse fra " + socket.getRemoteSocketAddress());
                ClientHandler handler = new ClientHandler(socket, model);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}