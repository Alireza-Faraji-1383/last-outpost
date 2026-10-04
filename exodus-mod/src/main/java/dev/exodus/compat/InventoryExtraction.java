package dev.exodus.compat;
import java.util.List;
import java.util.function.Predicate;
public final class InventoryExtraction {
 private InventoryExtraction() {}
 public static <T> void move(List<T> inventory,T empty,Predicate<T> selected,Predicate<T> destination) {
  for(int i=0;i<inventory.size();i++) {
   T stack=inventory.get(i);
   if(selected.test(stack)&&destination.test(stack)) inventory.set(i,empty);
  }
 }
}
