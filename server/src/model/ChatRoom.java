package model;

import java.util.Map;

public interface ChatRoom {
    /**
     * Henter navnet på chatrummet
     *
     * @return Navnet
     */
    String getName();

    /**
     * Henter id'et på chatrummet
     *
     * @return id'et
     */
    long getRoomId();

    /**
     * Henter alle brugere i chatrummet
     *
     * @return id'et på alle brugere
     */
    long[] getUsers();

    /**
     * Tilføjer ny bruger til chatrummet
     *
     * @param userToAdd   Id'et på brugeren der skal tilføjes
     * @param addedByUser Id'et på brugeren der forsøger at tilføje en ny bruger
     */
    void addUser(long userToAdd, long addedByUser);

    /**
     * Laver objektet om til et map, så det kan sendes med JSON til clienten uden fejl
     *
     * @return Map af alle instans variabler
     */
    Map<String, Object> getData();

    /**
     * Sætter navnet på chatrummet
     *
     * @param name navnet på chatrummet
     */
    void setName(String name);

    /**
     * Tjekker om en bruger er i chatrummet
     *
     * @param user Id'et på brugeren
     * @return true hvis brugeren deltager i chatrummet
     */
    boolean isInRoom(long user);

    void removeUser(long user, long adminUser);
}
