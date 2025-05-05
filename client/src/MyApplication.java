import javafx.application.Application;
import javafx.stage.Stage;
import mediator.ChatClient;
import model.Model;
import model.ModelManager;
import view.ViewHandler;
import viewModel.ViewModelFactory;

import java.awt.*;

public class MyApplication extends Application {
    public final static int PORT = 42069;
    public final static String HOST = "localhost";

    private ChatClient client;
    private Model model;

    @Override
    public void start(Stage primaryStage) {
        this.client = ChatClient.getInstance();
        this.model = new ModelManager();

        ViewModelFactory viewModelFactory = new ViewModelFactory(model);
        ViewHandler view = new ViewHandler(viewModelFactory);
        view.start(primaryStage);

//        try {
//            displayTray();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

//    public void displayTray() throws AWTException, InterruptedException {
//        Thread.sleep(1000);
//        //Obtain only one instance of the SystemTray object
//        SystemTray tray = SystemTray.getSystemTray();
//
//        //If the icon is a file
//        Image image = Toolkit.getDefaultToolkit().createImage("view/viaLogo.png");
//        //Alternative (if the icon is on the classpath):
//        //Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));
//
//        TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
//        //Let the system resize the image if needed
//        trayIcon.setImageAutoSize(true);
//        //Set tooltip text for the tray icon
//        trayIcon.setToolTip("System tray icon demo");
//        tray.add(trayIcon);
//
//        trayIcon.displayMessage("Hello, World", "notification demo", TrayIcon.MessageType.INFO);
//    }
}
