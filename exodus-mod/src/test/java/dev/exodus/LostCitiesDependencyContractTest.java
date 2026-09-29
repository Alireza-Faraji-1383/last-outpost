package dev.exodus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LostCitiesDependencyContractTest {
    @Test
    void declaredRangeAcceptsPinnedLostCitiesRuntimeVersion() throws Exception {
        String properties = Files.readString(Path.of("gradle.properties"));
        String modsToml = Files.readString(Path.of("src", "main", "resources", "META-INF", "mods.toml"));

        String runtimeVersion = capture(properties, "(?m)^lost_cities_version=(.+)$");
        String lowerBound = capture(modsToml,
            "(?s)modId=\"lostcities\".*?versionRange=\"\\[([^,]+),[^)]+\\)\"");

        assertTrue(runtimeVersion.startsWith(lowerBound),
            () -> "Lost Cities " + runtimeVersion + " is outside the declared dependency range starting at " + lowerBound);
    }

    private static String capture(String input, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(input);
        assertTrue(matcher.find(), () -> "Expected pattern not found: " + regex);
        return matcher.group(1).trim();
    }
}
