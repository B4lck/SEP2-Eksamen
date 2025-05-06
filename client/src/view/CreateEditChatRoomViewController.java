package view;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import viewModel.CreateEditChatRoomViewModel;
import viewModel.ViewUser;

public class CreateEditChatRoomViewController extends ViewController<CreateEditChatRoomViewModel> {
    @FXML
    public Text title;
    @FXML
    private Button muteButton;
    @FXML
    private Button unmuteButton;
    @FXML
    private TextField nameTextField;
    @FXML
    private ListView<ViewUser> users;
    @FXML
    private Label errorLabel;

    @Override
    protected void init() {
        nameTextField.textProperty().bindBidirectional(getViewModel().getNameField());
        errorLabel.textProperty().bind(getViewModel().getErrorTextProperty());
        title.textProperty().bind(getViewModel().getTitleTextProperty());

        users.setItems(getViewModel().getProfiles());

        users.setCellFactory(cell -> new ListCell<>() {
            @Override
            protected void updateItem(ViewUser item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.username);
                }
            }
        });
    }

    @FXML
    public void addUser() {
        getViewHandler().openPopup(PopupViewID.USER_PICKER, (Long userId) -> {
            if (userId != null)
                getViewModel().addUser(userId);
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
        getViewModel().removeUser(users.getSelectionModel().getSelectedItems().getFirst().userId);
    }
    @FXML
    public void mute(ActionEvent actionEvent) {
    getViewModel().muteUser(users.getSelectionModel().getSelectedItems().getFirst().userId);
    }
    @FXML
    public void unmute(ActionEvent actionEvent) {
    getViewModel().unmuteUser(users.getSelectionModel().getSelectedItems().getFirst().userId);
    }
}
