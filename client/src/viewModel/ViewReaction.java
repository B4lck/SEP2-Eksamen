package viewModel;

import java.util.ArrayList;
import java.util.List;

public class ViewReaction {
    public String reaction;
    public List<ViewUser> reactedByUsers;
    public boolean isMyReaction;

    public ViewReaction(String reaction, ViewUser reactedByUsers, boolean isMyReaction) {
        this.reaction = reaction;
        this.isMyReaction = isMyReaction;
        this.reactedByUsers = new ArrayList<>(List.of(reactedByUsers));
    }
}
