package dev.exodus.map;
/** Keeps retries local to the current client connection. */
public final class MapRenderState {
 private MatchMapSnapshot pending, drawn;
 public void submit(MatchMapSnapshot snapshot){pending=snapshot;}
 public MatchMapSnapshot pendingToRender(){return java.util.Objects.equals(pending,drawn)?null:pending;}
 public void rendered(MatchMapSnapshot snapshot){drawn=snapshot;}
 public void resetRendered(){drawn=null;}
 public void disconnect(){pending=null;drawn=null;}
}
