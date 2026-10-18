package thaumcraft.content;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.equipment.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import thaumcraft.Registrar;
import thaumcraft.Thaumcraft;

/** Content whose runtime behavior uses ordinary Minecraft blocks/components. */
public final class SimpleContent {
    public static final Map<String, RegistryObject<Block>> BLOCKS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> ITEMS = new LinkedHashMap<>();
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Thaumcraft.MODID);
    private static final RegistryObject<SoundEvent> GORE = SOUNDS.register("gore", () -> SoundEvent.createVariableRangeEvent(id("gore")));
    private static final TagKey<Item> THAUMIUM_REPAIR = TagKey.create(Registries.ITEM, id("repairs_thaumium"));
    private static final ToolMaterial THAUMIUM_TOOL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 500, 7.0F, 2.5F, 22, THAUMIUM_REPAIR);
    private static final ArmorMaterial THAUMIUM_ARMOR = new ArmorMaterial(25,
        Map.of(ArmorType.BOOTS, 2, ArmorType.LEGGINGS, 5, ArmorType.CHESTPLATE, 6, ArmorType.HELMET, 2),
        25, SoundEvents.ARMOR_EQUIP_IRON, 1.0F, 0.0F, THAUMIUM_REPAIR,
        ResourceKey.create(EquipmentAssets.ROOT_ID, id("thaumium")));

    static {
        SimpleDefinitions.register();
        for (String type : new String[]{"sword", "pick", "axe", "shovel", "hoe"}) {
            item("thaumium_" + type, p -> new Item(switch (type) {
                case "sword" -> p.sword(THAUMIUM_TOOL, 3.0F, -2.4F);
                case "pick" -> p.pickaxe(THAUMIUM_TOOL, 1.0F, -2.8F);
                case "axe" -> p.axe(THAUMIUM_TOOL, 5.5F, -3.0F);
                case "shovel" -> p.shovel(THAUMIUM_TOOL, 1.5F, -3.0F);
                default -> p.hoe(THAUMIUM_TOOL, -2.5F, -1.0F);
            }));
        }
        armor("thaumium_helm", ArmorType.HELMET);
        armor("thaumium_chest", ArmorType.CHESTPLATE);
        armor("thaumium_legs", ArmorType.LEGGINGS);
        armor("thaumium_boots", ArmorType.BOOTS);
        for (String meat : new String[]{"beef", "chicken", "pork", "fish", "rabbit", "mutton"}) {
            item("chunk_" + meat, p -> new Item(p.food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.3F).build(),
                Consumables.defaultFood().consumeSeconds(0.5F).build())));
        }
        item("triple_meat_treat", p -> new Item(p.food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.8F).alwaysEdible().build(),
            Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 100), 0.66F)).build())));
    }

    private SimpleContent() {}
    public static void register(BusGroup bus) { SOUNDS.register(bus); }
    public static Identifier id(String name) { return Identifier.fromNamespaceAndPath(Thaumcraft.MODID, name); }
    public static void material(String name) { item(name, Item::new); }

    public static void item(String name, Function<Item.Properties, Item> factory) {
        var item = Registrar.ITEMS.register(name, () -> factory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id(name)))));
        ITEMS.put(name, item);
        BasicContent.display(item);
    }

    private static void armor(String name, ArmorType type) {
        item(name, p -> new Item(p.humanoidArmor(THAUMIUM_ARMOR, type).rarity(Rarity.UNCOMMON)));
    }

    public static void block(String name, String kind, String base) {
        RegistryObject<Block> block = Registrar.BLOCKS.register(name, () -> {
            var p = BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id(name))).strength(2.0F, 10.0F).sound(SoundType.STONE);
            if (kind.equals("stone") || kind.equals("metal") || kind.equals("pillar")) p.requiresCorrectToolForDrops();
            if (kind.equals("wood") || kind.equals("log")) p.strength(2.0F, kind.equals("log") ? 5.0F : 2.0F).sound(SoundType.WOOD).ignitedByLava();
            if (kind.equals("log")) return new SimpleLogBlock(p.lightLevel(state -> name.equals("log_silverwood") ? 5 : 0));
            if (kind.equals("wood")) return new SimpleWoodBlock(p);
            if (kind.equals("stairs") || kind.equals("slab")) {
                Block parent = BLOCKS.get(base).get();
                p = BlockBehaviour.Properties.ofFullCopy(parent).setId(ResourceKey.create(Registries.BLOCK, id(name)));
                return kind.equals("stairs") ? new SimpleStairBlock(parent.defaultBlockState(), p, base.startsWith("plank_")) : new SimpleSlabBlock(p, base.startsWith("plank_"));
            }
            if (kind.equals("pillar")) return new SimplePillarBlock(p.strength(2.5F).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.IMMOVEABLE));
            if (kind.equals("plant")) return new SimplePlantBlock(name, p.strength(0.0F).noCollision().noOcclusion().sound(SoundType.GRASS).lightLevel(state -> name.equals("cinderpearl") ? 7 : 6));
            if (kind.equals("rail")) return new PoweredRailBlock(p.strength(0.7F).sound(SoundType.METAL).noCollision(), true) {};
            if (kind.equals("metal")) p.strength(4.0F, 10.0F).sound(SoundType.METAL);
            if (kind.equals("flesh")) p.strength(0.25F, 2.0F).sound(new ForgeSoundType(0.5F, 1.0F, GORE, GORE, GORE, GORE, GORE));
            if (name.equals("stone_ancient_rock") || name.equals("stone_ancient_doorway")) p.strength(-1.0F, 10.0F).noLootTable();
            if (name.equals("stone_eldritch_tile")) p.strength(15.0F, 1000.0F);
            return new Block(p);
        });
        BLOCKS.put(name, block);
        item(name, p -> new BlockItem(block.get(), p.useBlockDescriptionPrefix()));
    }
}
