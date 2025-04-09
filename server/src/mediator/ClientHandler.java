package mediator;

import com.google.gson.Gson;
import model.Model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable, PropertyChangeListener {
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson;

    public ClientHandler(Socket socket, Model model) throws IOException {
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.gson = new Gson();
    }

    public void setAuthenticatedUser(long userId) {
        // IDK?!?
    }

    @Override
    public void run() {
        while (true) {
            try {
                String req = in.readLine();
                switch (req.toUpperCase()) {
                    case "LOG_IN":
                    case "SIGN_UP":
                        break;
                    default:
                        throw new IllegalArgumentException("Forstod ikke en besked som blev modtaget");
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }
}
