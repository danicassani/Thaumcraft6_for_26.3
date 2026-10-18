package thaumcraft.rifts;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import thaumcraft.aura.*;

public final class RiftSpawning {
    private RiftSpawning() {}
    public static boolean trySpawn(ServerLevel level,int chunkX,int chunkZ) {
        if(!thaumcraft.config.ThaumcraftConfig.generateFluxRifts.get() || level.getChunkSource().getChunkNow(chunkX,chunkZ)==null
            || !level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.pack(chunkX,chunkZ)))return false;
        int x=chunkX*16+level.getRandom().nextInt(16),z=chunkZ*16+level.getRandom().nextInt(16);
        int y=level.getHeight(Heightmap.Types.WORLD_SURFACE,x,z);
        if(!level.dimensionType().hasSkyLight()) {
            y=level.getMinY()+10;
            while(y<level.getMaxY()-5 && !level.getBlockState(new BlockPos(x,y,z)).isAir())y+=1+level.getRandom().nextInt(5);
        }
        if(y>=level.getMaxY()-4)return false;
        var pos=new BlockPos(x,y,z);var cell=AuraManager.get(level,pos);if(cell==null)return false;
        double initialSize=Math.sqrt(cell.flux()*3);if(initialSize<=5)return false;int size=Math.min(100,(int)initialSize);
        if(!level.getEntitiesOfClass(FluxRiftEntity.class,new AABB(pos).inflate(32)).isEmpty())return false;
        var rift=new FluxRiftEntity(RiftContent.RIFT.get(),level);rift.setPos(x+.5,y+.5,z+.5);rift.setSize(size);rift.setSeed(level.getRandom().nextInt());
        if(!level.addFreshEntity(rift))return false;AuraManager.drainFlux(level,pos,size,false);return true;
    }
}
