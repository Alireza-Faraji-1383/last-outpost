package dev.exodus.wasteland.domain;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CityCoverageSamplerTest{
 @Test void acceptsAtThresholdAndRejectsBelow(){var a=new CityCoverageSampler(15,100);for(int i=0;i<100;i++)a.record(i<15?CityCoverageSampler.Result.CITY:CityCoverageSampler.Result.OUTSIDE);assertTrue(a.accepted());var b=new CityCoverageSampler(15,100);for(int i=0;i<100;i++)b.record(i<14?CityCoverageSampler.Result.CITY:CityCoverageSampler.Result.OUTSIDE);assertFalse(b.accepted());}
 @Test void unavailableSkipsGuardExplicitly(){var s=new CityCoverageSampler(15,10);s.record(CityCoverageSampler.Result.UNAVAILABLE);assertTrue(s.skipped());assertEquals("Lost Cities API unavailable",s.skipReason());}
 @Test void budgetStopsSampling(){var s=new CityCoverageSampler(15,2);s.record(CityCoverageSampler.Result.CITY);s.record(CityCoverageSampler.Result.OUTSIDE);assertFalse(s.canSample());}
}
