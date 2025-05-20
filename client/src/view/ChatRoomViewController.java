package view;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import util.Attachment;
import viewModel.SortingMethod;
import viewModel.ViewMessage;
import viewModel.ViewRoom;
import viewModel.ViewRoomUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class ChatRoomViewController extends ViewController<viewModel.ChatRoomViewModel> {
    // References til ChatRoomView.fxml
    @FXML
    public VBox rooms;
    @FXML
    public Label roomName;
    @FXML
    public VBox attachments;
    @FXML
    public HBox composeSection;
    @FXML
    private TextField composeField;
    @FXML
    public VBox messages;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Text greetingText;
    @FXML
    private TextField searchRoomField;

    // "activity" eller "alphabetically"
    private String roomSortingMethod = "activity";

    private Map<Long, MessageBox> messageNodes = new HashMap<>();

    public ObjectProperty<ViewMessage> editingMessageProperty = new SimpleObjectProperty<>();

    @Override
    protected void init() {
        // Bindings
        composeField.textProperty().bindBidirectional(getViewModel().getComposeMessageProperty());
        greetingText.textProperty().bind(getViewModel().getGreetingTextProperty());
        searchRoomField.textProperty().bindBidirectional(getViewModel().getSearchFieldProperty());
        searchRoomField.textProperty().addListener(_ -> getViewModel().resetRooms());

        roomName.textProperty().bind(getViewModel().getRoomNameProperty());

        getViewModel().getColorProperty().addListener((__, oldColor, newColor) -> {
            System.out.println(newColor);
            messages.setStyle("-fx-background-color: " + newColor);
        });

        Button loadMoreButton = new Button();
        loadMoreButton.setText("Indlæs mere du");
        loadMoreButton.addEventHandler(ActionEvent.ACTION, evt -> getViewModel().loadOlderMessages());
        messages.getChildren().add(loadMoreButton);

        getViewModel().getCurrentRoomUserProperty().addListener((_, _, u) -> {
            composeSection.setDisable(u == null || u.getState().equals("Muted"));
            composeField.setText(u == null || u.getState().equals("Muted") ? "Du er muted :(" : "");
        });

        // Rum
        getViewModel().getRoomsProperty().addListener((ListChangeListener<ViewRoom>) change -> {
            rooms.getChildren().clear();

            change.getList().forEach(r -> {
                Button roomButton = new Button(r.getName());
                roomButton.setPrefWidth(150);
                roomButton.addEventHandler(ActionEvent.ACTION, evt -> getViewModel().setChatRoom(r.getRoomId()));
                rooms.getChildren().add(roomButton);
            });
        });

        // Beskeder
        getViewModel().getMessagesProperty().addListener((ListChangeListener<ViewMessage>) change -> {
            // Kaldes for at hente getRemoved og getAddedSubList
            change.next();

            // Fjern beskeder som er fjernet
            for (ViewMessage m : change.getRemoved()) {
                Node messageNode = messageNodes.get(m.messageId);
                if (messageNode != null) {
                    messages.getChildren().remove(messageNode);
                    messageNodes.remove(m.messageId);
                }
            }

            // Tilføj nye beskeder
            for (ViewMessage m : change.getAddedSubList()) {
                addMessageNode(m);
            }

            // Hvis listen er helt tom, er der højst sandsynligt blevet åbnet en anden chat, så nulstil scroll til bunden.
            if (change.getList().isEmpty()) {
                Platform.runLater(() -> {
                    previousScrollHeight = 0;
                    scrollPane.setVvalue(1.0);
                });
            }
        });

        // Lyt efter ændringer i brugerne rummet
        getViewModel().getRoomUsersProperty().addListener((ListChangeListener<ViewRoomUser>) change -> {
            change.next();

            // Brugere som er blevet tilføjet (eller ændret, da de bliver udskiftet med en ny ViewRoomUser
            for (ViewRoomUser user : change.getRemoved()) {
                if (messageNodes.containsKey(user.getLatestReadMessage())) {
                    messageNodes.get(user.getLatestReadMessage()).update();
                }
            }

            // Brugere som er blevet tilføjet (eller ændret, da de bliver udskiftet med en ny ViewRoomUser
            for (ViewRoomUser user : change.getAddedSubList()) {
                if (messageNodes.containsKey(user.getLatestReadMessage())) {
                    messageNodes.get(user.getLatestReadMessage()).update();
                }
            }

            updateScroll();
        });

        // Bilag
        getViewModel().getAttachmentsProperty().addListener((ListChangeListener<Attachment>) change -> {
            attachments.getChildren().clear();

            change.getList().forEach(attachment -> {
                VBox body = new VBox();
                body.getStyleClass().add("attachment-body");
                attachments.getChildren().add(body);

                try {
                    FileInputStream stream = new FileInputStream(attachment.getFile());

                    Image image = new Image(stream);
                    ImageView imageView = new ImageView(image);

                    imageView.setFitWidth(150);
                    imageView.setPreserveRatio(true);

                    body.getChildren().add(imageView);

                    stream.close();
                } catch (IOException e) {
                    Label errorLabel = new Label();
                    errorLabel.getStyleClass().add("message-error");
                    errorLabel.setText("Fejl ved at hente bilag: " + attachment.getName());
                    body.getChildren().add(errorLabel);
                }

                Button removeButton = new Button("Fjern");
                removeButton.addEventHandler(ActionEvent.ACTION, evt -> getViewModel().getAttachmentsProperty().remove(attachment));
                body.getChildren().add(removeButton);

                Label fileName = new Label(attachment.getName());
                fileName.getStyleClass().add("attachment-name");
                body.getChildren().add(fileName);
            });

        });

        editingMessageProperty.addListener((_, _, m) -> {
            composeField.setText(m == null ? "" : m.body);
        });

        scrollPane.setVvalue(1.0);

        messages.getChildren().addListener((ListChangeListener<Node>) _ -> updateScroll());
    }

    private double previousScrollHeight = 0;

    private void updateScroll() {
        Platform.runLater(() -> {
            double newScrollHeight = Math.max(messages.getHeight() - scrollPane.getHeight(), 0);

            scrollPane.setVvalue(scrollPane.getVvalue() + (newScrollHeight - previousScrollHeight) / newScrollHeight);

            previousScrollHeight = newScrollHeight;
        });
    }

    private void addMessageNode(ViewMessage m) {
        MessageBox messageAlignmentContainer = new MessageBox(m, this, getViewModel());

        messages.getChildren().add(messageAlignmentContainer);
        messageNodes.put(m.messageId, messageAlignmentContainer);

        FXCollections.sort(messages.getChildren(), Comparator.comparing(node -> {
            if (node instanceof MessageBox) {
                MessageBox box = (MessageBox) node;
                return box.getViewMessage().dateTime;
            }
            return LocalDateTime.MIN;
        }));
    }

    @FXML
    public void logud(ActionEvent actionEvent) {
        getViewHandler().openView(ViewID.LOGIN);
    }

    @FXML
    public void send(ActionEvent actionEvent) {
        if (editingMessageProperty.get() != null) {
            getViewModel().editMessage(editingMessageProperty.get().messageId);
            editingMessageProperty.set(null);
        } else {
            getViewModel().sendMessage();
        }
        composeField.clear();
        scrollPane.setVvalue(1.0);
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

    @FXML
    public void editNicknames() {
        getViewHandler().openView(ViewID.ROOM_USERS);
    }

    @FXML
    public void upload(ActionEvent actionEvent) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            File file = fileChooser.showOpenDialog(getRoot().getScene().getWindow());
            getViewModel().getAttachmentsProperty().add(new Attachment(file.getName(), file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void sortActivity() {
        getViewModel().setSortingMethod(SortingMethod.ACTIVITY);
        reset();
    }

    @FXML
    public void sortAlphabetically() {
        getViewModel().setSortingMethod(SortingMethod.ALPHABETICALLY);
        reset();
    }
}
