package dev.exodus.wasteland.profile;

import mcjty.lostcities.api.ILostCityProfile;
import mcjty.lostcities.api.ILostCityProfileSetup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WastelandProfileDefinitionTest {
    @Test
    void registersMediumRuinedExodusProfileFromRareCities() {
        RecordingSetup setup = new RecordingSetup();

        WastelandProfileDefinition.register(setup);

        assertEquals("exodus", setup.profileName);
        assertEquals("rarecities", setup.baseProfile);
        assertEquals("Project Exodus: medium-ruined competitive wasteland", setup.profile.description);
        assertEquals(0.01, setup.profile.cityChance);
        assertEquals(0.65f, setup.profile.ruinChance);
        assertEquals(0.25f, setup.profile.minimumRuinLevel);
        assertEquals(0.75f, setup.profile.maximumRuinLevel);
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
