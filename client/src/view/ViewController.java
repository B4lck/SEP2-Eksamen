package view;

import javafx.scene.layout.Region;

public abstract class ViewController<T> {
    private ViewHandler viewHandler;
    private Region root;
    private T viewModel;

    public void init(ViewHandler viewHandler, T viewModel, Region root) {
        this.viewHandler = viewHandler;
        this.viewModel = viewModel;
        this.root = root;

        this.init();
    }

    protected abstract void init();

    public void reset() {
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
