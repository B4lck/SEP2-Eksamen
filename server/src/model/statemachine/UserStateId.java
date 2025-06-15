package model.statemachine;

/**
 * Enum over alle user states
 */
public enum UserStateId {
    ADMIN("Admin", AdministratorState.class),
    MUTED("Muted", MutedState.class),
    REGULAR("Regular", RegularState.class);

    private final String stateId;
    private final Class<? extends UserState> userStateClass;

    private UserStateId(String stateId, Class<? extends UserState> userState) {
        this.stateId = stateId;
        this.userStateClass = userState;
    }

    public String toString() {
        return stateId;
    }

    public Class<? extends UserState> getUserStateClass() {
        return userStateClass;
    }

    public static UserStateId fromString(String stateId) {
        for (UserStateId id : UserStateId.values()) {
            if (id.toString().equals(stateId)) {
                return id;
            }
        }
        throw new IllegalArgumentException("No state with id " + stateId);
    }
}
