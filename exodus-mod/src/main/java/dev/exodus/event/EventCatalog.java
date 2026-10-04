package dev.exodus.event;

import dev.exodus.event.domain.EventDefinition;
import java.util.*;

public final class EventCatalog {
    private static List<EventDefinition> definitions=List.of();
    private EventCatalog(){}
    public static List<EventDefinition> all(){return definitions;}
    public static Optional<EventDefinition> get(String id){return definitions.stream().filter(d->d.id().equals(id)).findFirst();}
    public static void replace(Collection<EventDefinition> values){definitions=values.stream().sorted(Comparator.comparing(EventDefinition::id)).toList();}
}
