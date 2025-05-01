package viewModel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import model.Profile;
import util.ServerError;

import java.rmi.ServerException;


public class CreateEditChatRoomViewModel implements ViewModel {
    private StringProperty nameProperty;
    private ObservableList<ViewUser> membersProperty;
    private Model model;
    private ViewState viewState;

    public CreateEditChatRoomViewModel(Model model, ViewState viewState) {
        this.model = model;
        this.nameProperty = new SimpleStringProperty();
        this.membersProperty = FXCollections.observableArrayList();
    }

    public StringProperty getNameProperty() {
        return nameProperty;
    }

    public void addUser(long userId) {
        try {
            model.getChatRoomManager().addUser(viewState.getCurrentChatRoom(), userId);
        } catch (ServerError error) {
            error.printStackTrace();
            error.showAlert();
        }
    }

    public void create() {
        try {
            model.getChatRoomManager().createRoom(nameProperty.getValue());
        } catch (ServerError error) {
            error.printStackTrace();
            error.showAlert();
        }
    }

    @Override
    public void reset() {
        nameProperty.set("");
        membersProperty.clear();

       /* try {
           for (Profile profile)
        } catch (ServerError error) {
            error.printStackTrace();
            error.showAlert();
        }
        */
    }
}
