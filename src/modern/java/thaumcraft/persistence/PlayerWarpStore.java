package thaumcraft.persistence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

/** Server-authoritative storage only. No warp mechanics, packets, or legacy conversion. */
public final class PlayerWarpStore {
    public static final String ID = "thaumcraft:warp";
    public static final int FORMAT_VERSION = 1;

    public record State(int version, int permanent, int normal, int temporary, int counter) {
        public static final State EMPTY = new State(FORMAT_VERSION, 0, 0, 0, 0);
        public State {
            if (version != FORMAT_VERSION || permanent < 0 || permanent > 500
                    || normal < 0 || normal > 500 || temporary < 0 || temporary > 500) {
                throw new IllegalArgumentException("Unsupported or invalid Thaumcraft warp state");
            }
        }
    }

    public static final Codec<State> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.intRange(FORMAT_VERSION, FORMAT_VERSION).fieldOf("version").forGetter(State::version),
        Codec.intRange(0, 500).fieldOf("permanent").forGetter(State::permanent),
        Codec.intRange(0, 500).fieldOf("normal").forGetter(State::normal),
        Codec.intRange(0, 500).fieldOf("temporary").forGetter(State::temporary),
        Codec.INT.fieldOf("counter").forGetter(State::counter)
    ).apply(instance, State::new));

    private PlayerWarpStore() {}

    /** Missing is empty; corrupt or future-version data is an explicit error, never rewritten. */
    public static DataResult<State> read(ServerPlayer player) {
        var tag = player.getPersistentData().get(ID);
        return tag == null ? DataResult.success(State.EMPTY) : CODEC.parse(NbtOps.INSTANCE, tag);
    }

    public static DataResult<State> write(ServerPlayer player, State state) {
        return read(player).flatMap(previous -> CODEC.encodeStart(NbtOps.INSTANCE, state).map(tag -> {
            player.getPersistentData().put(ID, tag);
            return state;
        }));
    }

    public static void registerEvents() {
        // Global gameplay event, not a lifecycle event on the mod bus.
        PlayerEvent.Clone.BUS.addListener(PlayerWarpStore::copyOnClone);
    }

    private static void copyOnClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer original
                && event.getEntity() instanceof ServerPlayer replacement) {
            var tag = original.getPersistentData().get(ID);
            if (tag != null) replacement.getPersistentData().put(ID, tag.copy());
        }
    }
}
