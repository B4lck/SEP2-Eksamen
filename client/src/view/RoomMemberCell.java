package view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.text.Text;
import viewModel.ViewRoomMember;

import java.io.IOException;
import java.time.Duration;

public class RoomMemberCell extends ListCell<ViewRoomMember> {

    @FXML
    private Text name;

    @FXML
    private Text lastOnline;

    public RoomMemberCell() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RoomMemberCell.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(ViewRoomMember item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        } else {
            String prefix = item.getState().equals("Admin") ? "\uD83D\uDC51 " : (item.getState().equals("Muted") ? "❌ " : " ");
            name.setText(prefix + item.getName() + " <" + item.getNickname() + ">");

            if (item.isBlocked()) {
                lastOnline.setText("Du har blokeret denne bruger.");
            }
            else if (item.getLastActive() + 120 * 1000 < System.currentTimeMillis()) {
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
