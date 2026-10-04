package dev.exodus.party;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PartyStateTest {
    private final UUID a=UUID.randomUUID(), b=UUID.randomUUID(), c=UUID.randomUUID();
    private PartyState state(){return new PartyState(UUID.randomUUID(),2);}
    private PartyState paired(){var s=state();s.create(a);s.invite(a,b,0,600);s.accept(b,a,1);return s;}
    @Test void invitationRequiresExplicitAcceptance(){var s=state();s.create(a);s.invite(a,b,0,600);assertFalse(s.sameParty(a,b));s.accept(b,a,1);assertTrue(s.sameParty(a,b));assertFalse(s.sameParty(a,a));assertEquals(Optional.of(b),s.teammate(a));}
    @Test void onlyCreatorInvitesAndCapacityIsTwo(){var s=paired();assertThrows(IllegalStateException.class,()->s.invite(b,c,2,600));assertThrows(IllegalStateException.class,()->s.invite(a,c,2,600));assertThrows(IllegalStateException.class,()->s.create(b));}
    @Test void expiredAndDeclinedInvitesCannotBeAccepted(){var s=state();s.create(a);s.invite(a,b,0,600);assertThrows(IllegalStateException.class,()->s.accept(b,a,600));s.invite(a,b,700,600);s.decline(b,a);assertThrows(IllegalStateException.class,()->s.accept(b,a,701));}
    @Test void secondAcceptanceRevalidatesCapacity(){var s=state();s.create(a);s.invite(a,b,0,600);s.invite(a,c,0,600);s.accept(b,a,1);assertThrows(IllegalStateException.class,()->s.accept(c,a,2));assertFalse(s.members().contains(c));}
    @Test void acceptanceDoesNotStealExistingMembership(){var s=state();s.create(a);s.invite(a,b,0,600);s.create(b);assertThrows(IllegalStateException.class,()->s.accept(b,a,1));assertEquals(b,s.owner(b).orElseThrow());}
    @Test void betrayalProtectsUntilExactDeadlineAndCannotBeRestarted(){var s=paired();assertTrue(s.leave(b,10,600));assertEquals(610,s.departureDeadline(a));assertThrows(IllegalStateException.class,()->s.leave(a,100,600));assertTrue(s.tick(609).isEmpty());assertTrue(s.sameParty(a,b));assertEquals(1,s.tick(610).size());assertFalse(s.sameParty(a,b));assertTrue(s.members().isEmpty());assertTrue(s.tick(611).isEmpty());}
    @Test void singletonLeavesImmediatelyAndFinalRemovalDissolvesPair(){var s=state();s.create(a);assertFalse(s.leave(a,0,600));assertTrue(s.members().isEmpty());s=paired();s.remove(a);assertTrue(s.members().isEmpty());}
    @Test void newMatchDoesNotInheritPartyOrInvitations(){var s=paired();var fresh=state();assertNotEquals(s.matchId(),fresh.matchId());assertFalse(fresh.sameParty(a,b));assertThrows(IllegalStateException.class,()->fresh.accept(b,a,1));}
    @Test void unrelatedInviteCannotBeUsedAfterCreatorRecreatesParty(){var s=state();s.create(a);s.invite(a,b,0,600);s.remove(a);s.create(a);assertThrows(IllegalStateException.class,()->s.accept(b,a,1));}
    @Test void duplicateInviteCannotExtendExistingDeadline(){var s=state();s.create(a);s.invite(a,b,0,600);assertThrows(IllegalStateException.class,()->s.invite(a,b,500,600));assertThrows(IllegalStateException.class,()->s.accept(b,a,600));}
    @Test void selfInvitationAndNonexistentPartyAreRejected(){var s=state();assertThrows(IllegalStateException.class,()->s.invite(a,b,0,600));s.create(a);assertThrows(IllegalStateException.class,()->s.invite(a,a,0,600));}
}
