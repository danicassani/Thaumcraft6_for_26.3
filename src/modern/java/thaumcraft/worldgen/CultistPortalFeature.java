package thaumcraft.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import thaumcraft.warp.*;

/** A single, persistent encounter saved with its generating chunk; never a loaded-chunk scan. */
public record CultistPortalFeature() implements Feature {
    public static final MapCodec<CultistPortalFeature> CODEC=MapCodec.unit(new CultistPortalFeature());
    public MapCodec<CultistPortalFeature> codec(){return CODEC;}
    public boolean place(WorldGenLevel level,ChunkGenerator generator,RandomSource random,BlockPos pos){
        if(!thaumcraft.config.ThaumcraftConfig.generateStructure.get()||thaumcraft.config.ThaumcraftConfig.wussMode.get()
            ||generator instanceof FlatLevelSource||!level.getLevel().dimension().equals(Level.OVERWORLD)
            ||level.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL||!level.getWorldBorder().isWithinBounds(pos)
            ||(pos.getY()<level.getMinY()||pos.getY()+3>=level.getMaxY()))return false;
        var ground=level.getBlockState(pos.below());
        if(!(ground.is(BlockTags.DIRT)||ground.is(Blocks.GRASS_BLOCK)||ground.is(Blocks.PODZOL)||ground.is(Blocks.MYCELIUM)||ground.is(BlockTags.BASE_STONE_OVERWORLD)||ground.is(BlockTags.SAND)
            ||ground.is(Blocks.SNOW_BLOCK))||!ground.isFaceSturdy(level,pos.below(),Direction.UP))return false;
        // The 1.5-wide portal stays inside the owning chunk, including on slopes/coasts.
        int x=pos.getX()&15,z=pos.getZ()&15;if(x<1||x>14||z<1||z>14)return false;
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)for(int y=0;y<3;y++){
            var p=pos.offset(dx,y,dz);if(!level.getBlockState(p).isAir()||!level.getFluidState(p).isEmpty())return false;
        }
        var portal=new WarpMonster(WarpCreatures.PORTAL.get(),level.getLevel(),WarpMonster.Kind.PORTAL);
        portal.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
        return level.addFreshEntity(portal);
    }
}
