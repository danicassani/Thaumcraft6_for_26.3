package thaumcraft.bore;
import java.util.OptionalInt;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.*;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
/** Isolated protection-event actor. The owner's hands and inventory are never changed. */
final class BoreActor extends ServerPlayer {
        BoreActor(ServerLevel level,GameProfile profile){super(level.getServer(),level,profile,ClientInformation.createDefault());
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
