package dev.exodus.supply.domain;

import java.util.Set;

public record SupplyDefinition(
        String id,
        String displayName,
        String iconItemId,
        String lootTableId,
        Set<RadioType> radioTypes,
        int maxRequests,
        int cooldownSeconds,
        SupplyCost cost,
        int smokeColor,
        boolean enabled,
        int sortOrder) {
    public SupplyDefinition {
        radioTypes = radioTypes == null ? Set.of() : Set.copyOf(radioTypes);
    }
}
