package thaumcraft.thaumaturgy;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import thaumcraft.devices.AuraDeviceEntity;
public final class TurretEntity extends AuraDeviceEntity {
    private UUID owner;private int arrows,mode;private long fireAt;
    public TurretEntity(BlockPos p,BlockState s){super(ThaumaturgyContent.TURRET_TYPE.get(),p,s);}
    public int ammo(){return arrows;}public int mode(){return mode;}public UUID owner(){return owner;}public long fireAt(){return fireAt;}
    public boolean advanced(){return ((TurretBlock)getBlockState().getBlock()).advanced;}
    public void claim(Player p){if(owner==null){owner=p.getUUID();sync();}}
    public InteractionResult interact(Player p,ItemStack item){if(level.isClientSide())return InteractionResult.SUCCESS;if(owner==null||!owner.equals(p.getUUID())||p.distanceToSqr(Vec3.atCenterOf(worldPosition))>64)return InteractionResult.FAIL;
        if(item.is(Items.ARROW)){int count=Math.min(64-arrows,item.getCount());arrows+=count;item.consume(count,p);}
        else if(item.is(Items.STICK)&&advanced())mode=(mode+1)%3;
        else if(p.isShiftKeyDown()&&item.isEmpty()){if(arrows>0){var out=new ItemStack(Items.ARROW,arrows);if(!p.addItem(out))level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level,p.getX(),p.getY(),p.getZ(),out));arrows=0;}}
        else if(!item.isEmpty())return InteractionResult.PASS;
        p.sendOverlayMessage(Component.translatable("device.thaumcraft.turret_status",arrows,Component.translatable("device.thaumcraft.targets_"+mode)));sync();return InteractionResult.SUCCESS;
    }
    private boolean eligible(ServerPlayer p,LivingEntity e){return e.isAlive()&&!e.isSpectator()&&!SpellEngine.protectedTarget(p,e)&&(mode==0?e instanceof Enemy:mode==1?e instanceof Mob:e instanceof Player);}
    public void serverTick(){if(!(level instanceof ServerLevel l))return;var turret=new CrossbowTurret((advanced()?TurretContent.ADVANCED:TurretContent.BASIC).get(),l);turret.setPos(worldPosition.getX()+.5,worldPosition.getY(),worldPosition.getZ()+.5);turret.configure(owner,mode==0?2:mode==1?3:4);if(arrows>0)turret.ammunition.setItem(0,new ItemStack(Items.ARROW,arrows));if(l.addFreshEntity(turret)){arrows=0;setChanged();l.setBlockAndUpdate(worldPosition,Blocks.AIR.defaultBlockState());}}
    public void preRemoveSideEffects(BlockPos q,BlockState s){super.preRemoveSideEffects(q,s);if(level instanceof ServerLevel&&arrows>0){Block.popResource(level,q,new ItemStack(Items.ARROW,arrows));arrows=0;}}
    protected void saveAdditional(ValueOutput o){super.saveAdditional(o);if(owner!=null)o.putString("owner",owner.toString());o.putInt("arrows",arrows);o.putInt("mode",mode);o.putLong("fire_at",fireAt);}
    protected void loadAdditional(ValueInput i){super.loadAdditional(i);try{owner=UUID.fromString(i.getStringOr("owner",""));}catch(IllegalArgumentException e){owner=null;}arrows=Math.clamp(i.getIntOr("arrows",0),0,64);mode=advanced()?Math.clamp(i.getIntOr("mode",0),0,2):0;fireAt=i.getLongOr("fire_at",0);}
}
