package dev.exodus.wasteland.loot;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnemyDropPolicyTest {
    @Test void inclusiveAmmoBoundsAndEmeraldProbability(){
        assertEquals(16,EnemyDropPolicy.amount(16,32,0));
        assertEquals(32,EnemyDropPolicy.amount(16,32,16));
        assertEquals(32,EnemyDropPolicy.amount(32,16,16));
        assertTrue(EnemyDropPolicy.roll(.099,.10));
        assertFalse(EnemyDropPolicy.roll(.10,.10));
        assertFalse(EnemyDropPolicy.roll(0,0));
    }
}
