package viewModel;

import model.Model;

public class ViewModelFactory {

    private ChatRoomViewModel chatRoomViewModel;
    private SignUpViewModel signUpViewModel;
    private LogInViewModel logInViewModel;
    private final Model model;

    public ViewModelFactory(Model model) {
        this.model = model;
    }

    public ChatRoomViewModel getChatRoomViewModel() {
        if (chatRoomViewModel == null) {
            chatRoomViewModel = new ChatRoomViewModel(model);
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

}
