package thaumcraft.rifts;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import thaumcraft.config.ThaumcraftConfig;

/** Shared light and configuration rules for natural wisps and rift encounters. */
public final class WispSpawning {
    private WispSpawning() {}
    public static boolean enabled(LevelAccessor level) {
        return ThaumcraftConfig.allowSpawnWisp.get()&&!ThaumcraftConfig.wussMode.get()&&level.getDifficulty()!=Difficulty.PEACEFUL;
    }
    public static boolean light(LevelAccessor level,BlockPos pos,RandomSource random) {
        return level.getBrightness(LightLayer.SKY,pos)<=random.nextInt(32)
            &&level.getMaxLocalRawBrightness(pos)<=random.nextInt(8);
    }
    private static boolean placement(EntityType<Wisp> type,ServerLevelAccessor level,EntitySpawnReason reason,BlockPos pos,RandomSource random) {
        return enabled(level)&&level.getFluidState(pos).isEmpty()&&level.getFluidState(pos.above()).isEmpty()&&light(level,pos,random);
    }
    public static void register() {
        SpawnPlacementRegisterEvent.BUS.addListener(event->event.register(RiftContent.WISP.get(),SpawnPlacementTypes.NO_RESTRICTIONS,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,WispSpawning::placement,SpawnPlacementRegisterEvent.Operation.REPLACE));
    }
}
