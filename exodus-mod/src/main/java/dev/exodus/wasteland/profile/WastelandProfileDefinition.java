package dev.exodus.wasteland.profile;

import mcjty.lostcities.api.ILostCityProfile;
import mcjty.lostcities.api.ILostCityProfileSetup;

public final class WastelandProfileDefinition {
    public static final String PROFILE_NAME = "exodus";
    public static final String BASE_PROFILE = "rarecities";

    private WastelandProfileDefinition() {}

    public static void register(ILostCityProfileSetup setup) {
        ILostCityProfile profile = setup.createProfile(PROFILE_NAME, BASE_PROFILE);
        profile.setDescription("Project Exodus: medium-ruined competitive wasteland");
        profile.setCityChancle(0.01);
        profile.setRuinChance(0.65f, 0.25f, 0.75f);
    }
}
