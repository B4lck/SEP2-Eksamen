package model.statemachine;

import model.RoomMember;

/**
 * State over brugerens nuværende rolle i en gruppe
 */
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
     * Returner statens ID som en streng
     */
    public abstract String toStateIdString();

    /**
     * Opretter et UserState, ud fra et UserStateId
     *
     * @param state UserStateId
     * @param user  RoomMember objektet af en profil i et chatrum
     * @return UserState
     */
    public static UserState stateFromId(UserStateId state, RoomMember user) {
        try {
            // Opretter UserState klassen som er angivet i UserStateId'et
            return state.getUserStateClass().getDeclaredConstructor(RoomMember.class).newInstance(user);
        } catch (Exception e) {
            throw new RuntimeException("Kunne ikke oprette UserState", e);
        }
    }
}
