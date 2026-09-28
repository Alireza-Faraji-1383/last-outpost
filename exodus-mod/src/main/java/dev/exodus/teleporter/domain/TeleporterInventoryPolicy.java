package dev.exodus.teleporter.domain;
import java.util.UUID;
public final class TeleporterInventoryPolicy {private TeleporterInventoryPolicy(){} public static boolean mayInsert(int slot,TeleporterComponent component,ComponentStackState state,UUID matchId,boolean locked){return !locked&&ComponentPolicy.accepts(slot,component,state,matchId);}public static boolean mayRemove(boolean locked){return !locked;}}
