package viewModel;

import model.Model;

public class ViewModelFactory {

    private ChatRoomViewModel chatRoomViewModel;
    private SignUpViewModel signUpViewModel;
    private LogInViewModel logInViewModel;
    private CreateEditChatRoomViewModel createEditChatRoomViewModel;
    private EditNicknameViewModel editNicknameViewModel;

    private ViewState viewState = new ViewState();

    private final Model model;

    public ViewModelFactory(Model model) {
        this.model = model;
    }

    public ChatRoomViewModel getChatRoomViewModel() {
        if (chatRoomViewModel == null) {
            chatRoomViewModel = new ChatRoomViewModel(model, viewState);
        }
        return chatRoomViewModel;
    }

    public SignUpViewModel getSignUpViewModel() {
        if (signUpViewModel == null) {
            signUpViewModel = new SignUpViewModel(model);
        }
        return signUpViewModel;
    }

    public LogInViewModel getLogInViewModel() {
        if (logInViewModel == null) {
            logInViewModel = new LogInViewModel(model);
        }
        return logInViewModel;
    }

    public ViewModel newUserPickerViewModel() {
        return new UserPickerViewModel(model);
    }

    public ViewModel newTextConfirmViewModel() {
        return new TextConfirmViewModel(model, viewState);
    }

    public ViewModel getCreateEditChatRoomViewModel() {
        if (createEditChatRoomViewModel == null) {
            createEditChatRoomViewModel = new CreateEditChatRoomViewModel(model, viewState);
        }
        return createEditChatRoomViewModel;
    }

    public ViewModel getEditNicknameViewModel() {
        if (editNicknameViewModel == null) {
            editNicknameViewModel = new EditNicknameViewModel(model, viewState);
        }
        return editNicknameViewModel;
    }
}
