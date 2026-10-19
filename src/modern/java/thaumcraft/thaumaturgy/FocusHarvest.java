package thaumcraft.thaumaturgy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;

/** Native mining runs with a disposable spell tool; the actual held stack is never modified. */
public final class FocusHarvest {
    private FocusHarvest(){}
    public static boolean destroy(ServerPlayer player,BlockPos pos,FocusProgram.Effect effect){
        var held=player.getMainHandItem();
        var tool=new ItemStack(Items.NETHERITE_PICKAXE);
        var enchantments=player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        if(effect.silk())tool.enchant(enchantments.getOrThrow(Enchantments.SILK_TOUCH),1);
        if(effect.fortune()>0)tool.enchant(enchantments.getOrThrow(Enchantments.FORTUNE),effect.fortune());
        player.setItemInHand(InteractionHand.MAIN_HAND,tool);
        try{return player.gameMode.destroyBlock(pos);}
        finally{player.setItemInHand(InteractionHand.MAIN_HAND,held);}
    }
}
