package model;

import utils.DataMap;

import java.util.List;

public interface Room {
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
    List<Long> getUsers();

    /**
     * Tilføjer ny bruger til chatrummet
     *
     * @param userToAdd   Id'et på brugeren der skal tilføjes
     * @param addedByUser Id'et på brugeren der forsøger at tilføje en ny bruger
     */
    void addUser(long userToAdd, long addedByUser);

    /**
     * Fjerner en bruger fra chatrummet
     *
     * @param user      id'et på brugeren der skal fjernes
     * @param adminUser id'et på brugeren som forsøger at fjerne
     */
    void removeUser(long user, long adminUser);

    /**
     * Laver objektet om til et map, så det kan sendes med JSON til clienten uden fejl
     *
     * @return Map af alle instans variabler
     */
    DataMap getData();

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

    /**
     * Sætter navnet på chatrummet
     *
     * @param name          Det nye navn
     * @param changedByUser id'et på brugeren som forsøger at ændre navnet
     */
    void setName(String name, long changedByUser);
}
