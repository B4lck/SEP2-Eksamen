package model;

import model.statemachine.UserState;
import model.statemachine.UserStateId;
import utils.DataMap;

public class RoomMember {
    /**
     * Id'et på profilen som RoomMember skal tilknyttes
     */
    private final long userId;

    /**
     * Staten på objektet
     */
    private UserState state;
    private long latestReadMessage;
    private String nickname;

    /**
     * @param userId Id'et på profilen som RoomMember skal tilknyttes
     * @param state  Staten på profilen
     */
    public RoomMember(long userId, UserStateId state, long latestReadMessage, String nickname) {
        this.userId = userId;
        this.state = UserState.stateFromString(state, this);
        this.latestReadMessage = latestReadMessage;
        this.nickname = nickname;
    }

    /**
     * Henter brugerens id
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Opdaterer brugerens state i rummet
     * Denne metode opdater ikke persistering
     *
     * @param state Ny state
     */
    public void setState(UserState state) {
        this.state = state;
    }

    /**
     * Henter brugerens nuværende state i rummet
     */
    public UserState getState() {
        return state;
    }

    /**
     * Henter brugerens kaldenavn i rummet
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Henter id'et på brugerens seneste læste besked i rummet
     */
    public long getLatestReadMessage() {
        return latestReadMessage;
    }

    /**
     * Opretter et DataMap af brugeren, som kan sendes til klienten
     */
    public DataMap getData() {
        return new DataMap()
                .with("userId", userId)
                .with("state", state.getStateAsString())
                .with("latestReadMessage", latestReadMessage)
                .with("nickname", nickname);
    }

    /**
     * Opdaterer brugerens kaldenavn i gruppen
     * Denne metode opdater ikke persistering
     *
     * @param nickname Brugerens nye kaldenavn, eller null hvis intet/slettet
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Opdaterer brugerens seneste læste besked i gruppen
     * Denne metode opdater ikke persistering
     *
     * @param messageId ID'et på den seneste læste besked
     */
    public void setLatestReadMessage(long messageId) {
        this.latestReadMessage = messageId;
    }
}
