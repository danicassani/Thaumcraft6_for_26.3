package thaumcraft.research;

import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class ThaumometerItem extends Item {
    public static final int SCAN_TICKS=20;
    private static final String OTHER_HAND="tc_scan_other_hand";
    public ThaumometerItem(Properties properties){super(properties);}
    @Override public int getUseDuration(ItemStack stack,LivingEntity user){return SCAN_TICKS;}
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack){return ItemUseAnimation.NONE;}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){
        if(!player.isAlive()||player.isSpectator())return InteractionResult.FAIL;
        player.getPersistentData().putBoolean(OTHER_HAND,player.isShiftKeyDown());
        player.startUsingItem(hand);if(!level.isClientSide())ScanEffects.start(player);return InteractionResult.CONSUME;
    }
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity user){
        if(user instanceof Player player){
            boolean other=player.getPersistentData().getBooleanOr(OTHER_HAND,false);
            player.getPersistentData().remove(OTHER_HAND);
            if(!level.isClientSide()){
                if(ScanningService.scan(player,other))level.playSound(null,player.getX(),player.getY(),player.getZ(),net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,net.minecraft.sounds.SoundSource.PLAYERS,.5F,1F);
            }
        }
        return stack;
    }
    @Override public boolean releaseUsing(ItemStack stack,Level level,LivingEntity user,int remaining){user.getPersistentData().remove(OTHER_HAND);return false;}
    @Override public InteractionResult useOn(UseOnContext context){return context.getPlayer()==null?InteractionResult.PASS:use(context.getLevel(),context.getPlayer(),context.getHand());}
    @Override public InteractionResult interactLivingEntity(ItemStack stack,Player player,LivingEntity entity,InteractionHand hand){return use(player.level(),player,hand);}
}
