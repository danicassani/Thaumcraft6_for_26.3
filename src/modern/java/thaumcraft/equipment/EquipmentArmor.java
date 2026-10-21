package thaumcraft.equipment;
import net.minecraft.world.item.Item;
public final class EquipmentArmor extends Item {
 private final String name;
 public EquipmentArmor(Properties p,String name){super(p);this.name=name;}
 @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer){consumer.accept(thaumcraft.client.EquipmentArmorModels.extension());}
}
