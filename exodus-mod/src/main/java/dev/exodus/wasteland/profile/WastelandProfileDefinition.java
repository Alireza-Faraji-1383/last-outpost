package dev.exodus.wasteland.profile;

import mcjty.lostcities.api.ILostCityProfile;
import mcjty.lostcities.api.ILostCityProfileSetup;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.config.LandscapeType;
import mcjty.lostcities.config.ProfileSetup;

public final class WastelandProfileDefinition {
    public static final String PROFILE_NAME = "exodus";
    public static final String BASE_PROFILE = "wasteland";
    public static final String LANDSCAPE_TYPE = "default";

    private WastelandProfileDefinition() {}

    public static void register(ILostCityProfileSetup setup) {
        ILostCityProfile profile = setup.createProfile(PROFILE_NAME, BASE_PROFILE);
        profile.setDescription("Project Exodus: medium-ruined competitive wasteland");
        profile.setCityChancle(0);
        profile.setRuinChance(0.35f, 0.65f, 0.90f);
        if (profile instanceof LostCityProfile concrete) configure(concrete);
    }

    /** Installed 7.5.5 exposes these fields; its public API omits radius and spawner settings. */
    public static void configure(LostCityProfile profile) {
        var layout = FixedCityLayout.load();
        profile.setDescription("Project Exodus: medium-ruined competitive wasteland");
        profile.setWorldStyle("exodus:wasteland");
        profile.LANDSCAPE_TYPE = LandscapeType.DEFAULT;
        profile.CITY_CHANCE = 0;
        // Sphere metadata uses nextInt(max-min), even when sphere generation is disabled.
        profile.CITY_MINRADIUS = layout.radius() - 1;
        profile.CITY_MAXRADIUS = layout.radius();
        profile.CITY_THRESHOLD = layout.threshold();
        profile.CITY_STYLE_THRESHOLD = .4f;
        profile.CITY_STYLE_ALTERNATIVE = "exodus:outskirts";
        profile.CITY_MINHEIGHT = -64;
        profile.CITY_MAXHEIGHT = 320;
        profile.CITY_SPAWN_DISTANCE2 = 0;
        profile.CITYSPHERE_CHANCE = 0;
        profile.CITYSPHERE_MONORAIL_CHANCE = 0;
        profile.GENERATE_SPAWNERS = false;
        profile.SCATTERED_CHANCE_MULTIPLIER = 0;
        profile.BUILDING_MINFLOORS = 0;
        profile.BUILDING_MAXFLOORS = 12;
        profile.BUILDING_MINFLOORS_CHANCE = 4;
        profile.BUILDING_MAXFLOORS_CHANCE = 12;
        profile.setRuinChance(.35f, .65f, .9f);
        profile.EXPLOSION_CHANCE = .0003f;
        profile.MINI_EXPLOSION_CHANCE = .003f;
    }

    public static void ensureInstalled() {
        LostCityProfile base = ProfileSetup.STANDARD_PROFILES.get(BASE_PROFILE);
        if (base == null) throw new IllegalStateException("Lost Cities wasteland profile is unavailable");
        LostCityProfile profile = new LostCityProfile(PROFILE_NAME, false);
        profile.copyFrom(base);
        configure(profile);
        ProfileSetup.STANDARD_PROFILES.put(PROFILE_NAME, profile);
    }
}
