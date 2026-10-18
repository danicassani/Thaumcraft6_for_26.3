package thaumcraft.content;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.RegistryObject;
import thaumcraft.Registrar;

/** First independent mechanic: permanently lit candles and floating nitor. */
public final class LuminousContent {
    public static final Map<String, RegistryObject<? extends Block>> BLOCKS = new LinkedHashMap<>();
    public static final Map<String, Integer> COLORS = new LinkedHashMap<>();
    private LuminousContent() {}
    public static void register() {
        for (DyeColor dye : DyeColor.values()) {
            String legacy = dye == DyeColor.LIGHT_GRAY ? "silver" : dye == DyeColor.LIGHT_BLUE ? "lightblue" : dye.getSerializedName();
            for (boolean nitor : new boolean[]{false, true}) {
                String name = (nitor ? "nitor_" : "candle_") + legacy;
                int color = dye.getTextureDiffuseColor() & 0xFFFFFF;
                var block = Registrar.BLOCKS.register(name, () -> new LuminousBlock(nitor, color,
                    BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, SimpleContent.id(name)))
                        .strength(0.1F).sound(SoundType.WOOL).noCollision().noOcclusion().lightLevel(state -> nitor ? 15 : 14)));
                var item = Registrar.ITEMS.register(name, () -> new BlockItem(block.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, SimpleContent.id(name))).useBlockDescriptionPrefix()));
                BLOCKS.put(name, block);
                COLORS.put(name, color);
                BasicContent.display(item);
            }
        }
    }
}
