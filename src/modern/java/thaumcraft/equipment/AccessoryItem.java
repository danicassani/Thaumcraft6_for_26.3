package thaumcraft.equipment;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import thaumcraft.api.items.IRechargable;
/** Native off-hand equipment: only the equipped stack grants its effect; never passive inventory copies. */
public final class AccessoryItem extends Item implements IRechargable {
    public AccessoryItem(Properties p){super(p.stacksTo(1));}
    public int getMaxCharge(ItemStack s,LivingEntity e){return EquipmentContent.name(s).startsWith("charm_verdant")?200:0;}
    public InteractionResult use(Level l,Player p,InteractionHand h){if(h!=InteractionHand.MAIN_HAND)return InteractionResult.PASS;if(!l.isClientSide()){var item=p.getMainHandItem();var old=p.getOffhandItem();p.setItemInHand(InteractionHand.OFF_HAND,item);p.setItemInHand(InteractionHand.MAIN_HAND,old);}return InteractionResult.SUCCESS;}
}
