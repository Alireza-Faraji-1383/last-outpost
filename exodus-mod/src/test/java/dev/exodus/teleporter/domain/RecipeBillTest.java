package dev.exodus.teleporter.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RecipeBillTest {
    @Test void specialDropsCoverOneCompleteRecipeSet(){
        RecipeBill bill=RecipeBill.oneSet();
        assertEquals(2,bill.blazeRods());
        assertEquals(5,bill.enderPearls());
        assertEquals(14,bill.quartz());
    }
}
