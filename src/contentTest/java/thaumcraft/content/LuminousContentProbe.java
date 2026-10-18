package thaumcraft.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.*;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;

@Mod.EventBusSubscriber(modid = Thaumcraft.MODID)
public final class LuminousContentProbe {
    private LuminousContentProbe() {}
    @SubscribeEvent public static void started(ServerStartedEvent event) {
        var level = event.getServer().overworld();
        var pos = new BlockPos(0, 210, 0);
        var original = level.getBlockState(pos);
        var below = level.getBlockState(pos.below());
        try {
            for (var entry : LuminousContent.BLOCKS.entrySet()) {
                String name = entry.getKey();
                var block = (LuminousBlock)entry.getValue().get();
                var state = block.defaultBlockState();
                boolean nitor = name.startsWith("nitor_");
                require(state.getLightEmission(level, pos) == (nitor ? 15 : 14), "Wrong light: " + name);
                require(state.getCollisionShape(level, pos).isEmpty(), "Light blocks should not collide");
                level.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());
                require(state.canSurvive(level, pos) == nitor, "Floating candle/nitor support mismatch");
                level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
                require(state.canSurvive(level, pos), "Supported light rejected");
                level.setBlockAndUpdate(pos, state);
                require(Block.getDrops(state, level, pos, null).stream().anyMatch(s -> s.is(block.asItem())), "Light lost drop: " + name);
                if (!nitor) {
                    level.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());
                    block.tick(state, level, pos, level.getRandom());
                    require(level.getBlockState(pos).isAir(), "Unsupported candle did not break");
                }
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                String family = nitor ? "nitor" : "candle";
                var input = CraftingInput.of(2, 1, List.of(new ItemStack(block), new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.withDefaultNamespace("red_dye")))));
                var recipe = event.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElseThrow();
                require(recipe.value().assemble(input).is(LuminousContent.BLOCKS.get(family + "_red").get().asItem()), "Recolor mismatch: " + name);
            }
            require(LuminousContent.BLOCKS.size() == 32, "Color variants missing");
        } finally {
            level.setBlockAndUpdate(pos, original);
            level.setBlockAndUpdate(pos.below(), below);
        }
        Thaumcraft.LOGGER.info("LUMINOUS_CONTENT_PROBE_OK: 32 variants, light, collision, candle support/drop and recoloring");
    }
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
}
