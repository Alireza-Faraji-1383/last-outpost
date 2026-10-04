package dev.exodus.wasteland.profile;

import mcjty.lostcities.api.ILostCityProfile;
import mcjty.lostcities.api.ILostCityProfileSetup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WastelandProfileDefinitionTest {
    @Test
    void registersMissingProfileInLostCitiesRuntimeRegistryAndPreservesExistingProfile() {
        var profiles = mcjty.lostcities.config.ProfileSetup.STANDARD_PROFILES;
        var oldBase = profiles.put("wasteland", new mcjty.lostcities.config.LostCityProfile("wasteland", false));
        var oldExodus = profiles.remove("exodus");
        try {
            WastelandProfileDefinition.ensureRuntimeProfile();
            var registered = profiles.get("exodus");
            org.junit.jupiter.api.Assertions.assertNotNull(registered);
            WastelandProfileDefinition.ensureRuntimeProfile();
            org.junit.jupiter.api.Assertions.assertSame(registered, profiles.get("exodus"));
        } finally {
            profiles.remove("exodus");
            profiles.remove("wasteland");
            if (oldBase != null) profiles.put("wasteland", oldBase);
            if (oldExodus != null) profiles.put("exodus", oldExodus);
        }
    }
    @Test
    void usesWastelandWithDefaultLandscape() {
        assertEquals("wasteland", WastelandProfileDefinition.BASE_PROFILE);
        assertEquals("default", WastelandProfileDefinition.LANDSCAPE_TYPE);
    }

    @Test
    void registersMediumRuinedExodusProfileFromRareCities() {
        RecordingSetup setup = new RecordingSetup();

        WastelandProfileDefinition.register(setup);

        assertEquals("exodus", setup.profileName);
        assertEquals("wasteland", setup.baseProfile);
        assertEquals("Project Exodus: medium-ruined competitive wasteland", setup.profile.description);
        assertEquals(0.0, setup.profile.cityChance);
        assertEquals(0.35f, setup.profile.ruinChance);
        assertEquals(0.65f, setup.profile.minimumRuinLevel);
        assertEquals(0.90f, setup.profile.maximumRuinLevel);
    }

    @Test void concreteProfileHasNoSpheresOrSpawnersAndKeepsLoot() {
        var profile = new mcjty.lostcities.config.LostCityProfile("test", false);
        WastelandProfileDefinition.configure(profile);
        assertEquals(mcjty.lostcities.config.LandscapeType.DEFAULT, profile.LANDSCAPE_TYPE);
        assertEquals(0, profile.CITYSPHERE_CHANCE);
        assertEquals(0, profile.CITY_CHANCE);
        assertFalse(profile.GENERATE_SPAWNERS);
        assertTrue(profile.GENERATE_LOOT);
        assertTrue(profile.CITY_MAXRADIUS >= FixedCityLayout.load().radius());
        assertTrue(profile.CITY_MAXRADIUS > profile.CITY_MINRADIUS,
                "Lost Cities sphere metadata calls nextInt(maxRadius-minRadius) even for disabled spheres");
        assertEquals("exodus:wasteland", profile.getWorldStyle());
        assertEquals(.2f, profile.CHEST_WITHOUT_LOOT_CHANCE);
    }

    private static final class RecordingSetup implements ILostCityProfileSetup {
        private String profileName;
        private String baseProfile;
        private final RecordingProfile profile = new RecordingProfile();

        @Override
        public ILostCityProfile createProfile(String name, String inheritFrom) {
            profileName = name;
            baseProfile = inheritFrom;
            return profile;
        }
    }

    private static final class RecordingProfile implements ILostCityProfile {
        private String description;
        private double cityChance;
        private float ruinChance;
        private float minimumRuinLevel;
        private float maximumRuinLevel;

        @Override public void setDescription(String value) { description = value; }
        @Override public void setCityChancle(double value) { cityChance = value; }
        @Override public void setRuinChance(float chance, float minimum, float maximum) {
            ruinChance = chance;
            minimumRuinLevel = minimum;
            maximumRuinLevel = maximum;
        }
        @Override public void setWorldStyle(String ignored) {}
        @Override public void setGroundLevel(int ignored) {}
        @Override public void setCityLevelHeights(int a, int b, int c, int d) {}
        @Override public void setCityLevelHeights(int a, int b, int c, int d, int e, int f, int g, int h) {}
        @Override public void setOceanCorrectionBorder(int ignored) {}
    }
}
