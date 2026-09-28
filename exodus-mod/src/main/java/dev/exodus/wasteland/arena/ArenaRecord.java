package dev.exodus.wasteland.arena;

import java.util.UUID;

public final class ArenaRecord {
    private final UUID id, initiator;
    private final long ordinal;
    private final long centerX, centerZ;
    private final int arenaSize, buffer;
    private ArenaState state = ArenaState.PREPARING;
    private String failure = "";
    private final PreparationCheckpoint checkpoint = new PreparationCheckpoint();

    ArenaRecord(UUID id, UUID initiator, long ordinal, long centerX, long centerZ, int arenaSize, int buffer) {
        this.id=id; this.initiator=initiator; this.ordinal=ordinal; this.centerX=centerX; this.centerZ=centerZ;
        this.arenaSize=arenaSize; this.buffer=buffer;
    }
    static ArenaRecord restore(UUID id,UUID initiator,long ordinal,long centerX,long centerZ,int arenaSize,int buffer,ArenaState state,String failure){ArenaRecord r=new ArenaRecord(id,initiator,ordinal,centerX,centerZ,arenaSize,buffer);r.state=state;r.failure=failure;return r;}
    public UUID id(){return id;} public UUID initiator(){return initiator;} public long ordinal(){return ordinal;}
    public long centerX(){return centerX;} public long centerZ(){return centerZ;} public int arenaSize(){return arenaSize;}
    public int buffer(){return buffer;} public ArenaState state(){return state;} public String failure(){return failure;}
    public PreparationCheckpoint checkpoint(){return checkpoint;}
    void transition(ArenaState next,String reason){state=next;failure=reason==null?"":reason;}
}
