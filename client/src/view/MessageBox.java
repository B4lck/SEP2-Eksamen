package view;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import viewModel.ChatRoomViewModel;
import viewModel.ViewMessage;
import viewModel.ViewReaction;
import viewModel.ViewRoomUser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class MessageBox extends HBox {

    private ViewMessage viewMessage;
    private ChatRoomViewController controller;
    private ChatRoomViewModel viewModel;

    public MessageBox(ViewMessage viewMessage, ChatRoomViewController controller, ChatRoomViewModel viewModel) {
        super();

        this.viewMessage = viewMessage;
        this.controller = controller;
        this.viewModel = viewModel;

        this.update();
    }

    public void update() {
        this.getChildren().clear();

        VBox messageOuterContainer = new VBox();
        this.getChildren().add(messageOuterContainer);

        this.getStyleClass().add("message-alignment-container");
        this.setAlignment(viewMessage.isMyMessage ? Pos.TOP_RIGHT : Pos.TOP_LEFT);

        VBox messageContainer = new VBox();
        messageContainer.getStyleClass().add("message-container");
        messageOuterContainer.getChildren().add(messageContainer);

        if (!viewMessage.isSystemMessage) {
            HBox messageHeader = new HBox();
            messageHeader.getStyleClass().add("message-header");
            messageContainer.getChildren().add(messageHeader);

            Text messageTime = new Text();
            messageTime.getStyleClass().add("message-time");
            messageTime.setText("%02d:%02d ".formatted(viewMessage.dateTime.getHour(), viewMessage.dateTime.getMinute()));
            messageHeader.getChildren().add(messageTime);

            Text messageSender = new Text();
            messageSender.getStyleClass().add("message-sender");
            messageSender.setText(viewMessage.sender + " ");
            messageHeader.getChildren().add(messageSender);
        } else {
            messageContainer.getStyleClass().add("system-message");
        }

        VBox body = new VBox();

        body.getStyleClass().add("message-body");
        messageContainer.getChildren().add(body);

        TextFlow messageBody = new TextFlow();
        Text rawBody = new Text(viewMessage.body);
        messageBody.maxWidthProperty().bind(controller.messages.widthProperty().subtract(100));
        messageBody.getStyleClass().add("message-body-text");
        messageBody.getChildren().add(rawBody);
        body.getChildren().add(messageBody);

        for (File attachment : viewMessage.attachments) {
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

        for (ViewReaction reaction : viewMessage.reactions) {
            HBox reactionBox = new HBox();
            reactionBox.getStyleClass().add("message-reaction-box");
            Text reactionLabel = new Text();
            reactionLabel.getStyleClass().add("message-reaction");
            reactionLabel.setText(reaction.getReaction());
            reactionBox.getChildren().add(reactionLabel);
            if (reaction.getReactedByUsers().size() > 1) {
                Text reactionCountLabel = new Text();
                reactionCountLabel.getStyleClass().add("message-reaction-count");
                reactionCountLabel.setText(" %d".formatted(reaction.getReactedByUsers().size()));
                reactionBox.getChildren().add(reactionCountLabel);
            }
            reactionsBox.getChildren().add(reactionBox);

            reactionBox.setOnMouseClicked((event) -> {
                if (reaction.isMyReaction()) {
                    viewModel.removeReaction(viewMessage.messageId, reaction.getReaction());
                } else {
                    viewModel.addReaction(viewMessage.messageId, reaction.getReaction());
                }
            });
        }

        messageContainer.setOnContextMenuRequested(e -> {
            // Context menu
            ContextMenu contextMenu = new ContextMenu();
            javafx.scene.control.MenuItem editMessageItem = new javafx.scene.control.MenuItem("Rediger");
            javafx.scene.control.MenuItem deleteMessageItem = new javafx.scene.control.MenuItem("Fjern");
            javafx.scene.control.MenuItem idItem = new javafx.scene.control.MenuItem("Besked-ID: " + viewMessage.messageId);
            idItem.setDisable(true);

            javafx.scene.control.Menu addReactionsMenu = new javafx.scene.control.Menu("Tilføj reaktion");

            String[] testReactions = {"👍", "😂", "😮", "😢", "😡", "❤️", "🙌", "🤔", "💀"};
            for (String reaction : testReactions) {
                var reactionItem = new javafx.scene.control.MenuItem(reaction);

                addReactionsMenu.getItems().add(reactionItem);

                reactionItem.setOnAction((_) -> {
                    viewModel.addReaction(viewMessage.messageId, reaction);
                });
            }

            for (ViewReaction reaction : viewMessage.reactions) {
                if (reaction.isMyReaction()) {
                    javafx.scene.control.MenuItem removeReactionItem = new MenuItem("Fjern reaktion " + reaction.getReaction());
                    removeReactionItem.setOnAction((_) -> {
                        viewModel.removeReaction(viewMessage.messageId, reaction.getReaction());
                    });
                    contextMenu.getItems().add(removeReactionItem);
                }
            }

            editMessageItem.setOnAction((_) -> {
                controller.editingMessageProperty.set(viewMessage);
            });

            deleteMessageItem.setOnAction((_) -> viewModel.deleteMessage(viewMessage.messageId));

            contextMenu.getItems().addAll(addReactionsMenu, editMessageItem, deleteMessageItem, idItem);
            contextMenu.show(messageContainer, e.getScreenX(), e.getScreenY());
        });

        List<ViewRoomUser> readByUsers = viewModel.getRoomUsersProperty().stream()
                .filter(u -> u.getLatestReadMessage() == viewMessage.messageId).toList();

        if (!readByUsers.isEmpty()) {
            HBox alignmentContainer = new HBox();
            alignmentContainer.getStyleClass().add("message-alignment-container");
            alignmentContainer.setAlignment(viewMessage.isMyMessage ? Pos.TOP_RIGHT : Pos.TOP_LEFT);

            Text readByLabel = new Text();
            if (readByUsers.size() == 1) {
                readByLabel.setText("Læst af " + readByUsers.getFirst().getDisplayName());
            } else {
                readByLabel.setText(
                        "Læst af "
                                + String.join(", ", readByUsers.stream().map(ViewRoomUser::getDisplayName).limit(readByUsers.size() - 1).toList())
                                + " og "
                                + readByUsers.getLast().getDisplayName()
                );
            }
            readByLabel.getStyleClass().add("message-read-by");
            alignmentContainer.getChildren().add(readByLabel);

            messageOuterContainer.getChildren().add(alignmentContainer);
        }
    }

    public ViewMessage getViewMessage() {
        return viewMessage;
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
