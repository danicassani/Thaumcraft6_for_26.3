package thaumcraft.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import thaumcraft.aura.AuraManager;

/** 1 vis -> 1000 FE, capacity 1000, powered off by redstone, output <=20 FE/tick. */
public final class VisGeneratorEntity extends AuraDeviceEntity implements IEnergyStorage {
    private int energy;
    private LazyOptional<IEnergyStorage> capability = LazyOptional.of(() -> this);
    public VisGeneratorEntity(BlockPos pos, BlockState state) { super(DeviceContent.GENERATOR_TYPE.get(), pos, state); }
    @Override public void serverTick() {
        if (!(level instanceof ServerLevel server) || getBlockState().getValue(AuraDeviceBlock.POWERED)) return;
        int before = energy;
        if (energy == 0) {
            float available = AuraManager.drainVis(server, worldPosition, 1, true);
            int produced = (int)(available * 1000);
            // Do not consume fractions too small to produce even one FE.
            if (produced > 0) energy = (int)(AuraManager.drainVis(server, worldPosition, produced / 1000F, false) * 1000 + .0001F);
        }
        Direction face = getBlockState().getValue(AuraDeviceBlock.FACING);
        BlockPos target = worldPosition.relative(face);
        if (energy > 0 && server.hasChunkAt(target)) {
            var neighbor = server.getBlockEntity(target);
            if (neighbor != null) neighbor.getCapability(ForgeCapabilities.ENERGY, face.getOpposite()).ifPresent(receiver -> {
                if (receiver.canReceive()) {
                    int offered = Math.min(20, energy);
                    int accepted = Math.clamp(receiver.receiveEnergy(offered, false), 0, offered);
                    energy -= accepted;
                }
            });
        }
        if (energy != before) { setChanged(); if ((energy == 0) != (before == 0)) sync(); }
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (!isRemoved() && cap == ForgeCapabilities.ENERGY && side == getBlockState().getValue(AuraDeviceBlock.FACING)) return capability.cast();
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); capability.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); capability = LazyOptional.of(() -> this); }
    @Override protected void saveAdditional(ValueOutput output) { super.saveAdditional(output); output.putInt("energy", energy); }
    @Override protected void loadAdditional(ValueInput input) { super.loadAdditional(input); energy = Math.clamp(input.getIntOr("energy", 0), 0, 1000); }
    @Override public int receiveEnergy(int amount, boolean simulate) { return 0; }
    // Original generator pushes into the adjacent receiver; its capability is read-only.
    @Override public int extractEnergy(int amount, boolean simulate) { return 0; }
    @Override public int getEnergyStored() { return energy; }
    @Override public int getMaxEnergyStored() { return 1000; }
    @Override public boolean canExtract() { return true; }
    @Override public boolean canReceive() { return false; }
}
