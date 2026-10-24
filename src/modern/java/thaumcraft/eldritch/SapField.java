package thaumcraft.eldritch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
/** Native, saved scheduled ticks bound the Warden's original effectSap fields. */
public final class SapField extends Block {
 public SapField(Properties p){super(p);}
 protected RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
 protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
 protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moved){l.scheduleTick(p,this,250+l.getRandom().nextInt(150));}
 protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource r){l.removeBlock(p,false);}
 protected void entityInside(BlockState s,Level l,BlockPos p,Entity e,InsideBlockEffectApplier applier,boolean precise){if(l instanceof ServerLevel&& !thaumcraft.config.ThaumcraftConfig.wussMode.get()&&e instanceof LivingEntity living&&!(e instanceof EldritchMob)&&!(e instanceof thaumcraft.warp.WarpMonster)&&!(e instanceof net.minecraft.world.entity.player.Player player&&(player.isCreative()||player.isSpectator()))&&!living.hasEffect(MobEffects.WITHER)){living.addEffect(new MobEffectInstance(MobEffects.WITHER,40));living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,40,1));living.addEffect(new MobEffectInstance(MobEffects.HUNGER,40,1));}}
 public void animateTick(BlockState s,Level l,BlockPos p,RandomSource r){l.addParticle(ParticleTypes.WITCH,p.getX()+r.nextFloat(),p.getY()+.15,p.getZ()+r.nextFloat(),0,.02,0);}
 public static boolean place(ServerLevel l,BlockPos p){if(!l.hasChunkAt(p)||!l.isInWorldBounds(p)||!l.getWorldBorder().isWithinBounds(p)||!l.getBlockState(p).isAir()||l.getBlockEntity(p)!=null||!l.getBlockState(p.below()).isFaceSturdy(l,p.below(),net.minecraft.core.Direction.UP))return false;return l.setBlockAndUpdate(p,EldritchContent.SAP.get().defaultBlockState());}
}
