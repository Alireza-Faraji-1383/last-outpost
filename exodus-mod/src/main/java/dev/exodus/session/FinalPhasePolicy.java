package dev.exodus.session;
import java.util.*;
public final class FinalPhasePolicy {private FinalPhasePolicy(){} public static boolean allDead(Set<UUID> roster,Set<UUID> eliminated){return !roster.isEmpty()&&eliminated.containsAll(roster);}}
