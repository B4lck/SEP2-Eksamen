package model;

import java.util.List;
import java.util.Optional;

public interface Profiles extends ServerRequestHandler {
    /**
     * Henter profilen ud fra et id
     *
     * @param userId - id'et på brugeren
     * @return En optional med profilen
     */
    Optional<Profile> getProfile(long userId);

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
     * @throws IllegalArgumentException - Hvis brugernavnet er null, er under 2 tegn eller over 20 tegn,
     *   eller hvis det indeholder andre tegn end bogstaver, tal, bindestrenge, punktum og understrenge.
     * @throws IllegalArgumentException - Hvis adgangskoden er null, eller under 8 tegn.
     * @throws IllegalStateException - Hvis brugernavnet er taget
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

    void updateProfileActivity(long userId);

    void blockProfile(long blockUserId, long blockedByUserId);

    void unblockProfile(long blockUserId, long blockedByUserId);

    List<Long> getBlockedProfiles(long userId);
}
