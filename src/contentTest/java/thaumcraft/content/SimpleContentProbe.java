package thaumcraft.content;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;

/** Real loaded registries/data, not a duplicate of the JSON generator. */
@Mod.EventBusSubscriber(modid = Thaumcraft.MODID)
public final class SimpleContentProbe {
    private SimpleContentProbe() {}
    @SubscribeEvent public static void started(ServerStartedEvent event) {
        var server = event.getServer();
        var level = server.overworld();
        var pos = new BlockPos(0, 205, 0);
        var tool = new ItemStack(Items.DIAMOND_PICKAXE);
        for (var entry : SimpleContent.BLOCKS.entrySet()) {
            String name = entry.getKey();
            Block block = entry.getValue().get();
            var state = block.defaultBlockState();
            var drops = Block.getDrops(state, level, pos, null, null, tool);
            if (name.equals("stone_ancient_rock") || name.equals("stone_ancient_doorway")) {
                require(drops.isEmpty() && state.getDestroySpeed(level, pos) < 0, "Unbreakable stone mismatch: " + name);
            } else if (name.startsWith("pillar_")) {
                require(drops.size() == 1 && drops.getFirst().getCount() == 2, "Pillar drop: " + name);
                require(state.getCollisionShape(level, pos).bounds().maxY == 2.0, "Pillar collision height");
            } else {
                require(drops.size() == 1 && drops.getFirst().is(block.asItem()) && drops.getFirst().getCount() == 1, "Drop mismatch: " + name);
            }
            if (block instanceof SlabBlock) {
                var doubled = state.setValue(SlabBlock.TYPE, SlabType.DOUBLE);
                var doubleDrops = Block.getDrops(doubled, level, pos, null, null, tool);
                require(doubleDrops.size() == 1 && doubleDrops.getFirst().is(block.asItem()) && doubleDrops.getFirst().getCount() == 2, "Double slab lost material: " + name);
                require(state.getCollisionShape(level, pos).bounds().maxY == 0.5, "Slab collision mismatch");
            }
            if (block instanceof SimplePlantBlock) {
                var previous = level.getBlockState(pos.below());
                try {
                    level.setBlockAndUpdate(pos.below(), name.equals("cinderpearl") ? Blocks.SAND.defaultBlockState() : Blocks.DIRT.defaultBlockState());
                    require(state.canSurvive(level, pos), "Plant rejects valid soil: " + name);
                    level.setBlockAndUpdate(pos.below(), Blocks.GLASS.defaultBlockState());
                    require(!state.canSurvive(level, pos), "Plant accepts glass: " + name);
                } finally { level.setBlockAndUpdate(pos.below(), previous); }
            }
        }
        for (String name : new String[]{"thaumium_sword", "thaumium_pick", "thaumium_axe", "thaumium_shovel", "thaumium_hoe"}) {
            var stack = new ItemStack(SimpleContent.ITEMS.get(name).get());
            require(stack.getMaxDamage() == 500, "Thaumium durability: " + name);
            require(stack.get(DataComponents.ENCHANTABLE).value() == 22, "Thaumium enchantability");
        }
        require(new ItemStack(SimpleContent.ITEMS.get("thaumium_pick").get()).isCorrectToolForDrops(BasicContent.ORE_CINNABAR.get().defaultBlockState()), "Thaumium pick cannot mine cinnabar");
        for (String name : new String[]{"beef", "chicken", "pork", "fish", "rabbit", "mutton"}) {
            var stack = new ItemStack(SimpleContent.ITEMS.get("chunk_" + name).get());
            require(stack.get(DataComponents.FOOD).nutrition() == 1, "Food nutrition");
            require(stack.get(DataComponents.CONSUMABLE).consumeSeconds() == 0.5F, "Food consumption speed");
        }
        String[] meats = {"beef", "chicken", "pork", "fish", "rabbit", "mutton"};
        for (int a = 0; a < 6; a++) for (int b = a + 1; b < 6; b++) for (int c = b + 1; c < 6; c++) {
            var inputs = new ArrayList<ItemStack>();
            for (int index : new int[]{a, b, c}) inputs.add(new ItemStack(SimpleContent.ITEMS.get("chunk_" + meats[index]).get()));
            inputs.add(new ItemStack(Items.SUGAR));
            var input = CraftingInput.of(2, 2, inputs);
            var recipe = server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElseThrow();
            require(recipe.value().assemble(input).is(SimpleContent.ITEMS.get("triple_meat_treat").get()), "Distinct meats recipe failed");
        }
        var beef = SimpleContent.ITEMS.get("chunk_beef").get();
        var invalid = CraftingInput.of(2, 2, List.of(new ItemStack(beef), new ItemStack(beef), new ItemStack(SimpleContent.ITEMS.get("chunk_chicken").get()), new ItemStack(Items.SUGAR)));
        require(server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, invalid, level).isEmpty(), "Repeated meat bypassed recipe");
        Thaumcraft.LOGGER.info("SIMPLE_CONTENT_PROBE_OK: {} blocks, slabs/pillars/soil, tools, food and 20 distinct-meat recipes", SimpleContent.BLOCKS.size());
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
