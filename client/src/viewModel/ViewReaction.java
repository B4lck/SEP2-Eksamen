package viewModel;

import java.util.ArrayList;
import java.util.List;

public class ViewReaction {
    private String reaction;
    private List<Long> reactedByMembers;
    private boolean isMyReaction;

    public ViewReaction(String reaction, long reactedBy, boolean isMyReaction) {
        this.reaction = reaction;
        this.isMyReaction = isMyReaction;
        this.reactedByMembers = new ArrayList<>(List.of(reactedBy));
    }

    public String getReaction() {
        return reaction;
    }

    public List<Long> getReactedByMembers() {
        return List.copyOf(reactedByMembers);
    }

    public boolean isMyReaction() {
        return isMyReaction;
    }

    public void addReactedBy(long userId, boolean isMyReaction) {
        reactedByMembers.add(userId);
        this.isMyReaction = this.isMyReaction || isMyReaction;
    }
}
