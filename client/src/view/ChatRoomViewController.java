package view;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import util.Attachment;
import viewModel.ViewMessage;
import viewModel.ViewRoom;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ChatRoomViewController extends ViewController<viewModel.ChatRoomViewModel> {
    // References til ChatRoomView.fxml
    @FXML
    public VBox rooms;
    @FXML
    public Label roomName;
    @FXML
    public VBox attachments;
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
            var scrollAfter = scrollPane.getVvalue() >= 1.0;

            messages.getChildren().clear();

            Button loadMoreButton = new Button();

            loadMoreButton.setText("Indlæs mere du");

            loadMoreButton.addEventHandler(ActionEvent.ACTION, evt -> {
                getViewModel().loadOlderMessages();
            });

            messages.getChildren().add(loadMoreButton);

            // Opret elementer
            change.getList().forEach(m -> {
                HBox messageContainer = new HBox();
                messageContainer.getStyleClass().add("message-container");

                if (!m.isSystemMessage) {
                    Label messageTime = new Label();
                    messageTime.getStyleClass().add("message-time");
                    messageTime.setText("%02d:%02d".formatted(m.dateTime.getHour(), m.dateTime.getMinute()));
                    messageContainer.getChildren().add(messageTime);

                    Label messageSender = new Label();
                    messageSender.getStyleClass().add("message-sender");
                    messageSender.setText(m.sender + "(" + m.messageId + ")");
                    messageContainer.getChildren().add(messageSender);
                }

                VBox body = new VBox();
                body.getStyleClass().add("message-body");
                messageContainer.getChildren().add(body);

                Label messageBody = new Label();
                messageBody.getStyleClass().add("message-body-text");
                messageBody.setText(m.body);
                body.getChildren().add(messageBody);

                for (File attachment : m.attachments) {
                    try {
                        HBox attachmentBox = new HBox();
                        attachmentBox.getStyleClass().add("message-attachment");
                        body.getChildren().add(attachmentBox);

                        if (isImageFile(attachment)) {
                            Image image = new Image(new FileInputStream(attachment));
                            ImageView imageView = new ImageView(image);

                            imageView.setFitWidth(300);
                            imageView.setPreserveRatio(true);

                            attachmentBox.getChildren().add(imageView);
                        } else {
                            Label attachmentFile = new Label();
                            attachmentFile.getStyleClass().add("message-attachment-file");
                            attachmentFile.setText(attachment.getName());
                            attachmentBox.getChildren().add(attachmentFile);
                        }

                        // Filen er allerede downloaded for at kunne vises, men den her metode viser hvor på computeren den er downloadet
                        Button button = new Button("Download");
                        button.addEventHandler(ActionEvent.ACTION, evt -> {
                            Desktop.getDesktop().browseFileDirectory(attachment);
                        });
                        attachmentBox.getChildren().add(button);
                    } catch (FileNotFoundException e) {
                        Label errorLabel = new Label();
                        errorLabel.getStyleClass().add("message-error");
                        errorLabel.setText("Komme ikke hente bilag: " + attachment.getName());
                        messageContainer.getChildren().add(errorLabel);
                    }
                }

                // TODO: Context menuen skal kun på, hvis det er brugeren egen besked
                messageContainer.setOnContextMenuRequested(e -> {
                    highlightedMessage = m;
                    contextMenu.show(messageContainer, e.getScreenX(), e.getScreenY());
                });

                messages.getChildren().add(messageContainer);
            });
            // Lad viewet opdater, før den scroller ned.
            Platform.runLater(() -> {
                if (scrollAfter) scrollPane.setVvalue(1.0);
            });
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
                removeButton.addEventHandler(ActionEvent.ACTION, evt -> {
                    getViewModel().getAttachmentsProperty().remove(attachment);
                });
                body.getChildren().add(removeButton);

                Label fileName = new Label(attachment.getName());
                fileName.getStyleClass().add("attachment-name");
                body.getChildren().add(fileName);
            });

        });

        scrollPane.setVvalue(1.0);
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
            editing = false;
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

    // Hjælpemetode, der tjekker om en fil er af et understøttet billede-format til at kunne vises.
    private boolean isImageFile(File file) {
        try {
            return ImageIO.getImageReadersBySuffix(getFileExtension(file.getName())).hasNext();
        } catch (Exception e) {
            return false;
        }
    }

    // Henter filens extension-navn ud fra filnavnet
    private String getFileExtension(String filename) {
        int i = filename.lastIndexOf('.');
        // Ikke alle filer har en extension
        if (i == -1) return "";
        return filename.substring(i + 1);
    }
}
