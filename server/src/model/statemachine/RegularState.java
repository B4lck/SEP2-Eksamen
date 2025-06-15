package model.statemachine;

import model.RoomMember;

public class RegularState extends UserState {
    private RoomMember user;

    public RegularState(RoomMember user) {
        this.user = user;
    }

    @Override
    public void promote() {
        user.setState(new AdministratorState(user));
    }

    @Override
    public void demote() {
        throw new IllegalStateException("User is not admin, therefor cannot demote");
    }

    @Override
    public void mute() {
        user.setState(new MutedState(user));
    }

    @Override
    public void unmute() {
        throw new IllegalStateException("User is not muted, therefor cannot be unmuted");
    }

    @Override
    public String toStateIdString() {
        return UserStateId.REGULAR.toString();
    }
}
