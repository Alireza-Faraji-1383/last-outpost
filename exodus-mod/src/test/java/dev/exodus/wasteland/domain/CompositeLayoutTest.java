package dev.exodus.wasteland.domain;

import dev.exodus.wasteland.structure.CompositeDefinition;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompositeLayoutTest {
    @Test
    void factionBaseUsesTheApprovedFourPieceGeometry() {
        CompositeDefinition base = CompositeDefinition.faction("russian");

        assertEquals(new CompositeDefinition.Size(57, 20, 60), base.total());
        assertEquals(List.of(
                new CompositeDefinition.Part("russian_base_1", new CompositeDefinition.Size(29, 20, 30), 0, 0, 0),
                new CompositeDefinition.Part("russian_base_2", new CompositeDefinition.Size(28, 20, 30), 29, 0, 0),
                new CompositeDefinition.Part("russian_base_3", new CompositeDefinition.Size(29, 20, 30), 0, 0, 30),
                new CompositeDefinition.Part("russian_base_4", new CompositeDefinition.Size(28, 20, 30), 29, 0, 30)
        ), base.parts());
        assertTrue(CompositeLayout.hasExactCoverage(base));
    }

    @Test
    void detectsACompositeGap() {
        CompositeDefinition broken = new CompositeDefinition("broken", List.of(
                new CompositeDefinition.Part("one", new CompositeDefinition.Size(1, 1, 1), 0, 0, 0)
        ), new CompositeDefinition.Size(2, 1, 1));

        assertFalse(CompositeLayout.hasExactCoverage(broken));
    }
}
