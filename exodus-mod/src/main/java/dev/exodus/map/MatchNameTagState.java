package dev.exodus.map;

/** Client visibility derived only from accepted authoritative match snapshots. */
public final class MatchNameTagState {
    private String dimension;
    private java.util.Set<java.util.UUID> teammates=java.util.Set.of();

    public void receive(MatchMapSnapshot snapshot) {
        dimension = snapshot.clear() ? null : snapshot.dimension();
        teammates = snapshot.clear() ? java.util.Set.of() : snapshot.teammates();
    }

    public boolean hidden(String entityDimension, boolean player, boolean self) {
        return hidden(entityDimension,player,self,null);
    }
    public boolean hidden(String entityDimension,boolean player,boolean self,java.util.UUID id){return player&&!self&&dimension!=null&&dimension.equals(entityDimension)&&!teammate(entityDimension,id);}
    public boolean teammate(String entityDimension,java.util.UUID id){return id!=null&&dimension!=null&&dimension.equals(entityDimension)&&teammates.contains(id);}

    public void reset() { dimension = null; teammates=java.util.Set.of(); }
}
