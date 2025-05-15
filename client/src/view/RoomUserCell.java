package view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.text.Text;
import viewModel.ViewRoomUser;

import java.io.IOException;
import java.time.Duration;

public class RoomUserCell extends ListCell<ViewRoomUser> {

    @FXML
    private Text name;

    @FXML
    private Text lastOnline;

    public RoomUserCell() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RoomUserCell.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(ViewRoomUser item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        } else {
            name.setText(item.getName() + " <" + item.getNickname() + ">");

            if (item.getLastActive() + 120 * 1000 < System.currentTimeMillis()) {
                Duration duration = Duration.ofMillis(System.currentTimeMillis() - item.getLastActive());
                if (duration.toHours() > 24) {
                    lastOnline.setText("Sidst aktiv for " + duration.toDays() + " dage siden");
                } else if (duration.toHours() > 0) {
                    lastOnline.setText("Sidst aktiv for " + duration.toHours() + " timer siden");
                } else {
                    lastOnline.setText("Sidst aktiv for " + duration.toMinutes() + " minutter siden");
                }
            } else {
                lastOnline.setText("Online");
            }

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }

}
