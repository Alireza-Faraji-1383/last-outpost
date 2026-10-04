package dev.exodus.compat;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class InventoryExtractionTest {
 @Test void movesOnlySelectedStacksOnceAndPreservesIdentity() {
  Object component=new Object(), ordinary=new Object(), empty=new Object();
  var inventory=new ArrayList<>(List.of(component,ordinary)); var dropped=new ArrayList<Object>();
  InventoryExtraction.move(inventory,empty,s->s==component,s->{dropped.add(s);return true;});
  InventoryExtraction.move(inventory,empty,s->s==component,s->{dropped.add(s);return true;});
  assertEquals(List.of(component),dropped);assertSame(component,dropped.get(0));assertSame(ordinary,inventory.get(1));assertSame(empty,inventory.get(0));
 }
 @Test void failedSpawnKeepsOriginalStackForRetry() {
  Object component=new Object(),empty=new Object();var inventory=new ArrayList<>(List.of(component));
  InventoryExtraction.move(inventory,empty,s->s==component,s->false);
  assertSame(component,inventory.get(0));
 }
}
