package view;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import viewModel.ViewMessage;

public class ChatRoomViewController extends ViewController<viewModel.ChatRoomViewModel> {
    @FXML
    private TextField message;
    @FXML
    private VBox messages;
    @FXML
    private ScrollPane scrollPane;

    @Override
    protected void init() {
        message.textProperty().bindBidirectional(getViewModel().getComposeMessageProperty());


        getViewModel().getMessagesProperty().addListener((ListChangeListener<ViewMessage>) change -> {
            System.out.println(scrollPane.getVvalue());
            // Hvis der er scrollet ned i bunden, skal den stadigvæk være scrollet helt ned, efter de nye beskeder bliver tilføjet.
            var isScrolledDown = scrollPane.getVvalue() >= 1.0;
            // TODO: Kun opret nye beskeder? Bemærk de nye beskeder vil kunne dukke op før og efter de beskeder som allerede
            //       er vist, alt efter om der bliver modtaget nye beskeder, eller brugere går tilbage i chatten.
            messages.getChildren().clear();
            // Opret elementer
            change.getList().forEach(m -> {
                HBox messageContainer = new HBox();
                messageContainer.getStyleClass().add("message-container");

                Label messageTime = new Label();
                messageTime.getStyleClass().add("message-time");
                messageTime.setText("%02d:%02d".formatted(m.dateTime.getHour(), m.dateTime.getMinute()));
                messageContainer.getChildren().add(messageTime);

                Label messageSender = new Label();
                messageSender.getStyleClass().add("message-sender");
                messageSender.setText(m.sender);
                messageContainer.getChildren().add(messageSender);

                Label messageBody = new Label();
                messageBody.getStyleClass().add("message-body");
                messageBody.setText(m.body);
                messageContainer.getChildren().add(messageBody);

                messages.getChildren().add(messageContainer);
            });
            // Lad viewet opdater, før den scroller ned.
            Platform.runLater(() -> {
                if (isScrolledDown) scrollPane.setVvalue(1.0);
            });
        });
    }

    @Override
    public void reset() {
        super.reset();
        getViewModel().reset();
    }

    public void logud(ActionEvent actionEvent) {
        getViewHandler().openView(ViewID.LOGIN);
    }

    public void send(ActionEvent actionEvent) {
        getViewModel().sendMessage();
        message.clear();
        scrollPane.setVvalue(1.0);
    }
}
