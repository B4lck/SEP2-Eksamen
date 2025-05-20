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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import util.Attachment;
import viewModel.SortingMethod;
import viewModel.ViewMessage;
import viewModel.ViewRoom;
import viewModel.ViewRoomMember;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
    public Text welcomeQuote;
    @FXML
    public VBox welcomeScreen;
    @FXML
    public VBox roomScreen;
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

    public static String[] quotes = new String[]{
            "Simplicity is beautiful.",
            "Slå din AI fra. Slå din hjerne til.",
            "Læring er en rejse, ikke en destination.",
            "En dag uden læring er en dag spildt.",
            "Det er okay at fejle - det er sådan man lærer.",
            "Små skridt hver dag fører til store resultater.",
            "Din indsats i dag former din fremtid i morgen.",
            "Uddannelse er ikke forberedelse til livet; uddannelse er livet selv.",
            "Der er ingen genveje til viden - kun hårdt arbejde.",
            "Nysgerrighed er den bedste læremester."
    };

    // "activity" eller "alphabetically"
    private String roomSortingMethod = "activity";

    private Map<Long, MessageBox> messageNodes = new HashMap<>();

    public ObjectProperty<ViewMessage> editingMessageProperty = new SimpleObjectProperty<>();

    private boolean loadOnScroll = false;

    @Override
    protected void init() {
        // Bindings
        composeField.textProperty().bindBidirectional(getViewModel().getComposeMessageProperty());
        greetingText.textProperty().bind(getViewModel().getGreetingTextProperty());
        searchRoomField.textProperty().bindBidirectional(getViewModel().getSearchFieldProperty());
        searchRoomField.textProperty().addListener(_ -> getViewModel().resetRooms());

        roomName.textProperty().bind(getViewModel().getRoomProperty().map(ViewRoom::getName).orElse("Vælg et rum!"));

        Pane fullHeightPane = new Pane();
        fullHeightPane.minHeightProperty().bind(scrollPane.heightProperty());
        messages.getChildren().add(fullHeightPane);

        welcomeQuote.setText(quotes[new Random().nextInt(quotes.length)]);

        roomScreen.setVisible(false);

        getViewModel().getRoomProperty().addListener((_, _, newRoom) -> {
            applyStyle();
            roomScreen.setVisible(newRoom != null);
            welcomeScreen.setVisible(newRoom == null);
            composeField.requestFocus();
        });

        getViewModel().getCurrentRoomMemberProperty().addListener((_, _, u) -> {
            composeSection.setDisable(u == null || u.getState().equals("Muted"));
            composeField.setText(u == null || u.getState().equals("Muted") ? "Du er muted :(" : "");
        });

        // Rum
        getViewModel().getRoomsProperty().addListener((ListChangeListener<ViewRoom>) change -> {
            rooms.getChildren().clear();

            ViewRoom currentRoom = getViewModel().getRoomProperty().get();

            change.getList().forEach(r -> {
                Button roomButton = new Button(r.getName());
                roomButton.setPrefWidth(200);
                roomButton.getStyleClass().add("room-button");
                if (r.hasNewActivity()) roomButton.getStyleClass().add("new-activity");
                if (currentRoom != null && currentRoom.getRoomId() == r.getRoomId())
                    roomButton.getStyleClass().add("active");
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
                    loadOnScroll = false;
                    previousScrollHeight = 0;
                    scrollPane.setVvalue(1.0);
                    loadOnScroll = true;
                });
            }
        });

        // Lyt efter ændringer i brugerne rummet
        getViewModel().getRoomMembersProperty().addListener((ListChangeListener<ViewRoomMember>) change -> {
            change.next();

            // Brugere som er blevet tilføjet (eller ændret, da de bliver udskiftet med en ny ViewRoomMember
            for (ViewRoomMember user : change.getRemoved()) {
                if (messageNodes.containsKey(user.getLatestReadMessage())) {
                    messageNodes.get(user.getLatestReadMessage()).update();
                }
            }

            // Brugere som er blevet tilføjet (eller ændret, da de bliver udskiftet med en ny ViewRoomMember
            for (ViewRoomMember user : change.getAddedSubList()) {
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

        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            // Tjek om brugeren er tæt på toppen (f.eks. inden for de øverste 5%)
            if (loadOnScroll && newValue.doubleValue() * (messages.getHeight() - scrollPane.getHeight()) < fullHeightPane.getHeight() + 50 ) {
                loadOnScroll = false;
                getViewModel().loadOlderMessages();
            }
        });
    }

    private double previousScrollHeight = 0;

    private void updateScroll() {
        Platform.runLater(() -> {
            double newScrollHeight = Math.max(messages.getHeight() - scrollPane.getHeight(), 0);

            loadOnScroll = false;
            scrollPane.setVvalue(scrollPane.getVvalue() + (newScrollHeight - previousScrollHeight) / newScrollHeight);
            loadOnScroll = true;

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
    public void send(ActionEvent actionEvent) {
        if (editingMessageProperty.get() != null) {
            getViewModel().editMessage(editingMessageProperty.get().messageId);
            editingMessageProperty.set(null);
        } else {
            getViewModel().sendMessage();
        }
        composeField.clear();
        loadOnScroll = false;
        scrollPane.setVvalue(1.0);
        loadOnScroll = true;
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
        getViewHandler().openView(ViewID.ROOM_MEMBERS);
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

    public void applyStyle() {
        ViewRoom room = getViewModel().getRoomProperty().get();

        if (room == null) return;

        messages.setStyle("-fx-background-color: " + room.getColor() + ";" +
                "-fx-color-font-primary: " + colorToHex(colorContrastText(Color.web(room.getColor()))) + ";" +
                "-fx-color-font-secondary: " + colorToHex(colorContrastText(Color.web(room.getColor()))) + "88;" +
                "-fx-font-family: '" + room.getFont() + "';");

        this.getRoot().setStyle("-fx-background-color: " + room.getColor() + ";" +
                "-fx-color-font-primary: " + colorToHex(colorContrastText(Color.web(room.getColor()))) + ";" +
                "-fx-color-font-secondary: " + colorToHex(colorContrastText(Color.web(room.getColor()))) + "88;");
    }

    public String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    public Color colorContrastText(Color color) {
        return (color.getRed() * 0.299 + color.getGreen() * 0.587 + color.getBlue() * 0.114) > 0.72 ? Color.BLACK : Color.WHITE;
    }

    @FXML
    public void logout() {
        getViewModel().logout();
        getViewHandler().openView(ViewID.LOGIN);
    }

    @FXML
    public void quote(ActionEvent actionEvent) {
        getViewModel().setChatRoom(-1);
    }
}
