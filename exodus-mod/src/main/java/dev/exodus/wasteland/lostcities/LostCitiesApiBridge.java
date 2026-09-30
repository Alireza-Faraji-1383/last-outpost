package dev.exodus.wasteland.lostcities;
import dev.exodus.wasteland.domain.CityCoverageSampler;
import mcjty.lostcities.api.ILostCities;
import net.minecraft.server.level.ServerLevel;
public final class LostCitiesApiBridge implements LostCitiesBridge{
 private final ILostCities api;public LostCitiesApiBridge(ILostCities api){this.api=api;}
 @Override public java.util.Optional<net.minecraft.core.BlockPos> cityCenterAt(ServerLevel level,int x,int z){var info=api.getLostInfo(level);if(info==null||info.getChunkInfo(x,z).getCityInfo()==null)return java.util.Optional.empty();return java.util.Optional.of(new net.minecraft.core.BlockPos(x*16+8,info.getRealHeight(info.getChunkInfo(x,z).getCityLevel()),z*16+8));}
 @Override public CityCoverageSampler.Result cityChunk(ServerLevel level,int x,int z){var info=api.getLostInfo(level);if(info==null)return CityCoverageSampler.Result.UNAVAILABLE;return info.getChunkInfo(x,z).isCity()?CityCoverageSampler.Result.CITY:CityCoverageSampler.Result.OUTSIDE;}
}
