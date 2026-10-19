package thaumcraft.equipment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;
import net.minecraftforge.event.TickEvent;
public final class CloudJump {
    public record Request(){}
    private static final SimpleChannel CHANNEL=ChannelBuilder.named("thaumcraft:cloud_jump").networkProtocolVersion(1).simpleChannel();
    public static void register(){CHANNEL.messageBuilder(Request.class,NetworkDirection.PLAY_TO_SERVER).encoder((m,b)->{}).decoder(b->new Request()).consumerMainThread((m,c)->{var p=c.getSender();if(p!=null)jump(p);}).add().build();TickEvent.PlayerTickEvent.Post.BUS.addListener(e->{if(e.player() instanceof ServerPlayer p&&(p.onGround()||p.isInWater()||p.onClimbable()))p.getPersistentData().putBoolean("tc_cloud_used",false);});}
    public static boolean jump(ServerPlayer p){if(!EquipmentContent.name(p.getOffhandItem()).equals("ring_cloud")||!p.isAlive()||p.isSpectator()||p.onGround()||p.isPassenger()||p.isInWater()||p.getAbilities().flying||p.getPersistentData().getBooleanOr("tc_cloud_used",false))return false;p.getPersistentData().putBoolean("tc_cloud_used",true);var motion=p.getDeltaMovement();p.setDeltaMovement(motion.x,.42,motion.z);p.syncVelocity=true;p.fallDistance=0;p.level().sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,p.getX(),p.getY(),p.getZ(),12,.3,.1,.3,.03);return true;}
    public static void request(){CHANNEL.send(new Request(),PacketDistributor.SERVER.noArg());}
}
