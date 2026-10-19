package thaumcraft.thaumaturgy;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import thaumcraft.aura.AuraManager;
import thaumcraft.equipment.EquipmentContent;
import thaumcraft.research.ResearchService;
public final class CasterItem extends Item {
    public CasterItem(Properties p){super(p.stacksTo(1));}
    /** Sneak use exchanges actual stacks with the off hand, conserving each focus and its components. */
    public boolean swap(Player p,ItemStack caster){var held=p.getOffhandItem();if(!held.isEmpty()&&!(held.getItem() instanceof FocusItem))return false;var old=caster.get(ThaumaturgyContent.FOCUS.get());if(held.isEmpty()&&old==null)return false;if(held.isEmpty())caster.remove(ThaumaturgyContent.FOCUS.get());else caster.set(ThaumaturgyContent.FOCUS.get(),held.copyWithCount(1));p.setItemInHand(InteractionHand.OFF_HAND,old==null?ItemStack.EMPTY:old.copy());return true;}
    public InteractionResult use(Level level,Player player,InteractionHand hand){if(hand!=InteractionHand.MAIN_HAND)return InteractionResult.PASS;if(!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;var caster=p.getItemInHand(hand);if(p.isShiftKeyDown())return (p.getOffhandItem().getItem() instanceof FocusPouch?FocusPouch.cycle(p,caster,p.getOffhandItem()):swap(p,caster))?InteractionResult.SUCCESS:InteractionResult.FAIL;
        if(p.getCooldowns().isOnCooldown(caster))return InteractionResult.FAIL;var focus=caster.get(ThaumaturgyContent.FOCUS.get());var program=focus==null?null:focus.get(ThaumaturgyContent.PROGRAM.get());if(program==null||!program.valid()||!(focus.getItem() instanceof FocusItem f)||program.complexity()>f.capacity){p.sendSystemMessage(Component.translatable("caster.thaumcraft.no_focus"),true);return InteractionResult.FAIL;}
        if(!program.knows(p))return InteractionResult.FAIL;
        float cost=EquipmentContent.visCost(p,program.vis());if(AuraManager.drainVis(p.level(),p.blockPosition(),cost,true)<cost){p.sendSystemMessage(Component.translatable("caster.thaumcraft.no_vis"),true);return InteractionResult.FAIL;}AuraManager.drainVis(p.level(),p.blockPosition(),cost,false);p.getCooldowns().addCooldown(caster,program.cooldown());SpellEngine.cast(p,program);return InteractionResult.SUCCESS;
    }
    public InteractionResult useOn(UseOnContext c){if(c.getLevel().getBlockEntity(c.getClickedPos()) instanceof thaumcraft.infusion.InfusionMatrix matrix){if(c.getPlayer() instanceof ServerPlayer p)return matrix.interact(p,c.getHand());return InteractionResult.SUCCESS;}return InteractionResult.PASS;}
}
