package dev.exodus.wasteland.lostcities;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DimensionProfileBindingTest {
    @Test void replacesExistingBiosphereAndDuplicatesButPreservesOtherDimensions() {
        var result = DimensionProfileBinding.replace(List.of("lostcities:lostcity=biosphere",
                "lostworlds:abyss=biosphere_caves", "lostcities:lostcity=other"), "lostcities:lostcity", "exodus");
        assertEquals(List.of("lostworlds:abyss=biosphere_caves", "lostcities:lostcity=exodus"), result);
    }
    @Test void addsMissingMappingAndIsIdempotent() {
        var result = DimensionProfileBinding.replace(List.of(), "lostcities:lostcity", "exodus");
        assertEquals(result, DimensionProfileBinding.replace(result, "lostcities:lostcity", "exodus"));
    }
}
