package thaumcraft.taint;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.*;
public final class GooBlock extends Block{
 public static final IntegerProperty QUANTA=IntegerProperty.create("quanta",1,8);
 public GooBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(QUANTA,8));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(QUANTA);}
 protected RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
 protected FluidState getFluidState(BlockState s){return TaintContent.GOO_FLUID.get().defaultFluidState().setValue(FlowingFluid.LEVEL,s.getValue(QUANTA));}
 protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moved){super.onPlace(s,l,p,old,moved);l.scheduleTick(p,TaintContent.GOO_FLUID.get(),10);}
 protected void entityInside(BlockState s,Level l,BlockPos p,Entity e,InsideBlockEffectApplier effects,boolean precise){if(!(l instanceof ServerLevel server))return;int q=s.getValue(QUANTA);if(e instanceof ThaumicSlime slime&&slime.getSize()<q-1&&l.getRandom().nextBoolean()){if(set(server,p,q-1))slime.setSize(slime.getSize()+1,true);}else if(e instanceof LivingEntity living){living.setDeltaMovement(living.getDeltaMovement().multiply(1-q/8D,1,1-q/8D));living.addEffect(new MobEffectInstance(TaintContent.EXHAUST.getHolder().orElseThrow(),600,(q-1)/3,true,true));}}
 private static boolean set(ServerLevel l,BlockPos p,int q){return TaintWorld.place(l,p,p,q>0?TaintContent.GOO.get().defaultBlockState().setValue(QUANTA,q):Blocks.AIR.defaultBlockState(),null);}
 public static int amount(Level l,BlockPos p){if(!l.isInWorldBounds(p)||!l.hasChunkAt(p))return -1;var s=l.getBlockState(p);return s.is(TaintContent.GOO.get())?s.getValue(QUANTA):s.canBeReplaced()&&s.getFluidState().isEmpty()&&l.getBlockEntity(p)==null?0:-1;}
 public static boolean transfer(ServerLevel l,BlockPos from,BlockPos to,int units){int a=amount(l,from),b=amount(l,to);if(a<1||b<0||units<1||units>a||units+b>8)return false;var target=TaintContent.GOO.get().defaultBlockState().setValue(QUANTA,b+units);var event=new net.minecraftforge.event.level.BlockEvent.FluidPlaceBlockEvent(l,to,from,target);if(net.minecraftforge.event.level.BlockEvent.FluidPlaceBlockEvent.BUS.post(event)||!event.getNewState().equals(target))return false;var source=units==a?Blocks.AIR.defaultBlockState():TaintContent.GOO.get().defaultBlockState().setValue(QUANTA,a-units);var sourceEvent=new net.minecraftforge.event.level.BlockEvent.FluidPlaceBlockEvent(l,from,from,source);if(net.minecraftforge.event.level.BlockEvent.FluidPlaceBlockEvent.BUS.post(sourceEvent)||!sourceEvent.getNewState().equals(source))return false;l.setBlockAndUpdate(to,target);l.setBlockAndUpdate(from,source);return true;}
 public static void step(ServerLevel l,BlockPos p,RandomSource r){if(!l.getBlockState(p).is(TaintContent.GOO.get()))return;int q=amount(l,p);if(q>=3&&l.hasChunkAt(p.above())&&l.getBlockState(p.above()).isAir()&&r.nextInt(50)==0&&!thaumcraft.config.ThaumcraftConfig.wussMode.get()){var slime=new ThaumicSlime(TaintContent.SLIME.get(),l);slime.setSize(q>=7?2:1,true);slime.setPos(p.getX()+.5,p.getY(),p.getZ()+.5);if(l.noCollision(slime)&&l.addFreshEntity(slime)){if(TaintWorld.place(l,p,p,Blocks.AIR.defaultBlockState(),null))return;slime.discard();}}
  if(r.nextInt(4)==0){if(q==1&&!r.nextBoolean())TaintWorld.place(l,p,p,TaintWorld.state("taint_fibre"),null);else {var next=q>1?TaintContent.GOO.get().defaultBlockState().setValue(QUANTA,TaintRules.gooAfterDecay(q)):Blocks.AIR.defaultBlockState();if(TaintWorld.place(l,p,p,next,null))thaumcraft.aura.AuraManager.addFlux(l,p,1);}}
  else {int down=amount(l,p.below());if(down>=0&&down<8)transfer(l,p,p.below(),Math.min(q,8-down));else {var dirs=new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST};int start=r.nextInt(4);for(int n=0;n<4;n++){var at=p.relative(dirs[(start+n)%4]);int a=amount(l,at);if(a>=0&&q-a>=2&&transfer(l,p,at,(q-a)/2))break;}}}
  if(l.getBlockState(p).is(TaintContent.GOO.get()))l.scheduleTick(p,TaintContent.GOO_FLUID.get(),10);
 }
}
