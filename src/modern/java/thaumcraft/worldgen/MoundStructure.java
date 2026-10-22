package thaumcraft.worldgen;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.structure.*;
import thaumcraft.config.ThaumcraftConfig;

public final class MoundStructure extends Structure {
    public static final MapCodec<MoundStructure> CODEC=simpleCodec(MoundStructure::new);
    public MoundStructure(StructureSettings settings) {super(settings);}
    public StructureType<?> type() {return MoundContent.MOUND.get();}
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!ThaumcraftConfig.generateStructure.get()||context.chunkGenerator() instanceof FlatLevelSource) return Optional.empty();
        int x=context.chunkPos().getMinBlockX()+4+context.random().nextInt(9),z=context.chunkPos().getMinBlockZ()+4+context.random().nextInt(9);
        int surface=context.chunkGenerator().getBaseHeight(x+9,z+9,Heightmap.Types.WORLD_SURFACE_WG,context.heightAccessor(),context.randomState());
        for (int[] offset:new int[][]{{0,0},{18,0},{18,18},{0,18},{9,9}}) {
            int height=context.chunkGenerator().getBaseHeight(x+offset[0],z+offset[1],Heightmap.Types.WORLD_SURFACE_WG,context.heightAccessor(),context.randomState());
            if (Math.abs(height-surface)>2) return Optional.empty();
            var state=context.chunkGenerator().getBaseColumn(x+offset[0],z+offset[1],context.heightAccessor(),context.randomState()).getBlock(height-1);
            if (!state.is(Blocks.STONE)&&!state.is(Blocks.GRASS_BLOCK)&&!state.is(Blocks.DIRT)&&!state.is(Blocks.SNOW_BLOCK)) return Optional.empty();
        }
        int baseY=surface-9;
        if (baseY-1<context.heightAccessor().getMinY()||baseY+15>=context.heightAccessor().getMaxY()) return Optional.empty();
        var origin=new BlockPos(x,baseY,z);long seed=context.random().nextLong();
        return Optional.of(new GenerationStub(origin,builder->builder.addPiece(new MoundPiece(origin,seed))));
    }
}
