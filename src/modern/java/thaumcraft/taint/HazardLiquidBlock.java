package thaumcraft.taint;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
public final class HazardLiquidBlock extends LiquidBlock{
 public final TaintContent.FluidSet kind;
 public HazardLiquidBlock(TaintContent.FluidSet k,Properties p){super(k.source,p);kind=k;}
 protected void entityInside(BlockState s,Level level,BlockPos p,Entity e,InsideBlockEffectApplier effects,boolean precise){if(!(level instanceof ServerLevel l)||!(e instanceof LivingEntity living))return;living.setDeltaMovement(living.getDeltaMovement().multiply(.5,1,.5));if(kind==TaintContent.DEATH){living.hurtServer(l,TaintContent.damage(l,"dissolve"),TaintRules.deathDamage(Math.min(3,s.getValue(LEVEL)/2)));}else if(s.getValue(LEVEL)==0&&living instanceof ServerPlayer player&&!living.hasEffect(TaintContent.WARD.getHolder().orElseThrow())){var warp=thaumcraft.persistence.PlayerWarpStore.read(player).result();if(warp.isEmpty())return;if(!TaintWorld.place(l,p,p,Blocks.AIR.defaultBlockState(),player))return;living.addEffect(new MobEffectInstance(TaintContent.WARD.getHolder().orElseThrow(),TaintRules.wardTicks(warp.get().permanent()),0,true,true));}}
}
