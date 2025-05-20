package model.statemachine;

import model.RoomMember;

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
     * Henter staten som String
     * @return staten som String
     */
    public abstract String getStateAsString();

    /**
     * Opretter et user state, ud fra et user state id
     *
     * @param state User state id
     * @param user  RoomMember objektet af en profil i et chatrum
     * @return User state objekt
     */
    public static UserState stateFromString(UserStateId state, RoomMember user) {
        return switch (state.getStateId()) {
            case "Regular" -> new RegularState(user);
            case "Muted" -> new MutedUser(user);
            case "Admin" -> new AdministratorState(user);
            default -> throw new IllegalStateException("State does not exist");
        };
    }
}
