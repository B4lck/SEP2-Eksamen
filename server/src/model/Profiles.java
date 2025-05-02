package model;

import java.util.List;

public interface Profiles extends ClientMessageHandler {
    /**
     * Henter profilen ud fra et id
     * @param uuid id'et på brugeren
     * @return Objektet på profilen
     */
    Profile getProfile(long uuid);

    /**
     * Henter profilen ud fra et brugernavn
     * @param username Brugernavnet på brugeren
     * @return Objektet på profilen
     */
    Profile getProfileByUsername(String username);

    /**
     * Gemmer ny profil i manageren
     * @param profile Profil objektet
     */
    void addProfile(Profile profile);

    /**
     * Fjerner profil fra manageren
     * @param profile Profilen som skal fjernes
     */
    void removeProfile(Profile profile);

    /**
     * Henter en array af profiler ud fra matchende query
     *
     * @param query Filtre - Kommer snart!
     * @return Array over profiler med matchende filtre
     */
    List<Profile> searchProfiles(String query);
}
