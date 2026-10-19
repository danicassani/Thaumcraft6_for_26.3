package thaumcraft.thaumaturgy;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
public final class FocusPouch extends Item {
    public FocusPouch(Properties p){super(p.stacksTo(1));}
    public static List<ItemStack> contents(ItemStack pouch){var result=net.minecraft.core.NonNullList.withSize(18,ItemStack.EMPTY);pouch.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY).copyInto(result);return result;}
    public static void save(ItemStack pouch,List<ItemStack> slots){if(slots.size()!=18||slots.stream().anyMatch(s->!s.isEmpty()&&(!(s.getItem() instanceof FocusItem)||s.getCount()!=1)))throw new IllegalArgumentException("Invalid focus pouch contents");pouch.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(slots));}
    public static boolean cycle(Player player,ItemStack caster,ItemStack pouch){
        var slots=contents(pouch);var old=caster.get(ThaumaturgyContent.FOCUS.get());int start=Math.clamp(pouch.getOrDefault(ThaumaturgyContent.POUCH_CURSOR.get(),0),0,17);
        for(int j=0;j<18;j++){int i=(start+j)%18;var focus=slots.get(i);if(!(focus.getItem() instanceof FocusItem))continue;slots.set(i,old==null?ItemStack.EMPTY:old.copy());caster.set(ThaumaturgyContent.FOCUS.get(),focus.copy());save(pouch,slots);pouch.set(ThaumaturgyContent.POUCH_CURSOR.get(),(i+1)%18);player.getInventory().setChanged();player.inventoryMenu.broadcastChanges();return true;}return false;
    }
    public InteractionResult use(Level level,Player player,InteractionHand hand){if(hand!=InteractionHand.MAIN_HAND)return InteractionResult.PASS;if(player instanceof ServerPlayer p){int slot=p.getInventory().getSelectedSlot();var pouch=p.getMainHandItem();p.openMenu(new SimpleMenuProvider((id,inv,q)->new PouchMenu(id,inv,pouch,slot),Component.translatable("item.thaumcraft.focus_pouch")));}return InteractionResult.SUCCESS;}
}
