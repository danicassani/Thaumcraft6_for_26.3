package thaumcraft.thaumaturgy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.*;

/** Clients send an action, never dimensions or arbitrary focus data. */
public final class PlanControls {
    public record Selection(int x,int y,int z,int axis){
        public static final Codec<Selection> CODEC=RecordCodecBuilder.create(i->i.group(Codec.intRange(0,3).fieldOf("x").forGetter(Selection::x),Codec.intRange(0,3).fieldOf("y").forGetter(Selection::y),Codec.intRange(0,3).fieldOf("z").forGetter(Selection::z),Codec.intRange(0,3).fieldOf("axis").forGetter(Selection::axis)).apply(i,Selection::new));
        public Selection next(boolean axisOnly){if(axisOnly)return new Selection(x,y,z,(axis+1)%4);int size=((axis==1?x:axis==2?z:axis==3?y:Math.max(x,Math.max(y,z)))+1)%4;return new Selection(axis==0||axis==1?size:x,axis==0||axis==3?size:y,axis==0||axis==2?size:z,axis);}
    }
    public record Request(boolean axisOnly){}
    private static final SimpleChannel CHANNEL=ChannelBuilder.named("thaumcraft:plan_controls").networkProtocolVersion(1).simpleChannel();
    private static final java.util.Map<java.util.UUID,Long> LAST=new java.util.HashMap<>();
    public static void register(){CHANNEL.messageBuilder(Request.class,NetworkDirection.PLAY_TO_SERVER).encoder((m,b)->b.writeBoolean(m.axisOnly())).decoder(b->new Request(b.readBoolean())).consumerMainThread((m,c)->{var p=c.getSender();if(p!=null)adjust(p,m.axisOnly());}).add().build();net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent.BUS.addListener(e->LAST.remove(e.getEntity().getUUID()));net.minecraftforge.event.server.ServerStoppedEvent.BUS.addListener(e->LAST.clear());}
    public static void request(boolean axisOnly){CHANNEL.send(new Request(axisOnly),PacketDistributor.SERVER.noArg());}
    public static FocusProgram.PlanSettings effective(ItemStack caster,FocusProgram program){var a=caster.get(ThaumaturgyContent.PLAN_SELECTION.get());return a==null?program.plan():new FocusProgram.PlanSettings(a.x,a.y,a.z,program.plan().surface());}
    public static boolean adjust(ServerPlayer p,boolean axisOnly){
        var caster=p.getMainHandItem();var focus=caster.get(ThaumaturgyContent.FOCUS.get());var program=focus==null?null:focus.get(ThaumaturgyContent.PROGRAM.get());
        if(!(caster.getItem() instanceof CasterItem)||!p.isAlive()||p.isSpectator()||p.containerMenu!=p.inventoryMenu||program==null||!program.valid()||!program.medium().equals("plan")||!program.knows(p))return false;
        long now=p.level().getGameTime();var last=LAST.get(p.getUUID());if(last!=null&&now>=last&&now-last<2)return false;LAST.put(p.getUUID(),now);
        var a=effective(caster,program);var old=caster.get(ThaumaturgyContent.PLAN_SELECTION.get());var next=new Selection(a.x(),a.y(),a.z(),old==null?0:old.axis()).next(axisOnly);caster.set(ThaumaturgyContent.PLAN_SELECTION.get(),next);p.inventoryMenu.broadcastChanges();
        p.sendSystemMessage(Component.translatable("caster.thaumcraft.plan_size",next.x,next.y,next.z,Component.translatable("caster.thaumcraft.axis_"+next.axis)),true);return true;
    }
}
