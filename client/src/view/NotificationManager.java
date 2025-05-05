package view;

import java.awt.*;

public class NotificationManager {

    private static NotificationManager instance = new NotificationManager();

    private TrayIcon trayIcon;

    private NotificationManager() {
        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("logo.png"));

        trayIcon = new TrayIcon(image, "SEP2 Chat");
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("SEP2 Chat");
        try {
            tray.add(trayIcon);
        }
        catch (AWTException e) {
            e.printStackTrace();
            System.out.println("Kunne ikke oprette tray icon");
        }
    }

    public static NotificationManager getNotificationManager() {
        return instance;
    }

    public void showNotification(String title, String message) {
        trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
    }
}
