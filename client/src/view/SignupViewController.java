package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import viewModel.SignUpViewModel;

public class SignupViewController extends ViewController<SignUpViewModel> {
    @Override
    protected void init() {
        passwordField.textProperty().bindBidirectional(getViewModel().getPasswordInputProperty());
        passwordCheck.textProperty().bindBidirectional(getViewModel().getVerifyPasswordInputProperty());
        viaId.textProperty().bindBidirectional(getViewModel().getUserNameInputProperty());
        error.textProperty().bind(getViewModel().getErrorProperty());
    }

    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField passwordCheck;
    @FXML
    private TextField viaId;
    @FXML
    private Label error;

    @FXML
    public void signUp(ActionEvent evt) {
        if (getViewModel().signUp()) {
            getViewHandler().openView(ViewID.CHATROOM);
        }
    }

    @FXML
    public void logIn(ActionEvent event) {
        getViewHandler().openView(ViewID.LOGIN);
    }
}
