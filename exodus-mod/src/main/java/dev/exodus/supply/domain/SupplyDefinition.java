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
        int sortOrder,
        String quotaGroup,
        String contents) {
    public SupplyDefinition(String id, String displayName, String iconItemId, String lootTableId,
                            Set<RadioType> radioTypes, int maxRequests, int cooldownSeconds,
                            SupplyCost cost, int smokeColor, boolean enabled, int sortOrder) {
        this(id, displayName, iconItemId, lootTableId, radioTypes, maxRequests, cooldownSeconds,
                cost, smokeColor, enabled, sortOrder, id, "");
    }
    public SupplyDefinition {
        radioTypes = radioTypes == null ? Set.of() : Set.copyOf(radioTypes);
        quotaGroup = quotaGroup == null ? id : quotaGroup;
        contents = contents == null ? "" : contents;
    }
}
