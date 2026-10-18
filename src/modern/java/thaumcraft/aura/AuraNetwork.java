package thaumcraft.aura;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

/** Server-to-client only. Clients cannot write or request mutations to world aura. */
public final class AuraNetwork {
    public record Snapshot(Identifier dimension, long chunk, AuraCell cell) {}
    private static final SimpleChannel CHANNEL = ChannelBuilder.named("thaumcraft:aura").networkProtocolVersion(1).simpleChannel();
    private static Snapshot clientSnapshot;
    private AuraNetwork() {}
    public static void register() {
        CHANNEL.messageBuilder(Snapshot.class, NetworkDirection.PLAY_TO_CLIENT)
            .encoder((message, buffer) -> {
                buffer.writeIdentifier(message.dimension()); buffer.writeLong(message.chunk());
                buffer.writeVarInt(message.cell().base()); buffer.writeFloat(message.cell().vis()); buffer.writeFloat(message.cell().flux());
            })
            .decoder(buffer -> new Snapshot(buffer.readIdentifier(), buffer.readLong(), new AuraCell(buffer.readVarInt(), buffer.readFloat(), buffer.readFloat())))
            .consumerMainThread((message, context) -> clientSnapshot = message).add().build();
        TickEvent.PlayerTickEvent.Post.BUS.addListener(event -> {
            if (event.player() instanceof ServerPlayer player && player.tickCount % 20 == 0) {
                var cell = AuraManager.get(player.level(), player.blockPosition());
                if (cell != null) CHANNEL.send(new Snapshot(player.level().dimension().identifier(), player.chunkPosition().pack(), cell), PacketDistributor.PLAYER.with(player));
            }
        });
    }
    public static Snapshot clientSnapshot() { return clientSnapshot; }
    public static void clearClient() { clientSnapshot = null; }
}
