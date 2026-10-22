package thaumcraft.worldgen;

import net.minecraft.core.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public final class InfestedGreatwood {
    private InfestedGreatwood() {}
    public static boolean place(WorldGenLevel level,RandomSource random,BlockPos origin) {
        for (int depth=1;depth<=2;depth++) {
            var p=origin.below(depth);
            if (!level.hasChunkAt(p)||p.getY()<level.getMinY()||level.getBlockEntity(p)!=null) return false;
            var s=level.getBlockState(p);
            if (!s.is(BlockTags.DIRT)&&!s.is(BlockTags.BASE_STONE_OVERWORLD)) return false;
        }
        level.setBlock(origin.below(),Blocks.SPAWNER.defaultBlockState(),2);
        if (level.getBlockEntity(origin.below()) instanceof SpawnerBlockEntity spawner) spawner.setEntityId(EntityTypes.CAVE_SPIDER,random);
        level.setBlock(origin.below(2),Blocks.CHEST.defaultBlockState(),2);
        if (level.getBlockEntity(origin.below(2)) instanceof ChestBlockEntity chest) chest.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON,random.nextLong());
        for (int n=0;n<50;n++) {
            var p=origin.offset(random.nextInt(14)-7,random.nextInt(10),random.nextInt(14)-7);
            if (!level.hasChunkAt(p)||!level.getBlockState(p).isAir()) continue;
            boolean touching=false;
            for (var direction:Direction.values()) {
                var q=p.relative(direction);if (!level.hasChunkAt(q)) continue;
                var s=level.getBlockState(q);touching|=s.is(thaumcraft.content.SimpleContent.BLOCKS.get("log_greatwood").get())||s.is(thaumcraft.trees.TreeContent.BLOCKS.get("leaves_greatwood").get());
            }
            if (touching) level.setBlock(p,Blocks.COBWEB.defaultBlockState(),2);
        }
        return true;
    }
}
