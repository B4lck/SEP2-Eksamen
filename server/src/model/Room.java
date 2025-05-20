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
    List<Long> getMembers();

    /**
     * Tilføjer ny bruger til chatrummet
     *
     * @param addUserId   Id'et på brugeren der skal tilføjes
     * @param adminUserId Id'et på brugeren der forsøger at tilføje en ny bruger
     */
    void addMember(long addUserId, long adminUserId);

    /**
     * Fjerner en bruger fra chatrummet
     *
     * @param removeUserId      id'et på brugeren der skal fjernes
     * @param adminUserId id'et på brugeren som forsøger at fjerne
     */
    void removeMember(long removeUserId, long adminUserId);

    /**
     * Laver objektet om til et map, så det kan sendes med JSON til clienten uden fejl
     *
     * @return Map af alle instans variabler
     */
    DataMap getData();

    /**
     * Tjekker om en bruger er i chatrummet
     *
     * @param userId Id'et på brugeren
     * @return true hvis brugeren deltager i chatrummet
     */
    boolean isMember(long userId);

    /**
     * Sætter navnet på chatrummet
     *
     * @param name          Det nye navn
     * @param adminUserId id'et på brugeren som forsøger at ændre navnet
     */
    void setName(String name, long adminUserId);

    /**
     * Muter en bruger i chatrummet
     *
     * @param muteUserId Id på valgte bruger
     * @param adminUserId Id'et på udførende
     */
    void muteUser(long muteUserId, long adminUserId);

    /**
     * Unmuter bruger i chatrummet
     *
     * @param unmuteUserId Id på valgte bruger
     * @param adminUserId Id'et på udførende
     */
    void unmuteUser(long unmuteUserId, long adminUserId);

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
     * @param promoteUserId Id på valgte bruger
     * @param adminUserId Id på udførende
     */
    void promoteUser(long promoteUserId, long adminUserId);

    /**
     * Degraderer en bruger i rummet
     *
     * @param demoteUserId Id på valgte bruger
     * @param adminUserId Id på udførende
     */
    void demoteUser(long demoteUserId, long adminUserId);

    /**
     * Sætter kaldenavnet på en bruger
     *
     * @param userId id'et på brugeren
     * @param nickname det nye kaldenavn
     */
    void setNicknameOfUser(long userId, String nickname);

    /**
     * Fjerner et brugernavn fra en bruger
     * @param userId id'et på brugeren
     */
    void removeNicknameFromUser(long userId);

    /**
     * Tjekker om en bruger er admin
     *
     * @param userId id'et på brugeren der skal tjekkes
     * @return True hvis brugeren er admin
     */
    boolean isAdmin(long userId);

    /**
     * Henter en bruger i rummet (RoomMember)
     * @param userId
     */
    RoomMember getProfile(long userId);

    /**
     *
     * @param color farve
     */
    void setColor(String color);


    String getColor();

    long getLatestActivity();

    long getLatestMessage();

    void addAdminMember(long userId);

    void setFont(String font);

    String getFont();

    void setLatestReadMessage(long messageId, long userId);
}

