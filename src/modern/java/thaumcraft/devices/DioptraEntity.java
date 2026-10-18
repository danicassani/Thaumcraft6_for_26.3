package thaumcraft.devices;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import thaumcraft.aura.AuraManager;

public final class DioptraEntity extends AuraDeviceEntity {
    private int[] grid = new int[169];
    public DioptraEntity(BlockPos pos, BlockState state) { super(DeviceContent.DIOPTRA_TYPE.get(), pos, state); }
    /** Snapshot, never the mutable array retained by the tile. */
    public int[] grid() { return grid.clone(); }
    @Override public void serverTick() {
        if (!(level instanceof ServerLevel server) || server.getGameTime() % 20 != 0) return;
        scan();
    }
    public void scan() {
        if (!(level instanceof ServerLevel server)) return;
        var next = new int[169]; boolean flux = getBlockState().getValue(AuraDeviceBlock.POWERED);
        int cx = worldPosition.getX() >> 4, cz = worldPosition.getZ() >> 4;
        for (int x = 0; x < 13; x++) for (int z = 0; z < 13; z++) {
            var cell = AuraManager.get(server, cx + x - 6, cz + z - 6);
            if (cell != null) next[x + z * 13] = Math.clamp((int)((flux ? cell.flux() : cell.vis()) / 500F * 64), 0, 64);
        }
        if (!Arrays.equals(next, grid)) { grid = next; sync(); }
    }
    @Override protected void saveAdditional(ValueOutput output) { super.saveAdditional(output); output.putIntArray("grid", grid); }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input); var values = input.getIntArray("grid").orElse(new int[169]);
        grid = values.length == 169 ? values.clone() : new int[169];
        for (int i = 0; i < grid.length; i++) grid[i] = Math.clamp(grid[i], 0, 64);
    }
}
