package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import util.Callback;
import viewModel.ViewUser;

public class UserPicker extends PopupViewController<String> {
    @Override
    protected void init() {
        System.out.println("Hej");
    }

    @FXML
    public void test(ActionEvent evt) {
        callback("Skibidi");
    }
}
