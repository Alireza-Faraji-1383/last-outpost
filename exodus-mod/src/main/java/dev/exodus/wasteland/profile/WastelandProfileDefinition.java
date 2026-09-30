package dev.exodus.wasteland.profile;

import mcjty.lostcities.api.ILostCityProfile;
import mcjty.lostcities.api.ILostCityProfileSetup;

public final class WastelandProfileDefinition {
    public static final String PROFILE_NAME = "exodus";
    public static final String BASE_PROFILE = "wasteland";
    public static final String LANDSCAPE_TYPE = "default";

    private WastelandProfileDefinition() {}

    /** Compatibility with pinned Lost Cities 7.5.5: profile callbacks registered
     * through enqueue IMC arrive after its constructor has set up profiles. */
    public static void ensureRuntimeProfile() {
        var profiles = mcjty.lostcities.config.ProfileSetup.STANDARD_PROFILES;
        if (profiles.containsKey(PROFILE_NAME)) return;
        if (!profiles.containsKey(BASE_PROFILE)) {
            throw new IllegalStateException("Lost Cities base profile " + BASE_PROFILE + " is unavailable");
        }
        register(new mcjty.lostcities.config.LostCityProfileSetupImp());
    }

    public static void register(ILostCityProfileSetup setup) {
        ILostCityProfile profile = setup.createProfile(PROFILE_NAME, BASE_PROFILE);
        profile.setDescription("Project Exodus: medium-ruined competitive wasteland");
        profile.setCityChancle(0.01);
        profile.setRuinChance(0.65f, 0.25f, 0.75f);
    }
}
