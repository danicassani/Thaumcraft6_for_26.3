package thaumcraft.devices;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import thaumcraft.aura.AuraManager;
/** Original four beam ranges and 1200-unit vis reserve; applies ordinary collision-respecting motion. */
public final class LevitatorEntity extends AuraDeviceEntity {
    private int range=1;private float fuel;
    public LevitatorEntity(BlockPos p,BlockState s){super(DeviceContent.LEVITATOR_TYPE.get(),p,s);}
    public int range(){return 4<<range;}public int cost(){return range()*2;}public float fuel(){return fuel;}
    public void increaseRange(Player p){range=(range+1)%4;sync();p.sendSystemMessage(Component.translatable("block.thaumcraft.levitator.range",range(),cost()));}
    public void serverTick(){if(!(level instanceof ServerLevel l)||getBlockState().getValue(AuraDeviceBlock.POWERED))return;var face=getBlockState().getValue(AuraDeviceBlock.FACING);int reach=0;for(int i=1;i<=range();i++){var q=worldPosition.relative(face,i);if(!l.hasChunkAt(q)||!l.getBlockState(q).getCollisionShape(l,q).isEmpty())break;reach=i;}if(reach==0)return;
        var beam=new AABB(worldPosition.relative(face)).minmax(new AABB(worldPosition.relative(face,reach)));
        for(var e:l.getEntities((net.minecraft.world.entity.Entity)null,beam,x->!x.isSpectator()&&x.isAlive()&&(x instanceof ItemEntity||x instanceof LivingEntity&&x.isPushable()))){
            if(fuel<cost()){float need=(1200-fuel)/1200;float available=AuraManager.drainVis(l,worldPosition,need,true);if(fuel+available*1200+.0001F<cost())break;fuel+=AuraManager.drainVis(l,worldPosition,need,false)*1200;}
            fuel=Math.max(0,fuel-cost());var v=e.getDeltaMovement();if(e.isShiftKeyDown()&&face==Direction.UP){if(v.y<0)v=v.multiply(1,.9,1);}else{v=v.add(.1*face.getStepX(),.1*face.getStepY(),.1*face.getStepZ());if(face.getAxis()!=Direction.Axis.Y&&!e.onGround())v=new Vec3(v.x,(v.y<0?v.y*.9:v.y)+.08,v.z);v=new Vec3(Mth.clamp(v.x,-.35,.35),Mth.clamp(v.y,-.35,.35),Mth.clamp(v.z,-.35,.35));}e.setDeltaMovement(v);e.fallDistance=0;e.syncVelocity=true;setChanged();
        }
    }
    protected void saveAdditional(ValueOutput o){super.saveAdditional(o);o.putInt("range",range);o.putFloat("fuel",fuel);}
    protected void loadAdditional(ValueInput i){super.loadAdditional(i);range=Math.clamp(i.getIntOr("range",1),0,3);fuel=Mth.clamp(i.getFloatOr("fuel",0),0,1200);}
}
