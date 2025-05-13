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
     * @param removedByUser id'et på brugeren som forsøger at fjerne
     */
    void removeUser(long user, long removedByUser);

    /**
     * Laver objektet om til et map, så det kan sendes med JSON til clienten uden fejl
     *
     * @return Map af alle instans variabler
     */
    DataMap getData();

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

    /**
     * Muter en bruger i chatrummet
     *
     * @param userId Id på valgte bruger
     * @param byUser Id'et på udførende
     */
    void muteUser(long userId, long byUser);

    /**
     * Unmuter bruger i chatrummet
     *
     * @param userId Id på valgte bruger
     * @param byUser Id'et på udførende
     */
    void unmuteUser(long userId, long byUser);

    /**
     * Tjekker om brugeren er muted
     *
     * @param userId Id på valgte bruger
     * @return True hvis brugeren er muted
     */
    boolean isMuted(long userId);

    /**
     * Forfremmer en bruger i rummet
     *
     * @param userId Id på valgte bruger
     * @param promotedByUser Id på udførende
     */
    void promoteUser(long userId, long promotedByUser);

    /**
     * Degraderer en bruger i rummet
     *
     * @param userId Id på valgte bruger
     * @param promotedByUser Id på udførende
     */
    void demoteUser(long userId, long promotedByUser);

    /**
     * Tjekker om en bruger er admin
     *
     * @param userId id'et på brugeren der skal tjekkes
     * @return True hvis brugeren er admin
     */
    boolean isAdmin(long userId);
}

