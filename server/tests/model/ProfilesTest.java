package model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ProfilesTest {
    Profiles profiles;

    @BeforeEach
    void setUp() throws SQLException {
        Database.startTesting();

        var model = new ChatModel();

        profiles = model.getProfiles();
    }

    @AfterEach
    void tearDown() throws SQLException {
        Database.endTesting();
    }

    @Test
    void getProfile_Regular() {
        var profile = profiles.createProfile("test", "12345678");

        assertEquals(profile, profiles.getProfile(profile.getUserId()).orElseThrow());
    }

    @Test
    void getProfile_NonExisting() {
        assertTrue(profiles.getProfile(1).isEmpty());
    }

    @Test
    void getProfileByUsername_Regular() {
        var profile = profiles.createProfile("test", "12345678");

        assertEquals(profile, profiles.getProfileByUsername("test").orElseThrow());
    }

    @Test
    void getProfileByUsername_NonExisting() {
        assertThrows(NoSuchElementException.class, () -> profiles.getProfileByUsername("test").orElseThrow());
    }

    @Test
    void getProfileByUsername_Null() {
        assertThrows(IllegalArgumentException.class, () -> profiles.getProfileByUsername(null));
    }

    @Test
    void createProfile_Regular() {
        var profile = profiles.createProfile("test", "12345678");

        assertEquals(profile, profiles.getProfileByUsername("test").orElseThrow());
    }

    @Test
    void createProfile_NullUsername() {
        assertThrows(IllegalArgumentException.class, () -> profiles.createProfile(null, "12345678"));
    }

    @Test
    void createProfile_NullPassword() {
        assertThrows(IllegalArgumentException.class, () -> profiles.createProfile("test", null));

        assertFalse(profiles.getProfileByUsername("test").isPresent());
    }

    @Test
    void createProfile_TakenUsername() {
        var originalProfile = profiles.createProfile("test", "12345678");

        assertThrows(IllegalStateException.class, () -> profiles.createProfile("test", "12345678"));

        assertEquals(originalProfile, profiles.getProfileByUsername("test").orElseThrow());
    }

    @Test
    void searchProfiles_Regular() {
        profiles.createProfile("test1", "12345678");
        profiles.createProfile("test2", "12345678");
        profiles.createProfile("test3", "12345678");
        profiles.createProfile("SteffenSteffensen", "12345678");

        assertEquals(3, profiles.searchProfiles("test").size());
    }

    @Test
    void searchProfiles_Null() {
        assertThrows(IllegalArgumentException.class, () -> profiles.searchProfiles(null));
    }

    @Test
    void getBlockedProfiles_Regular() {
        profiles.createProfile("test1", "12345678");
        profiles.createProfile("test2", "12345678");

        profiles.blockProfile(profiles.getProfileByUsername("test1").orElseThrow().getUserId(), profiles.getProfileByUsername("test2").orElseThrow().getUserId());

        assertEquals(1, profiles.getBlockedProfiles(profiles.getProfileByUsername("test2").orElseThrow().getUserId()).size());
    }

    @Test
    void getBlockedProfiles_NonExisting() {
        assertThrows(IllegalStateException.class, () -> profiles.getBlockedProfiles(-1));
    }

    @Test
    void blockProfile_Regular() {
        profiles.createProfile("test1", "12345678");
        profiles.createProfile("test2", "12345678");

        profiles.blockProfile(profiles.getProfileByUsername("test1").orElseThrow().getUserId(), profiles.getProfileByUsername("test2").orElseThrow().getUserId());

        assertEquals(1, profiles.getBlockedProfiles(profiles.getProfileByUsername("test2").orElseThrow().getUserId()).size());
    }

    @Test
    void blockProfile_BlockNonExisting() {
        profiles.createProfile("test1", "12345678");

        assertThrows(IllegalStateException.class, () -> profiles.blockProfile(-1, profiles.getProfileByUsername("test1").orElseThrow().getUserId()));

        assertEquals(0, profiles.getBlockedProfiles(profiles.getProfileByUsername("test1").orElseThrow().getUserId()).size());
    }

    @Test
    void blockProfile_BlockForNonExisting() {
        profiles.createProfile("test1", "12345678");

        assertThrows(IllegalStateException.class, () -> profiles.blockProfile(profiles.getProfileByUsername("test1").orElseThrow().getUserId(), -1));
    }

    @Test
    void blockProfile_AlreadyBlocked() {
        profiles.createProfile("test1", "12345678");
        profiles.createProfile("test2", "12345678");

        profiles.blockProfile(profiles.getProfileByUsername("test1").orElseThrow().getUserId(), profiles.getProfileByUsername("test2").orElseThrow().getUserId());

        assertDoesNotThrow(() -> profiles.blockProfile(profiles.getProfileByUsername("test1").orElseThrow().getUserId(), profiles.getProfileByUsername("test2").orElseThrow().getUserId()));

        assertEquals(1, profiles.getBlockedProfiles(profiles.getProfileByUsername("test2").orElseThrow().getUserId()).size());
    }

    @Test
    void unblockProfile_Regular() {
        profiles.createProfile("test1", "12345678");
        profiles.createProfile("test2", "12345678");

        profiles.blockProfile(profiles.getProfileByUsername("test1").orElseThrow().getUserId(), profiles.getProfileByUsername("test2").orElseThrow().getUserId());

        profiles.unblockProfile(profiles.getProfileByUsername("test1").orElseThrow().getUserId(), profiles.getProfileByUsername("test2").orElseThrow().getUserId());

        assertEquals(0, profiles.getBlockedProfiles(profiles.getProfileByUsername("test2").orElseThrow().getUserId()).size());
    }

    @Test
    void unblockProfile_BlockNonExisting() {
        profiles.createProfile("test1", "12345678");

        assertThrows(IllegalStateException.class, () -> profiles.unblockProfile(-1, profiles.getProfileByUsername("test1").orElseThrow().getUserId()));

        assertEquals(0, profiles.getBlockedProfiles(profiles.getProfileByUsername("test1").orElseThrow().getUserId()).size());
    }

    @Test
    void unblockProfile_BlockForNonExisting() {
        profiles.createProfile("test1", "12345678");

        assertThrows(IllegalStateException.class, () -> profiles.unblockProfile(profiles.getProfileByUsername("test1").orElseThrow().getUserId(), -1));
    }

    @Test
    void unblockProfile_NotAlreadyBlocked() {
        profiles.createProfile("test1", "12345678");
        profiles.createProfile("test2", "12345678");

        assertDoesNotThrow(() -> profiles.unblockProfile(profiles.getProfileByUsername("test1").orElseThrow().getUserId(), profiles.getProfileByUsername("test2").orElseThrow().getUserId()));

        assertEquals(0, profiles.getBlockedProfiles(profiles.getProfileByUsername("test2").orElseThrow().getUserId()).size());
    }

}