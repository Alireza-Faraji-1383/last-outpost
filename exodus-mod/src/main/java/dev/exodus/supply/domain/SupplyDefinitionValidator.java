package dev.exodus.supply.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public final class SupplyDefinitionValidator {
    private static final Pattern RESOURCE_ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");
    private static final Comparator<SupplyDefinition> ORDER = Comparator
            .comparingInt(SupplyDefinition::sortOrder)
            .thenComparing(SupplyDefinition::displayName)
            .thenComparing(SupplyDefinition::id);

    public record Error(String field, String message) {}

    private SupplyDefinitionValidator() {}

    public static List<Error> validate(SupplyDefinition definition) {
        List<Error> errors = new ArrayList<>();
        resource(errors, "id", definition.id());
        resource(errors, "quota_group", definition.quotaGroup());
        if (definition.displayName() == null || definition.displayName().isBlank())
            errors.add(new Error("display_name", "must not be blank"));
        resource(errors, "icon", definition.iconItemId());
        resource(errors, "loot_table", definition.lootTableId());
        if (definition.radioTypes().isEmpty()) errors.add(new Error("radio_types", "must not be empty"));
        if (definition.maxRequests() == 0 || definition.maxRequests() < -1)
            errors.add(new Error("max_requests", "must be -1 or positive"));
        if (definition.cooldownSeconds() < 0)
            errors.add(new Error("cooldown_seconds", "must not be negative"));
        if (definition.cost() != null) {
            resource(errors, "cost.item", definition.cost().itemId());
            if (definition.cost().count() <= 0) errors.add(new Error("cost.count", "must be positive"));
        }
        if (definition.smokeColor() < 0 || definition.smokeColor() > 0xFFFFFF)
            errors.add(new Error("drop.smoke_color", "must be a six-digit RGB value"));
        return List.copyOf(errors);
    }

    public static List<SupplyDefinition> sorted(Iterable<SupplyDefinition> definitions) {
        List<SupplyDefinition> result = new ArrayList<>();
        definitions.forEach(result::add);
        result.sort(ORDER);
        return List.copyOf(result);
    }

    private static void resource(List<Error> errors, String field, String value) {
        if (value == null || !RESOURCE_ID.matcher(value).matches())
            errors.add(new Error(field, "must be a namespaced resource ID"));
    }
}
