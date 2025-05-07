package model;

import java.util.List;
import java.util.Optional;

public interface Profiles extends ServerRequestHandler {
    /**
     * Henter profilen ud fra et id
     *
     * @param uuid - id'et på brugeren
     * @return Objektet på profilen
     * @throws IllegalStateException - Hvis brugeren ikke findes
     */
    Profile getProfile(long uuid);

    /**
     * Tjekker om brugeren med ID'et findes
     *
     * @param uuid - ID'et på brugeren
     * @return True hvis brugeren findes, ellers false
     */
    boolean doesProfileExist(long uuid);

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
     * Gemmer en profil i manageren
     *
     * @param profile - Et profil objekt
     * @throws IllegalArgumentException - Hvis profilen er null
     * @throws IllegalStateException    - Hvis en profil med samme UUID allerede findes
     * @throws IllegalStateException    - Hvis en profil med samme brugernavn allerede findes
     */
    void addProfile(Profile profile);

    /**
     * Fjerner profil fra manageren
     *
     * @param profile - Profilen som skal fjernes
     * @throws IllegalArgumentException - Profile cannot be null
     */
    void removeProfile(Profile profile);

    /**
     * Henter en array af profiler ud fra matchende query
     *
     * @param query - Søgeord
     * @return Array over profiler med matchende filtre
     * @throws IllegalArgumentException - Hvis query er null
     */
    List<Profile> searchProfiles(String query);
}
