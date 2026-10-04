package dev.exodus.horse;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HorseAdmissionTest {
    @Test void matchHorseCannotEscapeCleanupByMovingDimensions() {
        UUID match=UUID.randomUUID();
        assertTrue(HorseAdmission.allowed(true,match,match,"exodus:wasteland","exodus:wasteland"));
        assertFalse(HorseAdmission.allowed(true,match,match,"minecraft:overworld","exodus:wasteland"));
        assertFalse(HorseAdmission.allowed(false,match,match,"exodus:wasteland","exodus:wasteland"));
        assertFalse(HorseAdmission.allowed(true,match,UUID.randomUUID(),"exodus:wasteland","exodus:wasteland"));
        assertFalse(HorseAdmission.allowed(true,match,null,"exodus:wasteland","exodus:wasteland"));
    }
}
