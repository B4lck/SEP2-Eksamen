package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import viewModel.CreateEditChatRoomViewModel;
import viewModel.ViewRoomMember;

import java.util.List;

public class CreateEditChatRoomViewController extends ViewController<CreateEditChatRoomViewModel> {
    @FXML
    public Text title;
    @FXML
    public ColorPicker colorButton;
    @FXML
    public ChoiceBox<String> fontButton;
    @FXML
    public Button confirmButton;
    @FXML
    public Label colorLabel;
    @FXML
    public Label fontLabel;
    @FXML
    public Button removeMemberButton;
    @FXML
    private Button muteButton;
    @FXML
    private Button unmuteButton;
    @FXML
    private Button promoteButton;
    @FXML
    private Button demoteButton;
    @FXML
    private TextField nameTextField;
    @FXML
    private ListView<ViewRoomMember> users;
    @FXML
    private Label errorLabel;

    @Override
    protected void init() {
        nameTextField.textProperty().bindBidirectional(getViewModel().getNameProperty());
        errorLabel.textProperty().bind(getViewModel().getErrorProperty());
        title.textProperty().bind(getViewModel().getTitleProperty());

        fontButton.getItems().addAll("Arial", "Comic Sans MS", "Times New Roman", "Courier New", "Brush Script MT");
        fontButton.setValue("Arial");
        fontButton.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            getViewModel().setFont(newValue);
        });

        users.setItems(getViewModel().getMembersProperty());

        users.setCellFactory(cell -> new ListCell<>() {
            @Override
            protected void updateItem(ViewRoomMember member, boolean empty) {
                super.updateItem(member, empty);
                if (empty || member == null) {
                    setText(null);
                } else {
                    String userText = member.getDisplayName() + " (" + switch (member.getState()) {
                        case "Creator" -> "Gruppeopretter";
                        case "Admin" -> "Gruppeadministrator";
                        case "Regular" -> "Medlem";
                        case "Muted" -> "Muted";
                        default -> "Ukendt rolle";
                    } + ")";
                    if (member.getNewState() != null) userText += " vil blive " + switch (member.getNewState()) {
                        case "promote" -> "forfremmet";
                        case "demote" -> "degraderet";
                        case "mute" -> "muted";
                        case "unmute" -> "unmuted";
                        default -> "(ukendt)";
                    };
                    setText(userText);
                }
            }
        });

        users.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            demoteButton.setDisable(newValue == null || !newValue.getState().equals("Admin") && !"promote".equals(newValue.getNewState()));
            promoteButton.setDisable(newValue == null || !newValue.getState().equals("Regular") && !"demote".equals(newValue.getNewState()));
            muteButton.setDisable(newValue == null || !newValue.getState().equals("Regular") && !"unmute".equals(newValue.getNewState()));
            unmuteButton.setDisable(newValue == null || !newValue.getState().equals("Muted") && !"mute".equals(newValue.getNewState()));
            removeMemberButton.setDisable(newValue == null);
        });
    }

    @Override
    public void reset() {
        super.reset();
        demoteButton.setDisable(true);
        promoteButton.setDisable(true);
        muteButton.setDisable(true);
        unmuteButton.setDisable(true);
        removeMemberButton.setDisable(true);
        colorButton.setValue(Color.web(getViewModel().getRoomColor()));
        fontButton.setValue(getViewModel().getFont());
        confirmButton.setText(getViewModel().isEdit() ? "Bekræft" : "Opret");
    }

    @FXML
    public void confirm() {
        if (getViewModel().confirm()) {
            getViewHandler().openView(ViewID.CHATROOM);
        }
    }

    @FXML
    public void cancel() {
        getViewHandler().openView(ViewID.CHATROOM);
    }

    @FXML
    public void addMember() {
        getViewHandler().openPopup(PopupViewID.USER_PICKER, (List<Long> userId) -> {
            if (userId != null) {
                for (Long id : userId) {
                    getViewModel().addMember(id, "Regular");
                }
            }
        });
    }

    @FXML
    public void removeMember(ActionEvent actionEvent) {
        getViewModel().removeMember(users.getSelectionModel().getSelectedItems().getFirst().getUserId());
    }

    @FXML
    public void mute(ActionEvent actionEvent) {
        getViewModel().muteMember(users.getSelectionModel().getSelectedItems().getFirst().getUserId());
    }

    @FXML
    public void unmute(ActionEvent actionEvent) {
        getViewModel().unmuteMember(users.getSelectionModel().getSelectedItems().getFirst().getUserId());
    }

    @FXML
    public void promote(ActionEvent actionEvent) {
        getViewModel().promoteMember(users.getSelectionModel().getSelectedItems().getFirst().getUserId());
    }

    @FXML
    public void demote(ActionEvent actionEvent) {
        getViewModel().demoteMember(users.getSelectionModel().getSelectedItems().getFirst().getUserId());
    }

    @FXML
    public void editColor(ActionEvent actionEvent) {
        getViewModel().setColor(String.format(
                "#%02x%02x%02x",
                (int) (255 * colorButton.getValue().getRed()),
                (int) (255 * colorButton.getValue().getGreen()),
                (int) (255 * colorButton.getValue().getBlue())
        ));
    }
}
