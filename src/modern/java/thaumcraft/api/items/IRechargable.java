package thaumcraft.api.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Modern port of the original item contract. The entity may be null for pedestals. */
public interface IRechargable {
    int getMaxCharge(ItemStack stack, LivingEntity entity);
    default EnumChargeDisplay showInHud(ItemStack stack, LivingEntity entity) { return EnumChargeDisplay.NORMAL; }
    enum EnumChargeDisplay { NEVER, NORMAL, PERIODIC }
}
