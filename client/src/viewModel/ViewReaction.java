package viewModel;

public class ViewReaction {
    public String reaction;
    public ViewUser reactedUser;
    public boolean isMyReaction;

    public ViewReaction(String reaction, ViewUser reactedUser, boolean isMyReaction) {
        this.reaction = reaction;
        this.isMyReaction = isMyReaction;
        this.reactedUser = reactedUser;
    }
}
