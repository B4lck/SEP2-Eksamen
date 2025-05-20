package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import viewModel.CreateEditChatRoomViewModel;
import viewModel.ViewUser;

import java.util.List;

public class CreateEditChatRoomViewController extends ViewController<CreateEditChatRoomViewModel> {
    @FXML
    public Text title;
    @FXML
    public ColorPicker colorButton;
    @FXML
    public ChoiceBox <String> fontButton;
    @FXML
    public Button confirmButton;
    @FXML
    public Label colorLabel;
    @FXML
    public Label fontLabel;
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
    private ListView<ViewUser> users;
    @FXML
    private Label errorLabel;

    @Override
    protected void init() {
        nameTextField.textProperty().bindBidirectional(getViewModel().getNameProperty());
        errorLabel.textProperty().bind(getViewModel().getErrorProperty());
        title.textProperty().bind(getViewModel().getTitleProperty());

        fontButton.getItems().addAll("Arial", "Comic Sans MS", "Times New Roman", "Courier New", "Brush Script MT");
        fontButton.setValue("Arial");
        fontButton.getSelectionModel().selectedItemProperty().addListener((_,_,newValue)-> getViewModel().setFont(newValue));

        users.setItems(getViewModel().getMembersProperty());

        users.setCellFactory(cell -> new ListCell<>() {
            @Override
            protected void updateItem(ViewUser item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getUsername());
                }
            }
        });
    }

    @Override
    public void reset() {
        super.reset();
        muteButton.setVisible(getViewModel().isEdit());
        unmuteButton.setVisible(getViewModel().isEdit());
        promoteButton.setVisible(getViewModel().isEdit());
        demoteButton.setVisible(getViewModel().isEdit());
        colorLabel.setVisible(getViewModel().isEdit());
        colorButton.setVisible(getViewModel().isEdit());
        colorButton.setValue(Color.web(getViewModel().getRoomColor()));
        fontLabel.setVisible(getViewModel().isEdit());
        fontButton.setVisible(getViewModel().isEdit());
        fontButton.setValue(getViewModel().getFont());
    }

    @FXML
    public void addUser() {
        getViewHandler().openPopup(PopupViewID.USER_PICKER, (List<Long> userId) -> {
            if (userId != null) {
                for (Long id : userId) {
                    getViewModel().addUser(id);
                }
            }
        });
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
    public void removeUser(ActionEvent actionEvent) {
        getViewModel().removeUser(users.getSelectionModel().getSelectedItems().getFirst().getUserId());
    }

    @FXML
    public void mute(ActionEvent actionEvent) {
        getViewModel().muteUser(users.getSelectionModel().getSelectedItems().getFirst().getUserId());
    }

    @FXML
    public void unmute(ActionEvent actionEvent) {
        getViewModel().unmuteUser(users.getSelectionModel().getSelectedItems().getFirst().getUserId());
    }

    @FXML
    public void promote(ActionEvent actionEvent) {
        getViewModel().promoteUser(users.getSelectionModel().getSelectedItems().getFirst().getUserId());
    }

    @FXML
    public void demote(ActionEvent actionEvent) {
        getViewModel().demoteUser(users.getSelectionModel().getSelectedItems().getFirst().getUserId());
    }

    @FXML
    public void editColor(ActionEvent actionEvent) {
        getViewModel().editColor(String.format(
                "#%02x%02x%02x",
                (int) (255 * colorButton.getValue().getRed()),
                (int) (255 * colorButton.getValue().getGreen()),
                (int) (255 * colorButton.getValue().getBlue())
        ));
    }
}
