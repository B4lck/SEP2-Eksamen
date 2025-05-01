package view;

import javafx.scene.layout.Region;
import javafx.stage.Stage;
import util.Callback;
import viewModel.ViewModelFactory;

public abstract class PopupViewController<R> {

    protected ViewModelFactory factory;
    protected ViewHandler viewHandler;
    protected Stage stage;
    private Callback<R> callback;
    protected Region root;

    public void init(ViewHandler viewHandler, ViewModelFactory factory, Region root, Stage stage, Callback<R> callback) {
        this.factory = factory;
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

}
