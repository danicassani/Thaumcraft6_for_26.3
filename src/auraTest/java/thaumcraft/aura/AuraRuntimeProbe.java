package thaumcraft.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.content.AuraContent;
import thaumcraft.content.CreativeFluxSpongeItem;
import thaumcraft.content.VisBatteryBlock;

/** Explicit -PauraProbe fixture. Never part of production builds. */
@Mod.EventBusSubscriber(modid = Thaumcraft.MODID)
public final class AuraRuntimeProbe {
    private static final BlockPos POS = new BlockPos(0, 205, 0);
    private static final AuraCell MARKER = new AuraCell(281, 112.125F, 77.5F);
    private AuraRuntimeProbe() {}
    @SubscribeEvent public static void started(ServerStartedEvent event) {
        String mode = System.getProperty("thaumcraft.auraProbe", "");
        if (!mode.equals("write") && !mode.equals("read")) return;
        var level = event.getServer().overworld(); level.getChunk(0, 0);
        var data = AuraData.get(level);
        if (mode.equals("read")) {
            require(MARKER.equals(data.get(AuraCell.key(0, 0))), "Restart changed saved aura");
            require(level.getBlockState(POS).is(AuraContent.VIS_BATTERY.get()), "Battery lost on restart");
            require(level.getBlockState(POS).getValue(VisBatteryBlock.CHARGE) == 7, "Saved battery charge changed");
            var nether = event.getServer().getLevel(net.minecraft.world.level.Level.NETHER);
            require(new AuraCell(80, 21.5F, 2.25F).equals(AuraData.get(nether).get(AuraCell.key(0, 0))), "Nether data lost or shared across dimensions");
            Thaumcraft.LOGGER.info("AURA_RESTART_PROBE_OK: fractional vis/flux, dimension isolation, battery charge");
            return;
        }
        var generated = AuraManager.get(level, POS);
        require(generated != null && generated.base() >= 0 && generated.base() <= 500, "Aura generation failed");
        data.put(AuraCell.key(0, 0), new AuraCell(200, 100, 25));
        require(AuraManager.drainVis(level, POS, 500, true) == 100 && AuraManager.get(level, POS).vis() == 100, "Simulation mutated aura");
        require(AuraManager.drainVis(level, POS, 125, false) == 100 && AuraManager.get(level, POS).vis() == 0, "Vis drain did not clamp");
        require(AuraManager.addVis(level, POS, AuraCell.MAX) == AuraCell.MAX, "Vis fill");
        require(AuraManager.addVis(level, POS, 1) == 0, "Vis overflow");
        require(AuraManager.get(level, 1200000, -1200000) == null, "Read generated unloaded chunk");
        require(AuraManager.addFlux(level, new BlockPos(1200000 * 16, 200, -1200000 * 16), 2) == 0, "Mutation loaded terrain");
        try { AuraManager.addFlux(level, POS, Float.NaN); throw new AssertionError("NaN accepted"); } catch (IllegalArgumentException expected) {}
        var battery = AuraContent.VIS_BATTERY.get(); var state = battery.defaultBlockState();
        level.setBlockAndUpdate(POS, state); data.put(AuraCell.key(0, 0), new AuraCell(200, 200, 0));
        battery.transfer(state, level, POS, level.getRandom());
        state = level.getBlockState(POS);
        require(state.getValue(VisBatteryBlock.CHARGE) == 1 && AuraManager.get(level, POS).vis() == 199, "Battery charge did not conserve vis");
        require(state.getAnalogOutputSignal(level, POS, Direction.NORTH) == 1 && state.getLightEmission(level, POS) == 1, "Battery comparator/light");
        data.put(AuraCell.key(0, 0), new AuraCell(200, 50, 0)); battery.transfer(state, level, POS, level.getRandom());
        require(level.getBlockState(POS).getValue(VisBatteryBlock.CHARGE) == 0 && AuraManager.get(level, POS).vis() == 51, "Battery low-aura release");
        level.setBlockAndUpdate(POS.below(), Blocks.REDSTONE_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(POS, battery.defaultBlockState().setValue(VisBatteryBlock.CHARGE, 2));
        data.put(AuraCell.key(0, 0), new AuraCell(200, 100, 0)); battery.transfer(level.getBlockState(POS), level, POS, level.getRandom());
        require(level.getBlockState(POS).getValue(VisBatteryBlock.CHARGE) == 1 && AuraManager.get(level, POS).vis() == 101, "Redstone release");
        data.put(AuraCell.key(0, 0), new AuraCell(200, AuraCell.MAX, 0)); battery.transfer(level.getBlockState(POS), level, POS, level.getRandom());
        require(level.getBlockState(POS).getValue(VisBatteryBlock.CHARGE) == 1, "Overflow destroyed stored energy");
        data.put(AuraCell.key(0, 0), new AuraCell(200, AuraCell.MAX - .5F, 0)); battery.transfer(level.getBlockState(POS), level, POS, level.getRandom());
        require(AuraManager.get(level, POS).vis() == AuraCell.MAX - .5F && level.getBlockState(POS).getValue(VisBatteryBlock.CHARGE) == 1, "Partial overflow duplicated energy");
        var drops = Block.getDrops(level.getBlockState(POS), level, POS, null, null, ItemStack.EMPTY);
        require(drops.size() == 1 && drops.getFirst().is(AuraContent.BATTERY_ITEM.get()), "Battery loot");
        level.setBlockAndUpdate(POS.below(), Blocks.AIR.defaultBlockState());
        level.getChunk(1, 0); level.getChunk(5, 0);
        data.put(AuraCell.key(0, 0), new AuraCell(200, 100, 10));
        data.put(AuraCell.key(1, 0), new AuraCell(200, 100, 600));
        data.put(AuraCell.key(5, 0), new AuraCell(200, 100, 700));
        require(CreativeFluxSpongeItem.clearArea(level, POS) >= 610, "Sponge left flux above legacy 500-unit drain");
        require(AuraManager.get(level, 0, 0).flux() == 0 && AuraManager.get(level, 1, 0).flux() == 0 && AuraManager.get(level, 5, 0).flux() == 700, "Sponge area boundary");
        var nether = event.getServer().getLevel(net.minecraft.world.level.Level.NETHER); nether.getChunk(0, 0);
        AuraData.get(nether).put(AuraCell.key(0, 0), new AuraCell(80, 21.5F, 2.25F));
        require(AuraManager.get(level, 0, 0).base() == 200, "Dimensions share aura");
        Thaumcraft.LOGGER.info("AURA_RUNTIME_PROBE_OK: generation, simulation/drain/caps, no terrain loading, battery charge/release/redstone/comparator/light/loot, sponge boundary and dimensions");
    }
    @SubscribeEvent public static void stopping(ServerStoppingEvent event) {
        if (!System.getProperty("thaumcraft.auraProbe", "").equals("write")) return;
        var level = event.getServer().overworld();
        AuraData.get(level).put(AuraCell.key(0, 0), MARKER);
        level.setBlockAndUpdate(POS, AuraContent.VIS_BATTERY.get().defaultBlockState().setValue(VisBatteryBlock.CHARGE, 7));
        var nether = event.getServer().getLevel(net.minecraft.world.level.Level.NETHER);
        AuraData.get(nether).put(AuraCell.key(0, 0), new AuraCell(80, 21.5F, 2.25F));
        Thaumcraft.LOGGER.info("AURA_SAVE_PROBE_READY");
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
