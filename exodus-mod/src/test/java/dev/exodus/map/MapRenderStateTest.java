package dev.exodus.map;
import java.util.*;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class MapRenderStateTest {
 @Test void disconnectCannotRedrawPreviousWorld(){var state=new MapRenderState();var snapshot=new MatchMapSnapshot(1,1,UUID.randomUUID(),"lostcities:lostcity",0,0,2000,false,List.of());state.submit(snapshot);assertEquals(snapshot,state.pendingToRender());state.rendered(snapshot);assertNull(state.pendingToRender());state.disconnect();state.resetRendered();assertNull(state.pendingToRender());}
}
