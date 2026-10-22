package thaumcraft.worldgen;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import thaumcraft.content.SimpleContent;

public final class MoundPiece extends StructurePiece {
    private final long seed;
    private final Set<Long> completed=java.util.concurrent.ConcurrentHashMap.newKeySet();
    public MoundPiece(BlockPos origin,long seed) {
        super(MoundContent.PIECE.get(),0,new BoundingBox(origin.getX(),origin.getY()-1,origin.getZ(),origin.getX()+18,origin.getY()+15,origin.getZ()+18));this.seed=seed;
    }
    public MoundPiece(CompoundTag tag) {
        super(MoundContent.PIECE.get(),tag);seed=tag.getLongOr("LootSeed",0);
        for (long chunk:tag.getLongArray("Completed").orElseGet(()->new long[0])) completed.add(chunk);
    }
    protected void addAdditionalSaveData(StructurePieceSerializationContext context,CompoundTag tag) {
        tag.putLong("LootSeed",seed);tag.putLongArray("Completed",completed.stream().sorted().mapToLong(Long::longValue).toArray());
    }
    public BlockPos origin() {return new BlockPos(boundingBox.minX(),boundingBox.minY()+1,boundingBox.minZ());}
    public static int rarity(float roll) {return roll<.1F?2:roll<.33F?1:0;}
    public static String cache(long seed,int index) {
        var random=new Random(seed+index*0x9E3779B97F4A7C15L);int tier=rarity(random.nextFloat());
        return "loot_"+(random.nextFloat()<.3F?"crate_":"urn_")+List.of("common","uncommon","rare").get(tier);
    }
    public boolean trapped() {return new Random(seed^0x3A19C274L).nextInt(3)==0;}
    @Override public void postProcess(WorldGenLevel level,StructureManager structures,ChunkGenerator generator,RandomSource random,BoundingBox chunkBB,ChunkPos chunkPos,BlockPos reference) {
        if (completed.contains(chunkPos.pack())) return;
        var base=origin();
        for (var cell:MoundBlueprint.cells()) {
            var p=base.offset(cell.x(),cell.y(),cell.z());
            if (!chunkBB.isInside(p)) continue;
            level.setBlock(p,cell.state(),2);
            if (cell.state().getBlock() instanceof StairBlock||cell.state().is(Blocks.IRON_BARS)||cell.state().is(Blocks.LADDER)) level.getChunk(p).markPosForPostProcessing(p);
        }
        for (int index=0;index<2;index++) {
            var p=base.offset(9,1,index==0?7:11);
            if (chunkBB.isInside(p)) level.setBlock(p,SimpleContent.BLOCKS.get(cache(seed,index)).get().defaultBlockState(),2);
        }
        var chestPos=base.offset(10,1,9);
        if (chunkBB.isInside(chestPos)) {
            var block=trapped()?Blocks.TRAPPED_CHEST:Blocks.CHEST;
            level.setBlock(chestPos,block.defaultBlockState().setValue(ChestBlock.FACING,Direction.WEST),2);
            if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) chest.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON,seed^0x591FDA3L);
            if (trapped()) level.setBlock(base.offset(10,-1,9),Blocks.TNT.defaultBlockState(),2);
        }
        for (int index=0;index<2;index++) {
            var p=base.offset(4,5,index==0?4:14);
            if (!chunkBB.isInside(p)) continue;
            level.setBlock(p,Blocks.SPAWNER.defaultBlockState(),2);
            if (level.getBlockEntity(p) instanceof SpawnerBlockEntity spawner) spawner.setEntityId(index==0?EntityTypes.SKELETON:EntityTypes.ZOMBIE,RandomSource.create(seed+index));
        }
        completed.add(chunkPos.pack());
    }
}
