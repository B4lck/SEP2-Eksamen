package view;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import util.Callback;
import viewModel.ViewModel;
import viewModel.ViewModelFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ViewHandler {
    private Stage primaryStage;
    private Scene currentScene;
    private NotificationManager notificationManager = NotificationManager.getNotificationManager();

    public static ReadOnlyBooleanProperty focusedProperty = null;

    private ViewModelFactory viewModelFactory;

    private Map<String, ViewController> controllers = new HashMap<>();

    public ViewHandler(ViewModelFactory viewModelFactory) {
        this.viewModelFactory = viewModelFactory;
        currentScene = new Scene(new Region());
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        focusedProperty = primaryStage.focusedProperty();
        openView(ViewID.LOGIN);
    }

    public void openView(ViewID view) {
        Region root;

        try {
            root = switch (view) {
                case LOGIN -> getRoot(view, viewModelFactory.getLogInViewModel(), this);
                case SIGNUP -> getRoot(view, viewModelFactory.getSignUpViewModel(), this);
                case CHATROOM -> getRoot(view, viewModelFactory.getChatRoomViewModel(), this);
                case CREATE_EDIT_ROOM -> getRoot(view, viewModelFactory.getCreateEditChatRoomViewModel(), this);
                case ROOM_MEMBERS -> getRoot(view, viewModelFactory.getRoomMembersViewModel(), this);
            };
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke indlæse siden :(");
        }

        if (root == null) {
            throw new IllegalStateException("Bro siden findes ik din noob");
        }

        currentScene.setRoot(root);
        String title = "";
        if (root.getUserData() != null) {
            title += root.getUserData();
        }
        primaryStage.setTitle(title);
        primaryStage.setScene(currentScene);
        primaryStage.setWidth(root.getPrefWidth());
        primaryStage.setHeight(root.getPrefHeight());
        primaryStage.show();
    }

    public <R> void openPopup(PopupViewID view, Callback<R> callback) {
        ViewModel viewModel = switch (view) {
            case USER_PICKER -> viewModelFactory.newUserPickerViewModel();
            case TEXT_CONFIRM -> viewModelFactory.newTextConfirmViewModel();
        };

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(view.getFilename()));
            Region root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, root.getPrefWidth(), root.getPrefHeight()));
            ((PopupViewController<R, ViewModel>) loader.getController()).init(this, viewModel, root, stage, callback);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private <T extends ViewModel> Region getRoot(ViewID viewId, T viewModel, ViewHandler viewHandler) throws IOException {
        ViewController<T> vc = controllers.get(viewId.name());

        if (vc == null) {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(viewId.getFilename()));
            Region root = loader.load();
            vc = loader.getController();
            vc.init(this, viewModel, root);
            controllers.put(viewId.name(), vc);
        }

        vc.reset();

        return vc.getRoot();
    }
}
