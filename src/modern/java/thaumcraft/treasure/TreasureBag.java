package thaumcraft.treasure;

import java.util.ArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.*;
import thaumcraft.content.SimpleContent;

public final class TreasureBag extends Item {
    private final int tier;
    public TreasureBag(int tier,Properties properties) { super(properties);this.tier=tier; }
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        var stack=player.getItemInHand(hand);
        if (stack.isEmpty() || !stack.is(this)) return InteractionResult.PASS;
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        var key=ResourceKey.create(Registries.LOOT_TABLE,SimpleContent.id("treasure/bag_"+TreasureContent.TIERS[tier]));
        var table=server.getServer().reloadableRegistries().getLootTable(key);
        if (table==LootTable.EMPTY) return InteractionResult.FAIL;
        var params=new LootParams.Builder(server).withParameter(LootContextParams.ORIGIN,player.position())
            .withParameter(LootContextParams.THIS_ENTITY,player).create(LootContextParamSets.CHEST);
        var rewards=table.getRandomItems(params);
        var spawned=new ArrayList<ItemEntity>();
        for (var reward:rewards) {
            if (reward.isEmpty()) continue;
            var entity=new ItemEntity(server,player.getX(),player.getY(),player.getZ(),reward.copy());
            entity.setDefaultPickUpDelay();
            if (!server.addFreshEntity(entity)) {
                spawned.forEach(ItemEntity::discard);
                return InteractionResult.FAIL;
            }
            spawned.add(entity);
        }
        // The original consumes bags even in creative. Client packets never roll loot.
        stack.shrink(1);
        server.playSound(null,player.blockPosition(),TreasureContent.COINS.get(),SoundSource.PLAYERS,.75F,1);
        return InteractionResult.SUCCESS;
    }
}
