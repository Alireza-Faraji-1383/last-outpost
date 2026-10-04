package dev.exodus;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionContractTest {
    @Test
    void distributionOutputsReadTheAuthoritativeModVersion() throws Exception {
        String properties = Files.readString(Path.of("gradle.properties"));
        String curseForge = Files.readString(Path.of("tools", "build-curseforge-pack.ps1"));
        String curseForgeTest = Files.readString(Path.of("tools", "test-curseforge-pack.ps1"));
        String modrinth = Files.readString(Path.of("tools", "build-mrpack.ps1"));

        assertTrue(properties.contains("mod_version=0.7.3"));
        for (String content : new String[]{curseForge, curseForgeTest, modrinth}) {
            assertTrue(content.contains("gradle.properties"));
            assertTrue(content.contains("$packVersion"));
            assertFalse(content.contains("Project-Exodus-0.5.2"));
            assertFalse(content.contains("exodus-0.2.1.jar"));
            assertFalse(content.contains("exodus-0.2.0.jar"));
            assertFalse(content.contains("Project-Exodus-0.2.1"));
            assertFalse(content.contains("Project-Exodus-0.2.0"));
            assertFalse(content.contains("0.1.0"));
        }
    }
}
