package model;

import utils.PropertyChangeSubject;

import java.util.List;

public interface Rooms extends ServerRequestHandler, PropertyChangeSubject {
    /**
     * Opretter et nyt chatrum
     *
     * @param name navn på chatrummet
     * @param user id'et på brugeren som opretter chatrummet
     * @return ChatRoom objektet som bliver oprettet
     */
    Room createRoom(String name, long user);

    /**
     * Henter et rum ud fra id'et på rummet
     *
     * @param room id'et på rummet
     * @param user id'et på brugeren som prøver at hente rummet
     * @return ChatRoom hvis brugeren er en del af rummet, ellers thrower den
     */
    Room getRoom(long room, long user);

    Room getRoom(long roomId);

    /**
     * Henter alle rum som en bruger deltager i
     *
     * @param user id'et på brugeren
     * @return ChatRoom objektet for alle rum som brugeren deltager i
     */
    List<Room> getParticipatingRooms(long user);

    /**
     * Tilføjer en ny bruger til et chatrum
     *
     * @param chatroom  id'et på chatrummet
     * @param newUser   id'et på den nye bruger
     * @param adminUser id'et på brugeren som forsøger at tilføje newUser
     */
    void addUser(long chatroom, long newUser, long adminUser);

    /**
     * Fjerner en bruger fra et chatrum
     *
     * @param chatroom  id'et på chatrummet
     * @param user      id'et på brugeren som skal fjernes
     * @param adminUser id'et på brugeren som forsøger
     */
    void removeUser(long chatroom, long user, long adminUser);

    /**
     * Sætter navnet på et chatrum
     *
     * @param chatroom  id' på chatrummet
     * @param name      det nye navn på chatrummet
     * @param adminUser id'et på brugeren der forsøger at ændre navnet på chatrummet
     */
    void setName(long chatroom, String name, long adminUser);

    /**
     * Muter en bruger
     *
     * @param chatroom  id'et på chatrummet
     * @param user      id'et på user
     * @param adminUser id'et på adminUser
     */
    void muteUser(long chatroom, long user, long adminUser);

    /**
     * Unmuter en bruger
     *
     * @param chatroom  id'et på chatrummet
     * @param user      id'et på user
     * @param adminUser id'et på adminUser
     */
    void unmuteUser(long chatroom, long user, long adminUser);

    /**
     * Forfremmer en bruger
     * @param chatroom  Id på chatRoom
     * @param user Id på brugeren der skal forfremmes
     * @param adminUser Id på udførende
     */
    void promoteUser(long chatroom, long user, long adminUser);

    /**
     * Degradere en bruger
     * @param chatroom  Id på chatRoom
     * @param user Id på brugeren der skal degraderes
     * @param adminUser Id på udførende
     */
    void demoteUser(long chatroom, long user, long adminUser);

    /**
     * Sætter et kaldenavn på en bruger i et chatrum
     *
     * @param chatroom id'et på chatrummet
     * @param user id'et på brugeren
     * @param nickname det nye kaldenavn
     */
    void setNicknameOfUser(long chatroom, long user, String nickname);

    /**
     * Fjerner et kaldenavn på en bruger i et chatrum
     * @param chatroom id'et på chatrummet
     * @param user id'et på brugeren
     */
    void removeNicknameOfUser(long chatroom, long user);

    /**
     * Tjekker om rummet eksistere
     *
     * @param chatroom id'et på chatrummet
     * @return true hvis rummet findes
     */
    boolean doesRoomExists(long chatroom);

    /**
     *
     * @param chatroom id'et på chatrummet
     * @param user id'et på udførende
     * @param color farve
     * @return true hvis den findes
     */
    void editColor (long chatroom, long user, String color);

    void setFont(long chatroomId, long user, String font);
}
