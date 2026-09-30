package dev.exodus.session;
import java.util.*;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class FinalPhasePolicyTest {
 @Test void leavingAndPendingAreNotDeath(){UUID a=UUID.randomUUID(),b=UUID.randomUUID();assertFalse(FinalPhasePolicy.allDead(Set.of(a,b),Set.of(a)));assertTrue(FinalPhasePolicy.allDead(Set.of(a,b),Set.of(a,b)));assertFalse(FinalPhasePolicy.allDead(Set.of(),Set.of()));}
}
