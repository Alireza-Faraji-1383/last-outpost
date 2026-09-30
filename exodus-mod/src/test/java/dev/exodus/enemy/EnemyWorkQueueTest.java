package dev.exodus.enemy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnemyWorkQueueTest {
    @Test void requestsCoalesceAndRemainFairAcrossTicks() {
        var queue=new EnemyWorkQueue<String,Integer>();
        queue.request("first",1);queue.request("second",2);queue.request("first",3);
        assertEquals(3,queue.poll());
        queue.request("first",4);
        assertEquals(2,queue.poll());
        assertEquals(4,queue.poll());
        assertNull(queue.poll());
    }
    @Test void cancelledRequestsDoNotConsumeFutureWork() {
        var queue=new EnemyWorkQueue<String,Integer>();
        queue.request("old",1);queue.request("live",2);
        queue.cancel("old");
        assertEquals(2,queue.poll());
        queue.clear();assertNull(queue.poll());
    }
    @Test void sustainedTwoHundredEnemyDemandCannotStarveLaterRequests() {
        var queue=new EnemyWorkQueue<Integer,Integer>();
        var seen=new java.util.HashSet<Integer>();
        for(int i=0;i<200;i++)queue.request(i,i);
        for(int tick=0;tick<25;tick++)for(int budget=0;budget<8;budget++){
            int enemy=queue.poll();
            seen.add(enemy);
            queue.request(enemy,enemy);
        }
        assertEquals(200,seen.size());
    }
}
