package thaumcraft.trees;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import thaumcraft.content.SimpleContent;
public record SpecialTreeFeature(boolean silverwood) implements Feature{
    public static final MapCodec<SpecialTreeFeature> CODEC=RecordCodecBuilder.mapCodec(i->i.group(Codec.BOOL.fieldOf("silverwood").forGetter(SpecialTreeFeature::silverwood)).apply(i,SpecialTreeFeature::new));
    @Override public MapCodec<SpecialTreeFeature> codec(){return CODEC;}
    @Override public boolean place(WorldGenLevel level,ChunkGenerator generator,RandomSource random,BlockPos origin){
        if(!(level instanceof ServerLevel)&&!thaumcraft.config.ThaumcraftConfig.generateTrees.get())return false;
        String wood=silverwood?"silverwood":"greatwood";int height=(silverwood?8:11)+random.nextInt(3),width=silverwood?1:2;
        var logs=new LinkedHashSet<BlockPos>();var leaves=new LinkedHashSet<BlockPos>();
        for(int x=0;x<width;x++)for(int z=0;z<width;z++){var soil=origin.offset(x,-1,z);if(!level.hasChunkAt(soil)||!(level.getBlockState(soil).is(BlockTags.DIRT)||level.getBlockState(soil).is(Blocks.GRASS_BLOCK)||level.getBlockState(soil).is(Blocks.PODZOL)||level.getBlockState(soil).is(Blocks.MYCELIUM)))return false;for(int y=0;y<height;y++)logs.add(origin.offset(x,y,z));}
        for(int y=height-4;y<=height+1;y++){int radius=y>=height?2:silverwood?3:4;for(int x=-radius;x<=radius+width-1;x++)for(int z=-radius;z<=radius+width-1;z++)if(Math.abs(x)+Math.abs(z)<radius*2+width)leaves.add(origin.offset(x,y,z));}
        var all=new HashSet<>(logs);all.addAll(leaves);for(var p:all){if(p.getY()<level.getMinY()||p.getY()>=level.getMaxY()||!level.hasChunkAt(p))return false;var state=level.getBlockState(p);if(!state.isAir()&&!state.is(BlockTags.REPLACEABLE_BY_TREES)&&!state.is(TreeContent.BLOCKS.get("sapling_"+wood).get()))return false;}
        for(int x=0;x<width;x++)for(int z=0;z<width;z++)level.setBlock(origin.offset(x,-1,z),Blocks.DIRT.defaultBlockState(),2);
        var log=SimpleContent.BLOCKS.get("log_"+wood).get().defaultBlockState();for(var p:logs)level.setBlock(p,log,3);
        var leaf=TreeContent.BLOCKS.get("leaves_"+wood).get().defaultBlockState();for(var p:leaves)if(!logs.contains(p)){int distance=logs.stream().mapToInt(l->Math.abs(l.getX()-p.getX())+Math.abs(l.getY()-p.getY())+Math.abs(l.getZ()-p.getZ())).min().orElse(7);level.setBlock(p,leaf.setValue(LeavesBlock.DISTANCE,Math.min(7,distance)),3);}
        return true;
    }
}
