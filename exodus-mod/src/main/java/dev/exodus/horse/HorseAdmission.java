package dev.exodus.horse;

import java.util.UUID;

/** Managed horses are confined to their running match, including across chunk loads. */
public final class HorseAdmission {
    private HorseAdmission() {}
    public static boolean allowed(boolean running,UUID bound,UUID active,String dimension,String matchDimension) {
        return running && active!=null && active.equals(bound) && dimension.equals(matchDimension);
    }
}
