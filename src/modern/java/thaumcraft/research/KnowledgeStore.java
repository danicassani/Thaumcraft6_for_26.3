package thaumcraft.research;

import com.mojang.serialization.DataResult;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

/** Forge player data survives logout; cloning preserves the exact tag, including unsupported data. */
public final class KnowledgeStore {
    public static final String ID="thaumcraft:knowledge";
    private KnowledgeStore(){}
    public static DataResult<KnowledgeState> read(Player player){
        var tag=player.getPersistentData().get(ID);return tag==null?DataResult.success(KnowledgeState.EMPTY):KnowledgeState.CODEC.parse(NbtOps.INSTANCE,tag);
    }
    public static boolean write(Player player,KnowledgeState state){
        if(!(player.level() instanceof ServerLevel level)||!level.getServer().isSameThread())throw new IllegalStateException("Knowledge mutation outside server thread");
        // Never overwrite a corrupt or future-version save with an empty/default state.
        return read(player).flatMap(old->KnowledgeState.CODEC.encodeStart(NbtOps.INSTANCE,state)).map(tag->{player.getPersistentData().put(ID,tag);return true;}).result().orElse(false);
    }
    public static void register(){PlayerEvent.Clone.BUS.addListener(event->{var tag=event.getOriginal().getPersistentData().get(ID);if(tag!=null)event.getEntity().getPersistentData().put(ID,tag.copy());});}
}
