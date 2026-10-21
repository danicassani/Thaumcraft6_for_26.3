package thaumcraft.equipment;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import thaumcraft.api.items.IRechargable;
/** Right click equips a typed slot; shift-right click opens all seven slots. */
public final class AccessoryItem extends Item implements IRechargable {
    public AccessoryItem(Properties p){super(p.stacksTo(1));}
    public int getMaxCharge(ItemStack s,LivingEntity e){return EquipmentContent.name(s).startsWith("charm_verdant")?200:0;}
    public InteractionResult use(Level l,Player p,InteractionHand h){if(h!=InteractionHand.MAIN_HAND)return InteractionResult.PASS;if(p instanceof net.minecraft.server.level.ServerPlayer real){if(p.isShiftKeyDown())AccessoryMenuAccess.open(real);else if(!AccessorySlots.equip(real,p.getMainHandItem()))return InteractionResult.FAIL;}return InteractionResult.SUCCESS;}
}
