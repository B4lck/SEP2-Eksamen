package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import viewModel.LogInViewModel;

public class LoginViewController extends ViewController<LogInViewModel> {

    @Override
    protected void init() {
        username.textProperty().bindBidirectional(getViewModel().getUserNameInputProperty());
        password.textProperty().bindBidirectional(getViewModel().getPasswordInputProperty());
        error.textProperty().bind(getViewModel().getErrorProperty());
    }

    @FXML
    private TextField username;
    @FXML
    private Label error;
    @FXML
    private PasswordField password;

    @FXML
    public void login(ActionEvent evt) {
        getViewModel().login();
        getViewHandler().openView(ViewID.CHATROOM);
    }
    @FXML
    public void signUp(ActionEvent actionEvent) {
        //sendes til siden for opret profil
        getViewHandler().openView(ViewID.SIGNUP);
    }
}
