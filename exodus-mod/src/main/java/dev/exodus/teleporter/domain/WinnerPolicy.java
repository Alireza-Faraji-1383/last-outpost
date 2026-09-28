package dev.exodus.teleporter.domain;

import java.util.*;

public final class WinnerPolicy {
    private WinnerPolicy() {}
    public static List<UUID> select(Collection<EscapeCandidate> candidates,double radius,int capacity){
        if(candidates==null||radius<0||capacity<=0)return List.of();
        double maximum=radius*radius;
        return candidates.stream().filter(Objects::nonNull).filter(EscapeCandidate::eligible)
                .filter(c->c.playerId()!=null&&c.distanceSquared()<=maximum)
                .sorted(Comparator.comparingDouble(EscapeCandidate::distanceSquared).thenComparing(EscapeCandidate::playerId))
                .limit(capacity).map(EscapeCandidate::playerId).toList();
    }
}
