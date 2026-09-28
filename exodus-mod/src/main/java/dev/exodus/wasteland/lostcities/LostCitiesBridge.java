package dev.exodus.wasteland.lostcities;
import dev.exodus.wasteland.domain.CityCoverageSampler;
import net.minecraft.server.level.ServerLevel;
public interface LostCitiesBridge{CityCoverageSampler.Result cityChunk(ServerLevel level,int chunkX,int chunkZ);}
