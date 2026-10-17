package thaumcraft.persistence;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;

/** Explicit development fixture; excluded from normal builds and normal runs. */
@Mod.EventBusSubscriber(modid = Thaumcraft.MODID)
public final class WarpRestartProbe {
    private static final UUID ID = UUID.fromString("8730b78e-cd56-4274-8e81-c7084776a7c9");
    private static final PlayerWarpStore.State EXPECTED = new PlayerWarpStore.State(1, 21, 34, 55, 89);
    private WarpRestartProbe() {}

    @SubscribeEvent
    public static void started(ServerStartedEvent event) {
        String phase = System.getenv("TC_WARP_RESTART_PROBE");
        if (!"write".equals(phase) && !"read".equals(phase))
            throw new IllegalStateException("Probe needs TC_WARP_RESTART_PROBE=write or read");
        var server = event.getServer();
        var player = new ServerPlayer(server, server.overworld(), new GameProfile(ID, "WarpProbe"), ClientInformation.createDefault());
        var storage = server.getPlayerList().getPlayerIo();
        if (phase.equals("write")) {
            require(storage.load(new NameAndId(ID, "WarpProbe")).isEmpty(), "Use a fresh probe world for write phase");
            require(PlayerWarpStore.read(player).getOrThrow().equals(PlayerWarpStore.State.EMPTY), "Missing data default failed");
            require(player.getPersistentData().get(PlayerWarpStore.ID) == null, "Read created persistent state");
            PlayerWarpStore.write(player, EXPECTED).getOrThrow();
            storage.save(player);
            require(storage.load(new NameAndId(ID, "WarpProbe")).isPresent(), "Player data was not saved");
        } else {
            var saved = storage.load(new NameAndId(ID, "WarpProbe")).orElseThrow();
            player.load(TagValueInput.create(ProblemReporter.DISCARDING, server.registryAccess(), saved));
            require(PlayerWarpStore.read(player).getOrThrow().equals(EXPECTED), "Warp did not survive world restart");
            var replacement = new ServerPlayer(server, server.overworld(), new GameProfile(ID, "WarpProbe"), ClientInformation.createDefault());
            PlayerEvent.Clone.BUS.post(new PlayerEvent.Clone(replacement, player, true));
            require(PlayerWarpStore.read(replacement).getOrThrow().equals(EXPECTED), "Clone listener lost warp");
            var invalid = StringTag.valueOf("invalid-probe-data");
            replacement.getPersistentData().put(PlayerWarpStore.ID, invalid);
            require(PlayerWarpStore.read(replacement).error().isPresent(), "Invalid data was accepted");
            require(PlayerWarpStore.write(replacement, EXPECTED).error().isPresent(), "Invalid data was overwritten");
            require(replacement.getPersistentData().get(PlayerWarpStore.ID).equals(invalid), "Invalid data was changed");
        }
        Thaumcraft.LOGGER.info("WARP_RESTART_PROBE_{}_OK", phase.toUpperCase(java.util.Locale.ROOT));
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
