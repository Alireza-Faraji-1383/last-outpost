package dev.exodus.supply.catalog;

import dev.exodus.supply.domain.RadioType;
import dev.exodus.supply.domain.SupplyDefinition;
import dev.exodus.supply.domain.SupplyDefinitionValidator;
import java.util.*;

public final class SupplyCatalog {
    private static volatile Map<String,SupplyDefinition> values=Map.of();
    private SupplyCatalog(){}
    public static void replace(Collection<SupplyDefinition> next){LinkedHashMap<String,SupplyDefinition> map=new LinkedHashMap<>();SupplyDefinitionValidator.sorted(next).forEach(v->map.put(v.id(),v));values=Collections.unmodifiableMap(map);}
    public static Optional<SupplyDefinition> get(String id){return Optional.ofNullable(values.get(id));}
    public static List<SupplyDefinition> forType(RadioType type){return values.values().stream().filter(SupplyDefinition::enabled).filter(v->v.radioTypes().contains(type)).toList();}
}
