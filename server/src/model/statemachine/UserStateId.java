package model.statemachine;

public enum UserStateId {
    ADMIN("Admin"),
    MUTED("Muted"),
    REGULAR("Regular");

    private final String stateId;

    private UserStateId(String stateId) {
        this.stateId = stateId;
    }

    public String getStateId() {
        return stateId;
    }

    public static UserStateId fromString(String stateId) {
        for (UserStateId id : UserStateId.values()) {
            if (id.getStateId().equals(stateId)) {
                return id;
            }
        }
        throw new IllegalArgumentException("No state with id " + stateId);
    }
}
