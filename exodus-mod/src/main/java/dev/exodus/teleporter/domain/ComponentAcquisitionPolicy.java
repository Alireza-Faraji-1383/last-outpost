package dev.exodus.teleporter.domain;

/** Decides how newly acquired components join the current match. */
public final class ComponentAcquisitionPolicy {
    public enum Action { KEEP, BIND, CLAIM_RARE, DELETE }
    private ComponentAcquisitionPolicy() {}

    public static Action action(boolean bound, boolean rare, boolean alreadyClaimed) {
        if (bound) return Action.KEEP;
        if (!rare) return Action.BIND;
        return alreadyClaimed ? Action.DELETE : Action.CLAIM_RARE;
    }
}
