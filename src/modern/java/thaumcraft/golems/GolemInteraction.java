package thaumcraft.golems;

import java.util.OptionalInt;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.Direction;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.*;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;

/** An unregistered survival actor with a separate inventory; the real owner is never modified. */
public final class GolemInteraction {
    public static int slot(WorkerGolem g,SealEntity seal){for(int i=0;i<g.design().capacity();i++)if(seal.matches(g.cargo.getItem(i)))return i;return -1;}
    public static Vec3 stand(SealEntity seal,WorkerGolem g){var target=seal.useTarget();var face=seal.getBlockState().getValue(SealBlock.FACING);int x=face.getStepX(),z=face.getStepZ();if(x==0&&z==0){int dx=g.home().getX()-target.getX(),dz=g.home().getZ()-target.getZ();if(Math.abs(dx)>=Math.abs(dz))x=dx>0?1:-1;else z=dz>0?1:-1;}return new Vec3(target.getX()+.5+x*2,face.getStepY()==0?target.getY():g.getY(),target.getZ()+.5+z*2);}
    public static boolean click(ServerLevel level,ServerPlayer owner,WorkerGolem g,SealEntity seal){
        var q=seal.useAir&&!seal.useLeft?seal.useTarget().below():seal.useTarget();var face=seal.useAir&&!seal.useLeft?Direction.UP:seal.getBlockState().getValue(SealBlock.FACING);int slot=seal.useEmpty?-1:slot(g,seal);
        if(!seal.useReady()||!owner.mayBuild()||!level.mayInteract(owner,q)||!level.mayInteract(owner,q.relative(face))||(!seal.useEmpty&&slot<0))return false;
        var held=slot<0?ItemStack.EMPTY:g.cargo.getItem(slot);
        if(!owner.mayUseItemAt(q,face,held)||!owner.mayUseItemAt(q.relative(face),face,held))return false;
        var hit=new BlockHitResult(Vec3.atCenterOf(q).add(new Vec3(face.getStepX(),face.getStepY(),face.getStepZ()).scale(.501)),face,q,false);
        if(!thaumcraft.thaumaturgy.SpellEngine.loadedLine(level,g.getEyePosition(),hit.getLocation()))return false;
        var obstruction=level.clip(new net.minecraft.world.level.ClipContext(g.getEyePosition(),hit.getLocation(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,g));
        if(obstruction.getType()==HitResult.Type.BLOCK&&!obstruction.getBlockPos().equals(q))return false;
        var actor=new Actor(level,new GameProfile(g.getUUID(),"[TCGolem]"));actor.setPos(g.position());actor.setYRot(g.getYRot());actor.setXRot(g.getXRot());actor.setShiftKeyDown(seal.useSneak);
        // Move, rather than copy, the cargo. Every generated container/tool remainder is returned in finally.
        if(slot>=0)actor.setItemInHand(InteractionHand.MAIN_HAND,g.cargo.removeItemNoUpdate(slot));
        try{
            if(seal.useLeft){var state=level.getBlockState(q);if(state.isAir()||state.getDestroySpeed(level,q)<0)return false;var event=net.minecraftforge.event.ForgeEventFactory.onLeftClickBlock(actor,q,face,ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK);if(event==null||event.getUseBlock().isDenied()||event.getUseItem().isDenied())return false;state.attack(level,q,actor);return actor.gameMode.destroyBlock(q);}
            return actor.gameMode.useItemOn(actor,level,actor.getMainHandItem(),InteractionHand.MAIN_HAND,hit).consumesAction();
        }finally{
            for(int i=0;i<actor.getInventory().getContainerSize();i++){var stack=actor.getInventory().removeItemNoUpdate(i);var rest=g.insertCargo(stack,false);if(!rest.isEmpty())level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level,g.getX(),g.getY(),g.getZ(),rest));}
            actor.getTextFilter().leave();
        }
    }
    public static boolean plant(ServerLevel l,ServerPlayer owner,WorkerGolem worker,int slot,net.minecraft.core.BlockPos at){return plant(l,owner,worker,slot,at,at.below(),Direction.UP);}
    public static boolean plant(ServerLevel l,ServerPlayer owner,WorkerGolem worker,int slot,net.minecraft.core.BlockPos at,net.minecraft.core.BlockPos support,Direction face){if(!owner.mayBuild()||!l.hasChunkAt(at)||!l.hasChunkAt(support)||!l.mayInteract(owner,at)||!l.mayInteract(owner,support)||!owner.mayUseItemAt(support,face,worker.cargo.getItem(slot)))return false;var actor=new Actor(l,new GameProfile(worker.getUUID(),"[TCGolem]"));actor.setPos(worker.position());actor.setItemInHand(InteractionHand.MAIN_HAND,worker.cargo.removeItemNoUpdate(slot));try{return actor.gameMode.useItemOn(actor,l,actor.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(support).add(face.getStepX()*.501,face.getStepY()*.501,face.getStepZ()*.501),face,support,false)).consumesAction();}finally{for(int i=0;i<actor.getInventory().getContainerSize();i++){var rest=worker.insertCargo(actor.getInventory().removeItemNoUpdate(i),false);if(!rest.isEmpty())net.minecraft.world.level.block.Block.popResource(l,at,rest);}actor.getTextFilter().leave();}}
    private static final class Actor extends ServerPlayer {
        Actor(ServerLevel level,GameProfile profile){super(level.getServer(),level,profile,ClientInformation.createDefault());
            new ServerGamePacketListenerImpl(level.getServer(),new net.minecraft.network.Connection(PacketFlow.SERVERBOUND),this,CommonListenerCookie.createInitial(profile,false)){
                @Override public void send(Packet<?> packet){}
                @Override public void send(Packet<?> packet,io.netty.channel.ChannelFutureListener listener){}
            };
            gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        }
        @Override public OptionalInt openMenu(MenuProvider provider){return OptionalInt.empty();}
        @Override public void sendSystemMessage(Component message,boolean overlay){}
        @Override public void sendOverlayMessage(Component message){}
        @Override protected ItemCooldowns createItemCooldowns(){return new ItemCooldowns();}
    }
    private GolemInteraction(){}
}
