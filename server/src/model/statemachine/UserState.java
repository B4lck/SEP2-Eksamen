package model.statemachine;

import model.RoomUser;

public abstract class UserState {
    /**
     * Promoter user staten
     */
    public abstract void promote();

    /**
     * Demoter user staten
     */
    public abstract void demote();

    /**
     * Muter user staten
     */
    public abstract void mute();

    /**
     * Unmuter user staten
     */
    public abstract void unmute();

    /**
     * Opretter et user state, ud fra et user state id
     * @param state User state id
     * @param user RoomUser objektet af en profil i et chatrum
     * @return User state objekt
     */
    public static UserState stateFromString(UserStateId state, RoomUser user) {
        return switch (state.getStateId()) {
            case "regular" -> new RegularState(user);
            case "muted" -> new MutedUser(user);
            case "admin" -> new AdministratorState(user);
            default -> throw new IllegalStateException("State does not exist");
        };
    }
}
