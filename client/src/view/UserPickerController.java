package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import viewModel.UserPickerViewModel;
import viewModel.ViewUser;

import java.util.List;

public class UserPickerController extends PopupViewController<List<Long>, UserPickerViewModel> {
    @FXML
    public TextField search;
    @FXML
    public ListView<ViewUser> list;

    @Override
    protected void init() {
        getViewModel().reset();

        search.textProperty().bindBidirectional(getViewModel().getSearchProperty());
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        list.setItems(getViewModel().getResultsProperty());
        list.setCellFactory(cell -> new ListCell<>() {
            @Override
            protected void updateItem(ViewUser item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getUsername());
                }
            }
        });

        update();
    }

    @FXML
    public void update() {
        getViewModel().search();
    }

    @FXML
    public void cancel(ActionEvent actionEvent) {
        getStage().close();
    }

    @FXML
    public void submit(ActionEvent actionEvent) {
        getStage().close();
        callback(list.getSelectionModel().getSelectedItems().stream().map(ViewUser::getUserId).toList());
    }
}
