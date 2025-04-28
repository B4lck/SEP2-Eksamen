package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.Message;

public class ChatRoomViewController extends ViewController<viewModel.ChatRoomViewModel> {


    @Override
    protected void init() {
        message.textProperty().bindBidirectional(getViewModel().getComposeMessageProperty());
        getViewModel().getMessagesProperty().addListener((observable, oldValue, newValue) -> {
            beskeder.clear();
            for (Message m : newValue) {
                beskeder.textProperty().setValue(beskeder.textProperty().getValue() + m.getBody() + "\n");
            }
        });
    }

    @FXML
    private TextField message;
    @FXML
    public TextArea beskeder;

    public void logud(ActionEvent actionEvent) {
        getViewHandler().openView(ViewID.LOGIN);
    }

    public void send(ActionEvent actionEvent) {
        getViewModel().sendMessage();
        message.clear();
    }
}
