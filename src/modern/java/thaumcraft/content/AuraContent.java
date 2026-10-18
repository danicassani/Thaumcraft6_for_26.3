package thaumcraft.content;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.RegistryObject;
import thaumcraft.Registrar;

public final class AuraContent {
    public static final RegistryObject<VisBatteryBlock> VIS_BATTERY = Registrar.BLOCKS.register("vis_battery", () -> new VisBatteryBlock(
        BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, SimpleContent.id("vis_battery")))
            .strength(.5F).randomTicks().lightLevel(state -> state.getValue(VisBatteryBlock.CHARGE))));
    public static final RegistryObject<Item> BATTERY_ITEM = Registrar.ITEMS.register("vis_battery", () -> new BlockItem(VIS_BATTERY.get(),
        new Item.Properties().setId(ResourceKey.create(Registries.ITEM, SimpleContent.id("vis_battery"))).useBlockDescriptionPrefix()));
    public static final RegistryObject<Item> FLUX_SPONGE = Registrar.ITEMS.register("creative_flux_sponge", () -> new CreativeFluxSpongeItem(
        new Item.Properties().setId(ResourceKey.create(Registries.ITEM, SimpleContent.id("creative_flux_sponge"))).stacksTo(1).rarity(Rarity.EPIC)
            .component(DataComponents.LORE, new ItemLore(List.of(Component.translatable("item.thaumcraft.creative_flux_sponge.help"))))));
    private AuraContent() {}
    public static void register() { BasicContent.display(BATTERY_ITEM); BasicContent.display(FLUX_SPONGE); }
}
