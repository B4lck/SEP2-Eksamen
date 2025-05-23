import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import mediator.ChatClient;
import model.Message;
import model.Model;
import model.ModelManager;
import model.Reaction;
import util.ServerError;
import view.NotificationManager;
import view.ViewHandler;
import viewModel.ViewModelFactory;

public class MyApplication extends Application {
    public final static int PORT = 30000;
    public final static String HOST = "localhost";

    private ChatClient client;
    private Model model;

    @Override
    public void start(Stage primaryStage) {
        this.client = ChatClient.createInstance(HOST, PORT);
        this.model = new ModelManager();

        ViewModelFactory viewModelFactory = new ViewModelFactory(model);
        ViewHandler view = new ViewHandler(viewModelFactory);
        view.start(primaryStage);

        // Notifikationer
        NotificationManager notificationManager = NotificationManager.getNotificationManager();
        model.getMessagesManager().addListener(evt -> {
            // Hvis primary stage er i fokus, behøves notifikationer ikke
            if (primaryStage.isFocused()) return;
            // Lyt efter nye beskeder
            if (evt.getPropertyName().equals("NEW_MESSAGE")) {
                Message message = (Message) evt.getNewValue();
                // Send ikke system beskeder, eller egne beskeder som notifikationer
                if (message.getSentBy() == 0) return;
                if (message.getSentBy() == model.getProfileManager().getCurrentUserId()) return;
                if (model.getProfileManager().isBlocked(message.getSentBy())) return;
                // Send notifikation
                Platform.runLater(() -> {
                    try {
                        notificationManager.showNotification(model.getProfileManager().getProfile(message.getSentBy()).getUsername() + " har sendt dig en besked", message.getBody());
                    } catch (ServerError e) {
                        // Gør intet
                    }
                });
            } else if (evt.getPropertyName().equals("NEW_REACTION")) {
                Reaction reaction = (Reaction) evt.getNewValue();
                if (model.getProfileManager().isBlocked(reaction.getReactedBy())) return;
                // Send notifikation
                Platform.runLater(() -> {
                    try {
                        notificationManager.showNotification(model.getProfileManager().getProfile(reaction.getReactedBy()).getUsername() + " reageret med " + reaction.getReaction() + " på din besked!", "");
                    } catch (ServerError e) {
                        // Gør intet
                    }
                });
            }
        });

        primaryStage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("view/logo.png")));
    }
}
