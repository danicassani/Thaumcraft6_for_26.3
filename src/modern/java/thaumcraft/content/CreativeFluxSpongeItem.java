package thaumcraft.content;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import thaumcraft.aura.AuraCell;
import thaumcraft.aura.AuraManager;

public final class CreativeFluxSpongeItem extends Item {
    public CreativeFluxSpongeItem(Properties properties) { super(properties); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.getAbilities().instabuild) return InteractionResult.FAIL;
        if (level instanceof ServerLevel server) {
            float drained = clearArea(server, player.blockPosition());
            player.sendSystemMessage(Component.translatable("item.thaumcraft.creative_flux_sponge.drained", String.format(java.util.Locale.ROOT, "%.2f", drained)));
        }
        return InteractionResult.SUCCESS;
    }
    /** Clears loaded chunks in the original 9x9 area, without terrain generation. */
    public static float clearArea(ServerLevel level, BlockPos center) {
        float total = 0;
        for(var rift:level.getEntitiesOfClass(thaumcraft.rifts.FluxRiftEntity.class,new net.minecraft.world.phys.AABB(center).inflate(64)))rift.discard();
        for (int x = -4; x <= 4; x++) for (int z = -4; z <= 4; z++)
            total += AuraManager.drainFlux(level, center.offset(x * 16, 0, z * 16), AuraCell.MAX, false);
        return total;
    }
}
