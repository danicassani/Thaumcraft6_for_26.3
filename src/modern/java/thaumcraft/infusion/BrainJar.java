package thaumcraft.infusion;
import com.mojang.serialization.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
public final class BrainJar extends thaumcraft.devices.AuraDeviceEntity{
    private int xp,delay;private Tag unsupported;
    public BrainJar(BlockPos p,BlockState s){super(InfusionContent.BRAIN_TYPE.get(),p,s);}
    public int xp(){return xp;}
    public boolean supported(){return unsupported==null;}
    private void changed(){sync();if(level!=null)level.updateNeighbourForOutputSignal(worldPosition,getBlockState().getBlock());}
    /** Merged XP orbs encode a multiplicity; account for it before removing the entity. */
    public boolean absorb(ExperienceOrb orb){
        if(!(level instanceof ServerLevel server)||!supported()||delay>0||xp>=2000||!orb.isAlive()||orb.level()!=level)return false;
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,server.registryAccess());orb.saveWithoutId(out);int count=out.buildResult().getIntOr("Count",1);long total=(long)count*orb.getValue();if(total<1||total>Integer.MAX_VALUE)return false;int take=(int)Math.min(2000-xp,total);xp+=take;orb.discard();if(total>take)ExperienceOrb.award(server,orb.position(),(int)total-take);changed();return true;
    }
    @Override public void serverTick(){if(!(level instanceof ServerLevel server)||!supported()||!server.tickRateManager().runsNormally())return;if(delay>0){delay--;setChanged();return;}if(xp>=2000)return;var area=new AABB(worldPosition).inflate(8);var orbs=server.getEntitiesOfClass(ExperienceOrb.class,area,ExperienceOrb::isAlive);ExperienceOrb closest=null;double distance=Double.MAX_VALUE;for(var orb:orbs){double d=orb.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5);if(d<distance){closest=orb;distance=d;}if(new AABB(worldPosition).inflate(.1).contains(orb.position()))absorb(orb);}if(closest!=null&&closest.isAlive()&&xp<2000){var delta=Vec3.atCenterOf(worldPosition).subtract(closest.position());double length=delta.length();if(length>.001)closest.setDeltaMovement(closest.getDeltaMovement().add(delta.scale(.15/length)));}}
    public InteractionResult interact(Player p){if(p.getMainHandItem().is(thaumcraft.research.ResearchContent.METER.get()))return InteractionResult.PASS;if(level.isClientSide())return InteractionResult.SUCCESS;if(!supported()||p.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)>64)return InteractionResult.FAIL;if(p.getMainHandItem().is(thaumcraft.essentia.EssentiaContent.RESONATOR.get())){p.sendSystemMessage(net.minecraft.network.chat.Component.translatable("infusion.thaumcraft.brain_xp",xp,2000));return InteractionResult.SUCCESS;}int amount=level.getRandom().nextInt(Math.min(xp+1,64));delay=40;if(amount>0){xp-=amount;ExperienceOrb.award((ServerLevel)level,Vec3.atCenterOf(worldPosition).add(0,.5,0),amount);}changed();return InteractionResult.SUCCESS;}
    public ItemStack drop(){var s=new ItemStack(InfusionContent.BRAIN_JAR.get());if(supported()&&xp>0)s.set(InfusionContent.BRAIN_XP.get(),xp);return s;}
    public void place(ItemStack s){if(supported()){xp=s.getOrDefault(InfusionContent.BRAIN_XP.get(),0);changed();}}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.store("brain_xp",Codec.PASSTHROUGH,new Dynamic<>(NbtOps.INSTANCE,unsupported==null?IntTag.valueOf(xp):unsupported));out.putInt("eat_delay",delay);}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);xp=0;delay=Math.clamp(in.getIntOr("eat_delay",0),0,40);unsupported=null;var raw=in.read("brain_xp",Codec.PASSTHROUGH);if(raw.isPresent()){var tag=raw.get().convert(NbtOps.INSTANCE).getValue();var parsed=Codec.intRange(0,2000).parse(NbtOps.INSTANCE,tag).result();if(parsed.isPresent())xp=parsed.get();else unsupported=tag.copy();}}
}
