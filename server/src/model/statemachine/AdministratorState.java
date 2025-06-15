package model.statemachine;

import model.RoomMember;

public class AdministratorState extends UserState {
    private RoomMember user;

    public AdministratorState(RoomMember user) {
        this.user = user;
    }

    @Override
    public void promote() {
        throw new IllegalStateException("User is already admin, therefor cannot promoted further");
    }

    @Override
    public void demote() {
        user.setState(new RegularState(user));
    }

    @Override
    public void mute() {
        throw new IllegalStateException("Admin cannot be muted");
    }

    @Override
    public void unmute() {
        throw new IllegalStateException("Admin is not muted");
    }

    @Override
    public String toStateIdString() {
        return UserStateId.ADMIN.toString();
    }
}
