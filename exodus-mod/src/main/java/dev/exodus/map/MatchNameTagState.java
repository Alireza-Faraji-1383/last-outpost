package dev.exodus.map;

/** Client visibility derived only from accepted authoritative match snapshots. */
public final class MatchNameTagState {
    private String dimension;

    public void receive(MatchMapSnapshot snapshot) {
        dimension = snapshot.clear() ? null : snapshot.dimension();
    }

    public boolean hidden(String entityDimension, boolean player, boolean self) {
        return player && !self && dimension != null && dimension.equals(entityDimension);
    }

    public void reset() { dimension = null; }
}
