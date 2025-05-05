package view;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Message;
import model.Model;
import util.Callback;
import util.ServerError;
import viewModel.ViewModel;
import viewModel.ViewModelFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ViewHandler {
    private Stage primaryStage;
    private Scene currentScene;
    private NotificationManager notificationManager = NotificationManager.getNotificationManager();

    private ViewModelFactory viewModelFactory;

    private Map<String, ViewController> controllers = new HashMap<>();

    public ViewHandler(ViewModelFactory viewModelFactory, Model model) {
        this.viewModelFactory = viewModelFactory;
        currentScene = new Scene(new Region());

        // Notifikationer
        model.getMessagesManager().addListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // Hvis primary stage er i fokus, behøves notifikationer ikke
                if (primaryStage != null && primaryStage.isFocused()) return;
                // Lyt efter nye beskeder
                if (evt.getPropertyName().equals("NEW_MESSAGE")) {
                    Message message = (Message) evt.getNewValue();
                    // Send ikke system beskeder, eller egne beskeder som notifikationer
                    if (message.getSentBy() == 0) return;
                    if (message.getSentBy() == model.getProfileManager().getCurrentUserUUID()) return;
                    // Send notifikation
                    Platform.runLater(() -> {
                        try {
                            notificationManager.showNotification(model.getProfileManager().getProfile(message.getSentBy()).getUsername() + " har sendt dig en besked", message.getBody());
                        } catch (ServerError e) {
                            e.printStackTrace();
                            e.showAlert();
                        }
                    });
                }
            }
        });
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        openView(ViewID.LOGIN);
    }

    public void openView(ViewID view) {
        Region root = null;

        root = switch (view) {
            case LOGIN -> getRoot(view, viewModelFactory.getLogInViewModel(), this);
            case SIGNUP -> getRoot(view, viewModelFactory.getSignUpViewModel(), this);
            case CHATROOM -> getRoot(view, viewModelFactory.getChatRoomViewModel(), this);
            case CREATE_EDIT_ROOM -> getRoot(view, viewModelFactory.getCreateEditChatRoomViewModel(), this);
        };

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
        };

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(view.getFilename()));
            Region root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, root.getPrefWidth(), root.getPrefHeight()));
            ((PopupViewController) loader.getController()).init(this, viewModel, root, stage, callback);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private <T extends ViewModel> Region getRoot(ViewID viewId, T viewModel, ViewHandler viewHandler) {
        ViewController<T> vc = controllers.get(viewId.name());

        if (vc == null) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource(viewId.getFilename()));
                Region root = loader.load();
                vc = loader.getController();
                vc.init(this, viewModel, root);
                controllers.put(viewId.name(), vc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        vc.reset();

        return vc.getRoot();
    }
}
