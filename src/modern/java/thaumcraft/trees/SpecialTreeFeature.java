package thaumcraft.trees;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import thaumcraft.content.SimpleContent;

public record SpecialTreeFeature(boolean silverwood) implements Feature {
    public static final MapCodec<SpecialTreeFeature> CODEC=RecordCodecBuilder.mapCodec(i->i.group(Codec.BOOL.fieldOf("silverwood").forGetter(SpecialTreeFeature::silverwood)).apply(i,SpecialTreeFeature::new));
    @Override public MapCodec<SpecialTreeFeature> codec() {return CODEC;}
    @Override public boolean place(WorldGenLevel level,ChunkGenerator generator,RandomSource random,BlockPos origin) {
        // Sapling growth remains available when natural tree generation is disabled.
        if (!(level instanceof net.minecraft.server.level.ServerLevel) && !thaumcraft.config.ThaumcraftConfig.generateTrees.get()) return false;
        String wood=silverwood?"silverwood":"greatwood";
        boolean placed=placeShape(level,origin,silverwood?TreeShape.silverwood(random.nextLong()):TreeShape.greatwood(random.nextLong()),
            SimpleContent.BLOCKS.get("log_"+wood).get().defaultBlockState(),TreeContent.BLOCKS.get("leaves_"+wood).get().defaultBlockState(),silverwood?1:2);
        if (placed&&silverwood&&!(level instanceof net.minecraft.server.level.ServerLevel)) {
            for (int n=0;n<32;n++) {
                var p=origin.offset(random.nextInt(8)-random.nextInt(8),random.nextInt(4)-random.nextInt(4),random.nextInt(8)-random.nextInt(8));
                if (!inside(level,p)||!level.getBlockState(p).isAir()) continue;
                var flower=SimpleContent.BLOCKS.get("shimmerleaf").get().defaultBlockState();
                if (flower.canSurvive(level,p)) level.setBlock(p,flower,2);
            }
        }
        if (placed&&!silverwood&&!(level instanceof net.minecraft.server.level.ServerLevel)&&random.nextInt(8)==0)
            thaumcraft.worldgen.InfestedGreatwood.place(level,random,origin);
        return placed;
    }
    static boolean soil(BlockState s) {return s.is(BlockTags.DIRT)||s.is(Blocks.GRASS_BLOCK)||s.is(Blocks.MYCELIUM);}
    static boolean replaceable(BlockState s) {return s.isAir()||s.is(BlockTags.REPLACEABLE_BY_TREES)||s.is(BlockTags.SAPLINGS);}
    public static boolean placeShape(WorldGenLevel level,BlockPos origin,TreeShape shape,BlockState log,BlockState leaf,int width) {
        for (int x=0;x<width;x++) for (int z=0;z<width;z++) {
            var p=origin.offset(x,-1,z);if (!level.hasChunkAt(p)||!soil(level.getBlockState(p))) return false;
        }
        var logs=new LinkedHashMap<BlockPos,BlockState>();var foliage=new HashSet<BlockPos>();
        for (var entry:shape.logs.entrySet()) {
            var p=origin.offset(entry.getKey());
            if (!inside(level,p)) return false;
            var old=level.getBlockState(p);
            if (!replaceable(old) && !(entry.getKey().getY()<0&&soil(old))) return false;
            logs.put(p,log.hasProperty(RotatedPillarBlock.AXIS)?log.setValue(RotatedPillarBlock.AXIS,entry.getValue()):log);
        }
        for (var offset:shape.leaves) {
            var p=origin.offset(offset);if (!inside(level,p)) return false;
            if (replaceable(level.getBlockState(p))) foliage.add(p);
        }
        // Compute native leaf distances over the actual foliage graph before committing any writes.
        var distances=new HashMap<BlockPos,Integer>();var queue=new ArrayDeque<BlockPos>();
        for (var p:logs.keySet()) {distances.put(p,0);queue.add(p);}
        while (!queue.isEmpty()) {
            var p=queue.remove();int distance=distances.get(p)+1;if (distance>6) continue;
            for (var direction:Direction.values()) {var next=p.relative(direction);if (foliage.contains(next)&&!distances.containsKey(next)) {distances.put(next,distance);queue.add(next);}}
        }
        for (int x=0;x<width;x++) for (int z=0;z<width;z++) level.setBlock(origin.offset(x,-1,z),Blocks.DIRT.defaultBlockState(),2);
        logs.forEach((p,s)->level.setBlock(p,s,2));
        for (var p:foliage) if (distances.containsKey(p)) level.setBlock(p,leaf.setValue(LeavesBlock.DISTANCE,distances.get(p)),2);
        return true;
    }
    private static boolean inside(WorldGenLevel level,BlockPos p) {return p.getY()>=level.getMinY()&&p.getY()<level.getMaxY()&&level.hasChunkAt(p);}
}
