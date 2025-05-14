package model;

import java.util.List;
import java.util.Optional;

public interface Profiles extends ServerRequestHandler {
    /**
     * Henter profilen ud fra et id
     *
     * @param uuid - id'et på brugeren
     * @return En optional med profilen
     */
    Optional<Profile> getProfile(long uuid);

    /**
     * Henter profilen ud fra et brugernavn
     *
     * @param username - Brugernavnet på brugeren
     * @return En optional med profilen
     * @throws IllegalArgumentException - Hvis username er null
     */
    Optional<Profile> getProfileByUsername(String username);

    /**
     * Opretter en ny profil
     *
     * @param username - Brugernavnet til profillen
     * @param password - Adgangskoden til profillen
     * @return Profilen der blev oprettet
     * @throws IllegalArgumentException - Hvis brugernavnet er null
     * @throws IllegalArgumentException - Hvis adgangskoden er null
     * @throws IllegalStateException    - Se exceptions fra addProfile
     */
    Profile createProfile(String username, String password);

    /**
     * Henter en array af profiler ud fra matchende query
     *
     * @param query - Søgeord
     * @return Array over profiler med matchende filtre
     * @throws IllegalArgumentException - Hvis query er null
     */
    List<Profile> searchProfiles(String query);

    void updateUserActivity(long userId);
}
