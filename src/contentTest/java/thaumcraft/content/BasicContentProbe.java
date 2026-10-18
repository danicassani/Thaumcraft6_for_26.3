package thaumcraft.content;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;

/** Opt-in integration checks against real registries, loot, recipes and generated chunks. */
@Mod.EventBusSubscriber(modid = Thaumcraft.MODID)
public final class BasicContentProbe {
    private BasicContentProbe() {}

    @SubscribeEvent
    public static void started(ServerStartedEvent event) {
        var server = event.getServer();
        var level = server.overworld();
        var pos = new BlockPos(0, 200, 0);
        var tool = new ItemStack(Items.DIAMOND_PICKAXE);
        for (String name : List.of("ore_amber", "ore_cinnabar", "ore_quartz", "metal_thaumium", "metal_void", "metal_brass", "amber_block", "amber_brick")) {
            var block = BuiltInRegistries.BLOCK.getValue(id(name));
            require(block != null && block.asItem() != Items.AIR, "Missing block item: " + name);
            var state = block.defaultBlockState();
            var previous = level.getBlockState(pos);
            try {
                require(level.setBlockAndUpdate(pos, state), "Placement failed: " + name);
                require(level.getBlockState(pos).is(block), "Wrong placed block: " + name);
                var drops = Block.getDrops(state, level, pos, null, null, tool);
                Item expected = name.equals("ore_amber") ? BasicContent.AMBER.get() : name.equals("ore_quartz") ? Items.QUARTZ : block.asItem();
                require(drops.size() == 1 && drops.getFirst().is(expected), "Wrong loot: " + name + " " + drops);
                require(drops.getFirst().getCount() >= 1 && drops.getFirst().getCount() <= (name.equals("ore_amber") ? 2 : 1), "Wrong quantity: " + name);
            } finally {
                level.setBlockAndUpdate(pos, previous);
            }
        }
        tool.enchant(server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1);
        for (var ore : List.of(BasicContent.ORE_AMBER.get(), BasicContent.ORE_QUARTZ.get())) {
            var drops = Block.getDrops(ore.defaultBlockState(), level, pos, null, null, tool);
            require(drops.size() == 1 && drops.getFirst().is(ore.asItem()), "Silk touch failed");
        }
        for (String metal : List.of("thaumium", "void", "brass")) {
            var ingot = BuiltInRegistries.ITEM.getValue(id("ingot_" + metal));
            var block = BuiltInRegistries.ITEM.getValue(id("metal_" + metal));
            var nugget = BuiltInRegistries.ITEM.getValue(id("nugget_" + metal));
            craft(server, ingot, 3, block, 1);
            craft(server, block, 1, ingot, 9);
            craft(server, nugget, 3, ingot, 1);
            craft(server, ingot, 1, nugget, 9);
        }
        craft(server, BasicContent.AMBER.get(), 2, BasicContent.AMBER_BLOCK.get().asItem(), 1);
        craft(server, BasicContent.AMBER_BLOCK.get().asItem(), 1, BasicContent.AMBER.get(), 4);
        craft(server, BasicContent.AMBER_BLOCK.get().asItem(), 2, BasicContent.AMBER_BRICK.get().asItem(), 4);
        craft(server, BasicContent.AMBER_BRICK.get().asItem(), 2, BasicContent.AMBER_BLOCK.get().asItem(), 4);
        craft(server, BasicContent.NUGGET_QUICKSILVER.get(), 3, BasicContent.QUICKSILVER.get(), 1);
        craft(server, BasicContent.QUICKSILVER.get(), 1, BasicContent.NUGGET_QUICKSILVER.get(), 9);
        var input = new SingleRecipeInput(new ItemStack(BasicContent.ORE_CINNABAR.get()));
        var smelting = server.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, level).orElseThrow();
        require(smelting.value().assemble(input).is(BasicContent.QUICKSILVER.get()), "Cinnabar smelting failed");

        int density = thaumcraft.config.ThaumcraftConfig.oreDensity.get();
        boolean amberEnabled = thaumcraft.config.ThaumcraftConfig.generateAmber.get();
        try {
            thaumcraft.config.ThaumcraftConfig.generateAmber.set(false);
            require(new OreCountPlacement("amber", 8).count(net.minecraft.util.RandomSource.create(), pos) == 0, "Generation toggle ignored");
            thaumcraft.config.ThaumcraftConfig.generateAmber.set(true);
            thaumcraft.config.ThaumcraftConfig.oreDensity.set(500);
            require(new OreCountPlacement("amber", 8).count(net.minecraft.util.RandomSource.create(), pos) == 40, "Density ignored");
        } finally {
            thaumcraft.config.ThaumcraftConfig.oreDensity.set(density);
            thaumcraft.config.ThaumcraftConfig.generateAmber.set(amberEnabled);
        }
        var found = new HashSet<Block>();
        var ores = List.of(BasicContent.ORE_AMBER.get(), BasicContent.ORE_CINNABAR.get(), BasicContent.ORE_QUARTZ.get());
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (var section : level.getChunk(x, z).getSections()) {
                    for (var ore : ores) if (section.maybeHas(state -> state.is(ore))) found.add(ore);
                }
            }
        }
        require(found.containsAll(ores), "Natural worldgen missing ores: " + found);
        Thaumcraft.LOGGER.info("BASIC_CONTENT_PROBE_OK: 8 block placements/loot, silk touch, 18 crafting recipes, smelting, 3 natural ores in 25 chunks");
    }

    private static void craft(net.minecraft.server.MinecraftServer server, Item inputItem, int width, Item expected, int count) {
        var items = new ArrayList<ItemStack>();
        for (int i = 0; i < width * width; i++) items.add(new ItemStack(inputItem));
        var input = CraftingInput.of(width, width, items);
        var recipe = server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, server.overworld()).orElseThrow();
        var result = recipe.value().assemble(input);
        require(result.is(expected) && result.getCount() == count, "Crafting mismatch: " + inputItem + " -> " + result);
    }
    private static Identifier id(String name) { return Identifier.fromNamespaceAndPath("thaumcraft", name); }
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
}
