package thaumcraft.api.items;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aura.AuraManager;
import thaumcraft.devices.DeviceContent;

/** Integer vis charge with exact aura debit: fractional vis is never lost by truncation. */
public final class RechargeHelper {
    private RechargeHelper() {}
    public static int getCharge(ItemStack stack) { return stack.getItem() instanceof IRechargable ? Math.clamp(stack.getOrDefault(DeviceContent.CHARGE.get(), 0), 0, 32766) : -1; }
    public static int rechargeItem(ServerLevel level, ItemStack stack, BlockPos pos, LivingEntity entity, int amount) {
        if (amount <= 0 || !(stack.getItem() instanceof IRechargable item)) return 0;
        int remaining = Math.max(0, Math.clamp(item.getMaxCharge(stack, entity), 0, 32766) - getCharge(stack));
        int available = (int)AuraManager.drainVis(level, pos, Math.min(amount, remaining), true);
        if (available <= 0) return 0;
        int spent = (int)AuraManager.drainVis(level, pos, available, false);
        stack.set(DeviceContent.CHARGE.get(), getCharge(stack) + spent);
        return spent;
    }
    public static boolean consumeCharge(ItemStack stack, LivingEntity entity, int amount) {
        if (amount < 0 || !(stack.getItem() instanceof IRechargable) || getCharge(stack) < amount) return false;
        stack.set(DeviceContent.CHARGE.get(), getCharge(stack) - amount); return true;
    }
}
