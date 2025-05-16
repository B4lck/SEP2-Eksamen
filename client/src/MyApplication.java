import javafx.application.Application;
import javafx.stage.Stage;
import mediator.ChatClient;
import model.Model;
import model.ModelManager;
import view.ViewHandler;
import viewModel.ViewModelFactory;

public class MyApplication extends Application {
    public final static int PORT = 30000;
    public final static String HOST = "10.154.216.82";

    private ChatClient client;
    private Model model;

    @Override
    public void start(Stage primaryStage) {
        this.client = ChatClient.createInstance(HOST, PORT);
        this.model = new ModelManager();


        ViewModelFactory viewModelFactory = new ViewModelFactory(model);
        ViewHandler view = new ViewHandler(viewModelFactory, model);
        view.start(primaryStage);

        primaryStage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("view/logo.png")));
    }
}
