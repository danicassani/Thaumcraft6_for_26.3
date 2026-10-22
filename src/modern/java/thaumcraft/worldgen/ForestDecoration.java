package thaumcraft.worldgen;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import thaumcraft.content.SimpleContent;
import thaumcraft.trees.*;

public record ForestDecoration(boolean ground) implements Feature {
    public static final MapCodec<ForestDecoration> CODEC=com.mojang.serialization.Codec.BOOL.fieldOf("ground").xmap(ForestDecoration::new,ForestDecoration::ground);
    public MapCodec<ForestDecoration> codec() {return CODEC;}
    public boolean place(WorldGenLevel level,ChunkGenerator generator,RandomSource random,BlockPos origin) {
        if (!ground) {
            if (!thaumcraft.config.ThaumcraftConfig.generateTrees.get()) return false;
            if (random.nextInt(18)==0) return new SpecialTreeFeature(true).place(level,generator,random,origin);
            if (random.nextInt(12)==0) return new SpecialTreeFeature(false).place(level,generator,random,origin);
            return SpecialTreeFeature.placeShape(level,origin,TreeShape.magicOak(random.nextLong()),Blocks.OAK_LOG.defaultBlockState(),Blocks.OAK_LEAVES.defaultBlockState(),1);
        }
        boolean placed=false;
        for (int n=0;n<3;n++) {
            int x=origin.getX()+4+random.nextInt(8),z=origin.getZ()+4+random.nextInt(8);
            var p=new BlockPos(x,level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z)-1,z);
            if (level.getBlockState(p).is(Blocks.GRASS_BLOCK)) {level.setBlock(p,thaumcraft.devices.DeviceContent.GRASS.get().defaultBlockState(),2);placed=true;break;}
        }
        // Decorate only the owning chunk, without touching builds or reading distant chunks.
        for (int n=0;n<24;n++) {
            int x=origin.getX()+random.nextInt(16),z=origin.getZ()+random.nextInt(16);
            var p=new BlockPos(x,level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z),z);
            if (!level.getBlockState(p.below()).is(Blocks.GRASS_BLOCK)||!level.getBlockState(p).isAir()) continue;
            boolean silver=false,wood=false;
            for (var d:Direction.Plane.HORIZONTAL) for (int dy=0;dy<3;dy++) {
                var state=level.getBlockState(p.relative(d).above(dy));
                wood|=state.is(BlockTags.LOGS);silver|=state.is(SimpleContent.BLOCKS.get("log_silverwood").get());
            }
            String plant=silver?"shimmerleaf":wood?"vishroom":null;
            if (plant!=null) {level.setBlock(p,SimpleContent.BLOCKS.get(plant).get().defaultBlockState(),2);placed=true;}
        }
        return placed;
    }
}
