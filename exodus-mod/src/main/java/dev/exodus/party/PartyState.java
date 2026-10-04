package dev.exodus.party;

import java.util.*;

/** Match-owned, server-thread state. Deadlines are captured and never extended by reconnects. */
public final class PartyState {
    private static final class Party {
        final Set<UUID> members=new LinkedHashSet<>();
        long departure=-1;
        Party(UUID creator){members.add(creator);}
    }
    private record Invitation(UUID owner,UUID target,long expiry) {}
    private final UUID matchId;
    private final int capacity;
    private final Map<UUID,Party> parties=new HashMap<>();
    private final Map<UUID,UUID> membership=new HashMap<>();
    private final Map<InvitationKey,Invitation> invitations=new HashMap<>();
    private record InvitationKey(UUID owner,UUID target) {}

    public PartyState(UUID matchId,int capacity){this.matchId=Objects.requireNonNull(matchId);if(capacity!=2)throw new IllegalArgumentException("Parties must contain at most two players");this.capacity=capacity;}
    public UUID matchId(){return matchId;}
    public Set<UUID> members(){return Set.copyOf(membership.keySet());}
    public Optional<UUID> owner(UUID player){return Optional.ofNullable(membership.get(player));}
    public Optional<UUID> teammate(UUID player){var owner=membership.get(player);if(owner==null)return Optional.empty();return parties.get(owner).members.stream().filter(id->!id.equals(player)).findFirst();}
    public boolean sameParty(UUID first,UUID second){return !Objects.equals(first,second)&&membership.containsKey(first)&&Objects.equals(membership.get(first),membership.get(second));}
    public long departureDeadline(UUID player){var owner=membership.get(player);return owner==null?-1:parties.get(owner).departure;}
    public Set<UUID> invitedBy(UUID target,long now){Set<UUID> owners=new HashSet<>();invitations.values().stream().filter(i->i.target().equals(target)&&i.expiry()>now).forEach(i->owners.add(i.owner()));return Set.copyOf(owners);}
    public void create(UUID player){require(!membership.containsKey(player),"You already belong to a party.");parties.put(player,new Party(player));membership.put(player,player);}
    public void invite(UUID creator,UUID target,long now,long duration){
        require(!creator.equals(target),"You cannot invite yourself.");
        require(parties.containsKey(creator),"Only the party creator can invite players.");
        Party party=parties.get(creator);require(party.departure<0,"Your party is separating.");require(party.members.size()<capacity,"Your party is full.");
        require(!membership.containsKey(target),"That player already belongs to a party.");
        var key=new InvitationKey(creator,target);var previous=invitations.get(key);require(previous==null||previous.expiry()<=now,"That invitation is already pending.");
        invitations.put(key,new Invitation(creator,target,Math.addExact(now,duration)));
    }
    public void accept(UUID target,UUID creator,long now){
        var invitation=invitations.get(new InvitationKey(creator,target));require(invitation!=null&&invitation.expiry()>now,"No valid invitation from that player.");
        require(!membership.containsKey(target),"Leave your current party before accepting.");
        var party=parties.get(creator);require(party!=null&&party.departure<0&&party.members.size()<capacity,"That party is unavailable or full.");
        party.members.add(target);membership.put(target,creator);
        invitations.entrySet().removeIf(e->e.getValue().target().equals(target)||e.getValue().owner().equals(creator));
    }
    public void decline(UUID target,UUID creator){require(invitations.remove(new InvitationKey(creator,target))!=null,"No invitation from that player.");}
    /** True means delayed separation; singleton parties disappear immediately. */
    public boolean leave(UUID player,long now,long duration){
        var owner=membership.get(player);require(owner!=null,"You do not belong to a party.");var party=parties.get(owner);
        require(party.departure<0,"Separation is already in progress.");
        if(party.members.size()==1){remove(player);return false;}
        party.departure=Math.addExact(now,duration);invitations.entrySet().removeIf(e->e.getValue().owner().equals(owner));return true;
    }
    public Set<UUID> remove(UUID player){
        var owner=membership.get(player);if(owner==null)return Set.of();var party=parties.remove(owner);Set<UUID> removed=Set.copyOf(party.members);
        removed.forEach(membership::remove);invitations.entrySet().removeIf(e->removed.contains(e.getValue().owner())||removed.contains(e.getValue().target()));return removed;
    }
    public List<Set<UUID>> tick(long now){
        invitations.entrySet().removeIf(e->e.getValue().expiry()<=now);
        List<Set<UUID>> ended=new ArrayList<>();for(var owner:new ArrayList<>(parties.keySet())){var p=parties.get(owner);if(p.departure>=0&&now>=p.departure)ended.add(remove(owner));}return List.copyOf(ended);
    }
    private static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
}
