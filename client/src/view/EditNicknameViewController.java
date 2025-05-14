package view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import viewModel.EditNicknameViewModel;
import viewModel.ViewUser;

public class EditNicknameViewController extends ViewController<EditNicknameViewModel>{
    @FXML
    private Label errorLabel;
    @FXML
    private Text title;
    @FXML
    private ListView<ViewUser> users;

    @Override
    protected void init() {
        errorLabel.textProperty().bind(getViewModel().getErrorText());
        title.textProperty().bind(getViewModel().getTitleText());
        users.setItems(getViewModel().getUsersProperty());

        users.setCellFactory(cell -> new ListCell<>() {
            @Override
            protected void updateItem(ViewUser item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String nickname = item.nickname;
                    setText(item.username + ": <" + (nickname == null? "intet kaldenavn" : nickname) + ">");
                }
            }
        });
    }

    @FXML
    public void editName() {
        getViewHandler().openPopup(PopupViewID.TEXT_CONFIRM, (String text) -> {
            getViewModel().editNickname(users.getSelectionModel().getSelectedItems().getFirst().userId, text);
            getViewModel().reset();
        });
    }

    @FXML
    public void removeName() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Er du sikker?");
        confirmAlert.setHeaderText("Er du sikker?");
        confirmAlert.setContentText("Er du sikker på at du vil fjerne kaldenavnet?");

        if (confirmAlert.showAndWait().orElseThrow() == ButtonType.OK) {
            getViewModel().removeNickname(users.getSelectionModel().getSelectedItems().getFirst().userId);
            getViewModel().reset();
        }
    }

    @FXML
    public void confirm() {
        getViewHandler().openView(ViewID.CHATROOM);
    }
}
