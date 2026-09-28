package dev.exodus.teleporter.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StartCommitPolicyTest {
    @Test void inventoryClearIsAllowedOnlyAfterEveryPreflightStep(){
        assertFalse(StartCommitPolicy.mayClearInventories(true,true,false));
        assertFalse(StartCommitPolicy.mayClearInventories(true,false,true));
        assertTrue(StartCommitPolicy.mayClearInventories(true,true,true));
    }
}
