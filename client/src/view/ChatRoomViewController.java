package view;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import viewModel.ViewMessage;
import viewModel.ViewRoom;

public class ChatRoomViewController extends ViewController<viewModel.ChatRoomViewModel> {
    // References til ChatRoomView.fxml
    @FXML
    public VBox rooms;
    @FXML
    public Label roomName;
    @FXML
    private TextField message;
    @FXML
    private VBox messages;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Text greetingText;

    // Context menu
    @FXML
    private ContextMenu contextMenu;
    @FXML
    private MenuItem editMessageItem;
    @FXML
    private MenuItem deleteMessageItem;
    private ViewMessage highlightedMessage;
    private boolean editing = false;

    @Override
    protected void init() {
        // Context menu
        contextMenu = new ContextMenu();
        editMessageItem = new MenuItem("Rediger");
        deleteMessageItem = new MenuItem("Fjern");

        editMessageItem.setOnAction((_) -> {
            editing = true;
            message.textProperty().setValue(highlightedMessage.body);
        });

        deleteMessageItem.setOnAction((_) -> getViewModel().deleteMessage(highlightedMessage.messageId));

        contextMenu.getItems().addAll(editMessageItem, deleteMessageItem);

        // Bindings
        message.textProperty().bindBidirectional(getViewModel().getComposeMessageProperty());
        greetingText.textProperty().bind(getViewModel().getGreetingTextProperty());

        roomName.textProperty().bind(getViewModel().getRoomNameProperty());

        // Rum
        getViewModel().getChatRoomsProperty().addListener((ListChangeListener<ViewRoom>) change -> {
            rooms.getChildren().clear();
            change.getList().forEach(r -> {
                Button roomButton = new Button(r.name);
                roomButton.addEventHandler(ActionEvent.ACTION, evt -> {
                    getViewModel().setChatRoom(r.roomId);
                });
                rooms.getChildren().add(roomButton);
            });
        });

        // Beskeder
        getViewModel().getMessagesProperty().addListener((ListChangeListener<ViewMessage>) change -> {
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

                messageContainer.setOnContextMenuRequested(e -> {
                    highlightedMessage = m;
                    contextMenu.show(messageContainer, e.getScreenX(), e.getScreenY());
                });

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

    @FXML
    public void logud(ActionEvent actionEvent) {
        getViewHandler().openView(ViewID.LOGIN);
    }

    @FXML
    public void send(ActionEvent actionEvent) {
        if (editing) {
            getViewModel().editMessage(highlightedMessage.messageId);
            message.clear();
            scrollPane.setVvalue(1.0);
        } else {
            getViewModel().sendMessage();
            message.clear();
            scrollPane.setVvalue(1.0);
        }
    }

    @FXML
    public void createRoom() {
        getViewModel().setChatRoom(-1);
        getViewHandler().openView(ViewID.CREATE_EDIT_ROOM);
    }

    @FXML
    public void editRoom(ActionEvent actionEvent) {
        getViewHandler().openView(ViewID.CREATE_EDIT_ROOM);
    }
}
