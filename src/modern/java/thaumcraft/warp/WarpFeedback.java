package thaumcraft.warp;
import net.minecraftforge.network.*;
import net.minecraft.server.level.ServerPlayer;
/** Private audiovisual signals; no world-changing request is accepted from clients. */
public final class WarpFeedback {
 public record Pulse(int kind){public Pulse{if(kind<1||kind>2)throw new IllegalArgumentException("Invalid warp signal");}}
 public static volatile int pending;
 private static final SimpleChannel CHANNEL=ChannelBuilder.named("thaumcraft:warp_feedback").networkProtocolVersion(1).simpleChannel();
 public static void register(){CHANNEL.messageBuilder(Pulse.class,NetworkDirection.PLAY_TO_CLIENT).encoder((m,b)->b.writeVarInt(m.kind())).decoder(b->new Pulse(b.readVarInt())).consumerMainThread((m,c)->pending=m.kind()).add().build();}
 public static void send(ServerPlayer p,int kind){if(p.connection!=null)CHANNEL.send(new Pulse(kind),PacketDistributor.PLAYER.with(p));}
}
