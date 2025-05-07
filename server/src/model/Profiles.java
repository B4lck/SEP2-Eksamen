package model;

import java.util.List;

public interface Profiles extends ServerRequestHandler {
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
     * Opretter en ny profil
     * @param username Brugernavnet til profillen
     * @param password Adgangskoden til profillen
     * @return Profilen der blev oprettet
     */
    Profile createProfile(String username, String password);

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
