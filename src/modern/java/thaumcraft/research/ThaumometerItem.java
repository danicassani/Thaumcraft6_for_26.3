package thaumcraft.research;

import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class ThaumometerItem extends Item {
    public ThaumometerItem(Properties properties){super(properties);}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){
        if(!level.isClientSide()&&!player.getCooldowns().isOnCooldown(player.getItemInHand(hand))){player.getCooldowns().addCooldown(player.getItemInHand(hand),20);ScanningService.scan(player);}return InteractionResult.SUCCESS;
    }
    @Override public InteractionResult useOn(UseOnContext context){return context.getPlayer()==null?InteractionResult.PASS:use(context.getLevel(),context.getPlayer(),context.getHand());}
    @Override public InteractionResult interactLivingEntity(ItemStack stack,Player player,LivingEntity entity,InteractionHand hand){return use(player.level(),player,hand);}
}
