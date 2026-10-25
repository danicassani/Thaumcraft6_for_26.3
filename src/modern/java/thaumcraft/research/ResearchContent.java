package thaumcraft.research;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.*;
import thaumcraft.*;
import thaumcraft.content.*;

public final class ResearchContent {
    private static final DeferredRegister<net.minecraft.core.component.DataComponentType<?>> COMPONENTS=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,Thaumcraft.MODID);
    public static final RegistryObject<net.minecraft.core.component.DataComponentType<net.minecraft.util.Unit>> CHEAT_SHEET=COMPONENTS.register("cheat_sheet",()->net.minecraft.core.component.DataComponentType.<net.minecraft.util.Unit>builder().persistent(net.minecraft.util.Unit.CODEC).networkSynchronized(net.minecraft.util.Unit.STREAM_CODEC).build());
    private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,Thaumcraft.MODID);
    public static final RegistryObject<MenuType<ResearchMenu>> MENU=MENUS.register("thaumonomicon",()->new MenuType<>(ResearchMenu::new,FeatureFlags.VANILLA_SET));
    public static final RegistryObject<Item> BOOK=Registrar.ITEMS.register("thaumonomicon",()->new ThaumonomiconItem(properties("thaumonomicon")));
    public static final RegistryObject<Item> METER=Registrar.ITEMS.register("thaumometer",()->new ThaumometerItem(properties("thaumometer")));
    static{BasicContent.display(BOOK);BasicContent.display(METER);}
    private ResearchContent(){}
    private static Item.Properties properties(String id){return new Item.Properties().setId(ResourceKey.create(Registries.ITEM,SimpleContent.id(id))).stacksTo(1).rarity(Rarity.UNCOMMON);}
    public static void register(BusGroup bus){COMPONENTS.register(bus);MENUS.register(bus);ScanEffects.register(bus);net.minecraftforge.event.entity.player.ItemTooltipEvent.BUS.addListener(e->{if(CheatSheet.special(e.getItemStack()))e.getToolTip().add(net.minecraft.network.chat.Component.translatable(CheatSheet.allowed()?"research.thaumcraft.cheat_hint":"research.thaumcraft.cheat_disabled"));});}
}
