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
        var profile = profiles.createProfile("test", "1234");

        assertEquals(profile, profiles.getProfile(profile.getUserId()).orElseThrow());
    }

    @Test
    void getProfile_NonExisting() {
        assertTrue(profiles.getProfile(1).isEmpty());
    }

    @Test
    void getProfileByUsername_Regular() {
        var profile = profiles.createProfile("test", "1234");

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
        var profile = profiles.createProfile("test", "1234");

        assertEquals(profile, profiles.getProfileByUsername("test").orElseThrow());
    }

    @Test
    void createProfile_NullUsername() {
        assertThrows(IllegalArgumentException.class, () -> profiles.createProfile(null, "1234"));
    }

    @Test
    void createProfile_NullPassword() {
        assertThrows(IllegalArgumentException.class, () -> profiles.createProfile("test", null));

        assertFalse(profiles.getProfileByUsername("test").isPresent());
    }

    @Test
    void createProfile_TakenUsername() {
        var originalProfile = profiles.createProfile("test", "1234");

        assertThrows(IllegalStateException.class, () -> profiles.createProfile("test", "1234"));

        assertEquals(originalProfile, profiles.getProfileByUsername("test").orElseThrow());
    }

    @Test
    void searchProfiles_Regular() {
        profiles.createProfile("test1", "1234");
        profiles.createProfile("test2", "1234");
        profiles.createProfile("test3", "1234");
        profiles.createProfile("Steffen Steffensen", "1234");

        assertEquals(3, profiles.searchProfiles("test").size());
    }

    @Test
    void searchProfiles_Null() {
        assertThrows(IllegalArgumentException.class, () -> profiles.searchProfiles(null));
    }

}