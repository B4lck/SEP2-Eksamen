import javafx.application.Application;
import javafx.stage.Stage;
import mediator.ChatClient;
import model.Model;
import model.ModelManager;
import view.ViewHandler;
import viewmodel.ViewModelFactory;

public class MyApplication extends Application {
    public final static int PORT = 42069;
    public final static String HOST = "localhost";

    private ChatClient client;
    private Model model;

    @Override
    public void start(Stage primaryStage) {
        try {
            this.client = new ChatClient(HOST, PORT);
            this.model = new ModelManager(client);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }

        ViewModelFactory viewModelFactory = new ViewModelFactory(model);
        ViewHandler view = new ViewHandler(viewModelFactory);
        view.start(primaryStage);
    }
}
