package view;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.Message;

public class ChatRoomViewController extends ViewController<viewModel.ChatRoomViewModel> {


    @Override
    protected void init() {
        message.textProperty().bindBidirectional(getViewModel().getComposeMessageProperty());

        getViewModel().getMessagesProperty().addListener((ListChangeListener<Message>) change -> {
            beskeder.setText("");
            change.getList().forEach(m -> {
                beskeder.setText(beskeder.getText() + m.getBody() + "\n");
            });
        });
    }

    @Override
    public void reset() {
        super.reset();
        getViewModel().reset();
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
