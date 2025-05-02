package view;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import viewModel.CreateEditChatRoomViewModel;
import viewModel.ViewUser;

public class CreateEditChatRoomViewController extends ViewController<CreateEditChatRoomViewModel> {
    @FXML
    public Text title;
    @FXML
    private TextField nameTextField;
    @FXML
    private VBox users;
    @FXML
    private Label errorLabel;

    @Override
    protected void init() {
        nameTextField.textProperty().bindBidirectional(getViewModel().getNameField());
        errorLabel.textProperty().bind(getViewModel().getErrorTextProperty());
        title.textProperty().bind(getViewModel().getTitleTextProperty());

        getViewModel().getProfiles().addListener((ListChangeListener<ViewUser>) change -> {
            users.getChildren().clear();

            change.getList().forEach(user -> {
                Label username = new Label();
                username.setText(user.username);
                username.getStyleClass().add("username");
                users.getChildren().add(username);
            });
        });
    }

    public void addUser() {
        getViewHandler().openPopup(PopupViewID.USER_PICKER, (Long userId) -> {
            if (userId != null)
                getViewModel().addUser(userId);
        });
    }

    public void confirm() {
        if (getViewModel().confirm()) {
            getViewHandler().openView(ViewID.CHATROOM);
        }
    }

    public void cancel() {
        getViewHandler().openView(ViewID.CHATROOM);
    }
}
