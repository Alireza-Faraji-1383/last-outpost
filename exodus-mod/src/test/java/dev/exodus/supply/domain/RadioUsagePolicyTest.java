package dev.exodus.supply.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RadioUsagePolicyTest {
    @Test void takeoverKeepsPhysicalRadioUsage() {
        UUID match = UUID.randomUUID();
        RadioUsage used = new RadioUsage(match, 1, 2_400L);

        assertEquals(2, RadioUsagePolicy.remaining(used, match, 3));
        assertEquals(2_400L, RadioUsagePolicy.normalize(used, match).cooldownUntilTick());
    }

    @Test void aNewMatchResetsUsageLazily() {
        RadioUsage old = new RadioUsage(UUID.randomUUID(), 2, 9_000L);

        RadioUsage reset = RadioUsagePolicy.normalize(old, UUID.randomUUID());

        assertEquals(0, reset.acceptedRequests());
        assertEquals(0L, reset.cooldownUntilTick());
    }

    @Test void unlimitedQuotaNeverRunsOutButCooldownStillApplies() {
        UUID match = UUID.randomUUID();
        RadioUsage used = new RadioUsage(match, 50, 200L);

        assertEquals(-1, RadioUsagePolicy.remaining(used, match, -1));
        assertEquals(RadioUsagePolicy.Decision.COOLDOWN,
                RadioUsagePolicy.evaluate(used, match, -1, 199L));
        assertEquals(RadioUsagePolicy.Decision.ALLOW,
                RadioUsagePolicy.evaluate(used, match, -1, 200L));
    }

    @Test void acceptingIncrementsCountAndSetsDeadline() {
        UUID match = UUID.randomUUID();
        RadioUsage accepted = RadioUsagePolicy.accept(new RadioUsage(match, 1, 0L), match, 500L);

        assertEquals(new RadioUsage(match, 2, 500L), accepted);
        assertEquals(RadioUsagePolicy.Decision.QUOTA_EXHAUSTED,
                RadioUsagePolicy.evaluate(accepted, match, 2, 500L));
    }
}
