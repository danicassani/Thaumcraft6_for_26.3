package thaumcraft.taint;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.level.Level;
/** Native slime sizing, collision damage, jumping, splitting and persistence. */
public final class ThaumicSlime extends Slime{
 public ThaumicSlime(EntityType<? extends Slime> t,Level l){super(t,l);}
 private int spit=100;
 public void setSize(int size,boolean heal){super.setSize(size,heal);getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(getSize()+1);xpReward=getSize()+2;}
 protected int getSplitCount(){return getSize();}
 protected void setUpSplitCube(net.minecraft.world.entity.monster.cubemob.AbstractCubeMob child,int half,float x,float z){super.setUpSplitCube(child,1,x,z);}
 protected boolean shouldDropLoot(){return getSize()>1;}
 public boolean considersEntityAsAlly(net.minecraft.world.entity.Entity other){return other instanceof TaintedMob||other instanceof ThaumicSlime||super.considersEntityAsAlly(other);}
 protected void customServerAiStep(net.minecraft.server.level.ServerLevel l){super.customServerAiStep(l);if(spit>0)spit--;var target=getTarget();if(getSize()>2&&spit==0&&target!=null&&distanceToSqr(target)>16&&distanceToSqr(target)<256&&getSensing().hasLineOfSight(target)){var child=new ThaumicSlime(TaintContent.SLIME.get(),l);child.setSize(1,true);child.setPos(getX(),getY()+getBbHeight()/2,getZ());var delta=target.getEyePosition().subtract(child.position());double horizontal=Math.sqrt(delta.x*delta.x+delta.z*delta.z);child.setDeltaMovement(delta.add(0,horizontal*.2,0).normalize().scale(1.5));if(l.noCollision(child)&&l.addFreshEntity(child)){setSize(getSize()-1,true);spit=101;}}}
 protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput out){super.addAdditionalSaveData(out);out.putInt("taint_spit",spit);}
 protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput in){super.readAdditionalSaveData(in);spit=Math.clamp(in.getIntOr("taint_spit",100),0,101);}
 public void tick(){if(!level().isClientSide()&&thaumcraft.config.ThaumcraftConfig.wussMode.get()){discard();return;}super.tick();}
}
