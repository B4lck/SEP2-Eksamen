package view;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import viewModel.RoomMembersViewModel;
import viewModel.ViewRoomMember;

public class RoomMembersViewController extends ViewController<RoomMembersViewModel> {
    @FXML
    public Button blockButton;
    @FXML
    private Label errorLabel;
    @FXML
    private Text title;
    @FXML
    private ListView<ViewRoomMember> users;

    @Override
    protected void init() {
        errorLabel.textProperty().bind(getViewModel().getErrorProperty());
        title.textProperty().bind(getViewModel().getTitleProperty());
        users.setItems(getViewModel().getMembersProperty());

        users.setCellFactory(cell -> new RoomMemberCell());

        users.getSelectionModel().getSelectedItems().addListener((ListChangeListener<ViewRoomMember>) user -> {
            user.next();

            blockButton.setText(getViewModel().isBlocked(user.getList().getFirst().getUserId()) ? "Unblock" : "Block");
        });
    }

    @FXML
    public void editName() {
        getViewHandler().openPopup(PopupViewID.TEXT_CONFIRM, (String text) -> {
            getViewModel().editNickname(users.getSelectionModel().getSelectedItems().getFirst().getUserId(), text);
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
            getViewModel().removeNickname(users.getSelectionModel().getSelectedItems().getFirst().getUserId());
            getViewModel().reset();
        }
    }

    @FXML
    public void confirm() {
        getViewHandler().openView(ViewID.CHATROOM);
    }

    public void blockUser(ActionEvent actionEvent) {
        long selected = users.getSelectionModel().getSelectedItems().getFirst().getUserId();
        if (getViewModel().isBlocked(selected)) getViewModel().unblock(selected);
        else getViewModel().block(selected);
        getViewModel().reset();
    }
}
