package model;

import utils.PropertyChangeSubject;

import java.util.List;

public interface Rooms extends ServerRequestHandler, PropertyChangeSubject {
    /**
     * Opretter et nyt chatrum
     *
     * @param name   navn på chatrummet
     * @param userId id'et på brugeren som opretter chatrummet
     * @return ChatRoom objektet som bliver oprettet
     */
    Room createRoom(String name, long userId);

    /**
     * Henter et rum ud fra id'et på rummet
     *
     * @param roomId id'et på rummet
     * @param userId id'et på brugeren som prøver at hente rummet
     * @return ChatRoom hvis brugeren er en del af rummet, ellers thrower den
     */
    Room getRoom(long roomId, long userId);

    Room getRoom(long roomId);

    /**
     * Henter alle rum som en bruger deltager i
     *
     * @param userId id'et på brugeren
     * @return ChatRoom objektet for alle rum som brugeren deltager i
     */
    List<Room> getParticipatingRooms(long userId);

    /**
     * Tilføjer en ny bruger til et chatrum
     *
     * @param roomId      id'et på chatrummet
     * @param addUserId   id'et på den nye bruger
     * @param adminUserId id'et på brugeren som forsøger at tilføje newUser
     */
    void addMember(long roomId, long addUserId, long adminUserId);

    /**
     * Fjerner en bruger fra et chatrum
     *
     * @param roomId       id'et på chatrummet
     * @param removeUserId id'et på brugeren som skal fjernes
     * @param adminUserId  id'et på brugeren som forsøger
     */
    void removeMember(long roomId, long removeUserId, long adminUserId);

    /**
     * Sætter navnet på et chatrum
     *
     * @param roomId      id' på chatrummet
     * @param name        det nye navn på chatrummet
     * @param adminUserId id'et på brugeren der forsøger at ændre navnet på chatrummet
     */
    void setName(long roomId, String name, long adminUserId);

    /**
     * Muter en bruger
     *
     * @param roomId      id'et på chatrummet
     * @param muteUserId  id'et på brugeren som skal mutes.
     * @param adminUserId id'et på brugeren der muter.
     */
    void muteMember(long roomId, long muteUserId, long adminUserId);

    /**
     * Unmuter en bruger
     *
     * @param roomId       id'et på chatrummet
     * @param unmuteUserId id'et på user
     * @param adminUserId  id'et på adminUser
     */
    void unmuteMember(long roomId, long unmuteUserId, long adminUserId);

    /**
     * Forfremmer en bruger
     *
     * @param roomId        Id på chatRoom
     * @param promoteUserId Id på brugeren der skal forfremmes
     * @param adminUserId   Id på udførende
     */
    void promoteMember(long roomId, long promoteUserId, long adminUserId);

    /**
     * Degradere en bruger
     *
     * @param roomId       Id på chatRoom
     * @param demoteUserId Id på brugeren der skal degraderes
     * @param adminUserId  Id på udførende
     */
    void demoteMember(long roomId, long demoteUserId, long adminUserId);

    /**
     * Sætter et kaldenavn på en bruger i et chatrum
     *
     * @param roomId   id'et på chatrummet
     * @param userId   id'et på brugeren
     * @param nickname det nye kaldenavn
     */
    void setMemberNickname(long roomId, long userId, String nickname);

    /**
     * Fjerner et kaldenavn på en bruger i et chatrum
     *
     * @param roomId id'et på chatrummet
     * @param userId id'et på brugeren
     */
    void removeMemberNickname(long roomId, long userId);

    /**
     * Tjekker om rummet eksistere
     *
     * @param roomId id'et på chatrummet
     * @return true hvis rummet findes
     */
    boolean doesRoomExists(long roomId);

    boolean hasAccessTo(long roomId, long userId);

    /**
     * @param roomId id'et på chatrummet
     * @param userId id'et på udførende
     * @param color  farve
     * @return true hvis den findes
     */
    void setColor(long roomId, long userId, String color);

    void setFont(long roomId, long userId, String font);
}
