package view;

import javafx.scene.layout.Region;
import viewModel.ViewModel;

public abstract class ViewController<T extends ViewModel> {
    private ViewHandler viewHandler;
    private Region root;
    private T viewModel;

    public void init(ViewHandler viewHandler, T viewModel, Region root) {
        this.viewHandler = viewHandler;
        this.viewModel = viewModel;
        this.root = root;

        this.init();
        viewModel.reset();
    }

    protected abstract void init();

    public void reset() {
        viewModel.reset();
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
