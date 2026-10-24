package thaumcraft.eldritch;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import thaumcraft.config.ThaumcraftConfig;
public final class EldritchSpawning {
 private static boolean enabled(LevelAccessor l){return !ThaumcraftConfig.wussMode.get()&&l.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL;}
 private static boolean zombie(EntityType<BrainyZombie> t,ServerLevelAccessor l,EntitySpawnReason r,BlockPos p,RandomSource random){return enabled(l)&&(t==EldritchContent.ZOMBIES.get(EldritchMob.Kind.INHABITED).get()||ThaumcraftConfig.allowSpawnAngryZombie.get())&&net.minecraft.world.entity.monster.Monster.checkMonsterSpawnRules(t,l,r,p,random);}
 private static boolean bat(EntityType<FireBat> t,ServerLevelAccessor l,EntitySpawnReason r,BlockPos p,RandomSource random){return enabled(l)&&ThaumcraftConfig.allowSpawnFireBat.get()&&l.getFluidState(p).isEmpty()&&l.getMaxLocalRawBrightness(p)<=random.nextInt(8);}
 private static boolean elder(EntityType<thaumcraft.warp.WarpMonster> t,ServerLevelAccessor l,EntitySpawnReason r,BlockPos p,RandomSource random){return enabled(l)&&ThaumcraftConfig.allowSpawnElder.get()&&net.minecraft.world.entity.Mob.checkMobSpawnRules(t,l,r,p,random)&&l.getLevel().getEntitiesOfClass(thaumcraft.warp.WarpMonster.class,new net.minecraft.world.phys.AABB(p).inflate(32,16,32),m->m.kind==thaumcraft.warp.WarpMonster.Kind.GUARDIAN).isEmpty();}
 public static void register(){SpawnPlacementRegisterEvent.BUS.addListener(e->{e.register(thaumcraft.warp.WarpCreatures.GUARDIAN.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,EldritchSpawning::elder,SpawnPlacementRegisterEvent.Operation.REPLACE);for(var k:java.util.List.of(EldritchMob.Kind.BRAINY,EldritchMob.Kind.GIANT,EldritchMob.Kind.INHABITED))e.register(EldritchContent.ZOMBIES.get(k).get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,EldritchSpawning::zombie,SpawnPlacementRegisterEvent.Operation.REPLACE);e.register(EldritchContent.FIREBAT.get(),SpawnPlacementTypes.NO_RESTRICTIONS,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,EldritchSpawning::bat,SpawnPlacementRegisterEvent.Operation.REPLACE);});}
}
