package view;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import util.Attachment;
import viewModel.ViewMessage;
import viewModel.ViewReaction;
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

    private ViewMessage highlightedMessage;
    private boolean editing = false;

    @Override
    protected void init() {
        // Bindings
        message.textProperty().bindBidirectional(getViewModel().getComposeMessageProperty());
        greetingText.textProperty().bind(getViewModel().getGreetingTextProperty());

        roomName.textProperty().bind(getViewModel().getRoomNameProperty());

        // Rum
        getViewModel().getChatRoomsProperty().addListener((ListChangeListener<ViewRoom>) change -> {
            rooms.getChildren().clear();
            change.getList().forEach(r -> {
                Button roomButton = new Button(r.name);
                roomButton.setPrefWidth(150);
                roomButton.addEventHandler(ActionEvent.ACTION, evt -> getViewModel().setChatRoom(r.roomId));
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

            loadMoreButton.addEventHandler(ActionEvent.ACTION, evt -> getViewModel().loadOlderMessages());

            messages.getChildren().add(loadMoreButton);

            // Opret elementer
            change.getList().forEach(m -> {
                HBox messageAlignmentContainer = new HBox();
                messageAlignmentContainer.getStyleClass().add("message-alignment-container");
                messageAlignmentContainer.setAlignment(m.isMyMessage ? Pos.TOP_RIGHT : Pos.TOP_LEFT);

                VBox messageContainer = new VBox();
                messageContainer.getStyleClass().add("message-container");
                messageAlignmentContainer.getChildren().add(messageContainer);

                if (!m.isSystemMessage) {
                    HBox messageHeader = new HBox();
                    messageHeader.getStyleClass().add("message-header");
                    messageContainer.getChildren().add(messageHeader);

                    Text messageTime = new Text();
                    messageTime.getStyleClass().add("message-time");
                    messageTime.setText("%02d:%02d ".formatted(m.dateTime.getHour(), m.dateTime.getMinute()));
                    messageHeader.getChildren().add(messageTime);

                    Text messageSender = new Text();
                    messageSender.getStyleClass().add("message-sender");
                    messageSender.setText(m.sender + " ");
                    messageHeader.getChildren().add(messageSender);
                } else {
                    messageContainer.getStyleClass().add("system-message");
                }

                VBox body = new VBox();
                body.getStyleClass().add("message-body");
                messageContainer.getChildren().add(body);

                TextFlow messageBody = new TextFlow();
                Text rawBody = new Text(m.body);
                messageBody.maxWidthProperty().bind(messages.widthProperty().subtract(100));
                messageBody.getStyleClass().add("message-body-text");
                messageBody.getChildren().add(rawBody);
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
                            Text attachmentFile = new Text();
                            attachmentFile.getStyleClass().add("message-attachment-file");
                            attachmentFile.setText(attachment.getName());
                            attachmentBox.getChildren().add(attachmentFile);
                        }

                        // Filen er allerede downloaded for at kunne vises, men den her metode viser hvor på computeren den er downloadet
                        Button button = new Button("Download");
                        button.addEventHandler(ActionEvent.ACTION, evt -> Desktop.getDesktop().browseFileDirectory(attachment));
                        attachmentBox.getChildren().add(button);
                    } catch (FileNotFoundException e) {
                        Text errorLabel = new Text();
                        errorLabel.getStyleClass().add("message-error");
                        errorLabel.setText("Komme ikke hente bilag: " + attachment.getName());
                        messageContainer.getChildren().add(errorLabel);
                    }
                }

                HBox reactionsBox = new HBox();
                reactionsBox.getStyleClass().add("message-reactions");
                body.getChildren().add(reactionsBox);

                for (ViewReaction reaction : m.reactions) {
                    HBox reactionBox = new HBox();
                    reactionBox.getStyleClass().add("message-reaction-box");
                    Text reactionLabel = new Text();
                    reactionLabel.getStyleClass().add("message-reaction");
                    reactionLabel.setText(reaction.reaction);
                    reactionBox.getChildren().add(reactionLabel);
                    if (reaction.reactedByUsers.size() > 1) {
                        Text reactionCountLabel = new Text();
                        reactionCountLabel.getStyleClass().add("message-reaction-count");
                        reactionCountLabel.setText(" %d".formatted(reaction.reactedByUsers.size()));
                        reactionBox.getChildren().add(reactionCountLabel);
                    }
                    reactionsBox.getChildren().add(reactionBox);

                    reactionBox.setOnMouseClicked((event) -> {
                        if (reaction.isMyReaction) {
                            getViewModel().removeReaction(m.messageId, reaction.reaction);
                        }
                        else {
                            getViewModel().addReaction(m.messageId, reaction.reaction);
                        }
                    });
                }

                messageContainer.setOnContextMenuRequested(e -> {
                    highlightedMessage = m;
                    // Context menu
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem editMessageItem = new MenuItem("Rediger");
                    MenuItem deleteMessageItem = new MenuItem("Fjern");
                    MenuItem idItem = new MenuItem("Besked-ID: " + highlightedMessage.messageId);
                    idItem.setDisable(true);

                    Menu addReactionsMenu = new Menu("Tilføj reaktion");

                    String[] testReactions = {"😩", "👌", "🤔"};
                    for (String reaction : testReactions) {
                        var reactionItem = new MenuItem(reaction);

                        addReactionsMenu.getItems().add(reactionItem);

                        reactionItem.setOnAction((_) -> {
                            System.out.println(reaction);
                            getViewModel().addReaction(m.messageId, reaction);
                        });
                    }

                    for (ViewReaction reaction : m.reactions) {
                        if (reaction.isMyReaction) {
                            MenuItem removeReactionItem = new MenuItem("Fjern reaktion " + reaction.reaction);
                            removeReactionItem.setOnAction((_) -> {
                                getViewModel().removeReaction(m.messageId, reaction.reaction);
                            });
                            contextMenu.getItems().add(removeReactionItem);
                        }
                    }

                    editMessageItem.setOnAction((_) -> {
                        editing = true;
                        message.textProperty().setValue(highlightedMessage.body);
                    });

                    deleteMessageItem.setOnAction((_) -> getViewModel().deleteMessage(highlightedMessage.messageId));

                    contextMenu.getItems().addAll(addReactionsMenu, editMessageItem, deleteMessageItem, idItem);
                    contextMenu.show(messageContainer, e.getScreenX(), e.getScreenY());
                });

                messages.getChildren().add(messageAlignmentContainer);
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
                removeButton.addEventHandler(ActionEvent.ACTION, evt -> getViewModel().getAttachmentsProperty().remove(attachment));
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
