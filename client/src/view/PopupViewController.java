package view;

import javafx.scene.layout.Region;
import javafx.stage.Stage;
import util.Callback;
import viewModel.ViewModel;
import viewModel.ViewModelFactory;

public abstract class PopupViewController<R, T extends ViewModel> {

    private T viewModel;
    private ViewHandler viewHandler;
    private Stage stage;
    private Callback<R> callback;
    private Region root;

    public void init(ViewHandler viewHandler, T viewModel, Region root, Stage stage, Callback<R> callback) {
        this.viewModel = viewModel;
        this.viewHandler = viewHandler;
        this.stage = stage;
        this.callback = callback;
        this.root = root;
        init();
    }

    protected void callback(R result) {
        if (this.callback == null) throw new IllegalStateException("Callback has already been called!");
        this.callback.callback(result);
        this.callback = null;
    }

    protected abstract void init();

    public Stage getStage() {
        return stage;
    }

    public Region getRoot() {
        return root;
    }

    public ViewHandler getViewHandler() {
        return viewHandler;
    }

    public T getViewModel() {
        return viewModel;
    }

}
