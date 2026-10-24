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
    private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,Thaumcraft.MODID);
    public static final RegistryObject<MenuType<ResearchMenu>> MENU=MENUS.register("thaumonomicon",()->new MenuType<>(ResearchMenu::new,FeatureFlags.VANILLA_SET));
    public static final RegistryObject<Item> BOOK=Registrar.ITEMS.register("thaumonomicon",()->new Item(properties("thaumonomicon")){
        @Override public InteractionResult use(Level level,Player player,InteractionHand hand){if(!level.isClientSide())ResearchService.openBook(player);return InteractionResult.SUCCESS;}
    });
    public static final RegistryObject<Item> METER=Registrar.ITEMS.register("thaumometer",()->new ThaumometerItem(properties("thaumometer")));
    static{BasicContent.display(BOOK);BasicContent.display(METER);}
    private ResearchContent(){}
    private static Item.Properties properties(String id){return new Item.Properties().setId(ResourceKey.create(Registries.ITEM,SimpleContent.id(id))).stacksTo(1).rarity(Rarity.UNCOMMON);}
    public static void register(BusGroup bus){MENUS.register(bus);ScanEffects.register(bus);}
}
