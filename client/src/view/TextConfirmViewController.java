package view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import viewModel.TextConfirmViewModel;

public class TextConfirmViewController extends PopupViewController<String, TextConfirmViewModel>{
    @FXML
    private TextField inputField;
    @FXML
    private Label title;

    @Override
    protected void init() {
        title.textProperty().bind(getViewModel().getTitleProperty());
    }

    @FXML
    public void confirm() {
        getStage().close();
        callback(inputField.getText());
    }

    @FXML
    public void cancel() {
        getStage().close();
        callback("");
    }
}
