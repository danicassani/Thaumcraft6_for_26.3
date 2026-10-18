package thaumcraft.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import thaumcraft.api.items.IRechargable;
import thaumcraft.api.items.RechargeHelper;

public final class RechargePedestalEntity extends AuraDeviceEntity {
    private boolean silent;
    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return stack.getItem() instanceof IRechargable; }
        @Override protected void onContentsChanged(int slot) { if (!silent) sync(); }
    };
    private LazyOptional<net.minecraftforge.items.IItemHandler> capability = LazyOptional.of(() -> inventory);
    public RechargePedestalEntity(BlockPos pos, BlockState state) { super(DeviceContent.PEDESTAL_TYPE.get(), pos, state); }
    public ItemStack item() { return inventory.getStackInSlot(0).copy(); }
    @Override public void serverTick() {
        if (level instanceof ServerLevel server && server.getGameTime() % 10 == 0) recharge();
    }
    public int recharge() {
        if (!(level instanceof ServerLevel server) || inventory.getStackInSlot(0).isEmpty()) return 0;
        int charged = RechargeHelper.rechargeItem(server, inventory.getStackInSlot(0), worldPosition, null, 5);
        if (charged > 0) sync();
        return charged;
    }
    public InteractionResult interact(Player player, InteractionHand hand) {
        var held = player.getItemInHand(hand);
        if (inventory.getStackInSlot(0).isEmpty() && !(held.getItem() instanceof IRechargable)) return InteractionResult.PASS;
        if (level == null || level.isClientSide()) return InteractionResult.SUCCESS;
        if (inventory.getStackInSlot(0).isEmpty()) {
            // Ownership moves from the held stack, including in creative; no item duplication.
            inventory.insertItem(0, held.split(1), false);
        } else {
            var extracted = inventory.extractItem(0, 1, false);
            if (!player.getInventory().add(extracted)) Block.popResource(level, worldPosition, extracted);
        }
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        return InteractionResult.SUCCESS;
    }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel) {
            silent = true;
            var stack = inventory.extractItem(0, 1, false);
            if (!stack.isEmpty()) Block.popResource(level, pos, stack);
        }
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return !isRemoved() && cap == ForgeCapabilities.ITEM_HANDLER ? capability.cast() : super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); capability.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); capability = LazyOptional.of(() -> inventory); }
    @Override protected void saveAdditional(ValueOutput output) { super.saveAdditional(output); if (!inventory.getStackInSlot(0).isEmpty()) output.store("item", ItemStack.CODEC, inventory.getStackInSlot(0)); }
    @Override protected void loadAdditional(ValueInput input) { super.loadAdditional(input); silent = true;
        try { inventory.setStackInSlot(0, input.read("item", ItemStack.CODEC).orElse(ItemStack.EMPTY)); } finally { silent = false; } }
}
