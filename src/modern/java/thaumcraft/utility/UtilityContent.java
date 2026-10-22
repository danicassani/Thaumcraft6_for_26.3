package thaumcraft.utility;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.*;
import thaumcraft.Registrar;
import thaumcraft.content.SimpleContent;
public final class UtilityContent {
 private static final DeferredRegister<BlockEntityType<?>> TYPES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,"thaumcraft");
 private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,"thaumcraft");
 public static final Map<String,RegistryObject<Block>> BLOCKS=new LinkedHashMap<>();
 static{for(var n:List.of("lamp_arcane","lamp_growth","lamp_fertility","everfull_urn","spa","potion_sprayer")){RegistryObject<Block> b=Registrar.BLOCKS.register(n,()->new UtilityBlock(n,props(n).strength(1.5F).sound(SoundType.METAL).noOcclusion().lightLevel(s->n.startsWith("lamp_")&&s.getValue(UtilityBlock.ACTIVE)?15:0)));BLOCKS.put(n,b);SimpleContent.BLOCKS.put(n,b);SimpleContent.item(n,p->new net.minecraft.world.item.BlockItem(b.get(),p));}}
 public static final RegistryObject<Block> GLIMMER=thaumcraft.infusion.InfusionContent.GLIMMER;
 public static final RegistryObject<BlockEntityType<LampEntity>> LAMPS=TYPES.register("utility_lamp",()->new BlockEntityType<>(LampEntity::new,Set.of(BLOCKS.get("lamp_arcane").get(),BLOCKS.get("lamp_growth").get(),BLOCKS.get("lamp_fertility").get())));
 public static final RegistryObject<BlockEntityType<GlimmerEntity>> LIGHT=TYPES.register("effect_glimmer",()->new BlockEntityType<>(GlimmerEntity::new,Set.of(GLIMMER.get())));
 public static final RegistryObject<BlockEntityType<WaterDevice>> WATER=TYPES.register("utility_water",()->new BlockEntityType<>(WaterDevice::new,Set.of(BLOCKS.get("everfull_urn").get(),BLOCKS.get("spa").get())));
 public static final RegistryObject<BlockEntityType<SprayerEntity>> SPRAYER=TYPES.register("potion_sprayer",()->new BlockEntityType<>(SprayerEntity::new,Set.of(BLOCKS.get("potion_sprayer").get())));
 public static final RegistryObject<MenuType<UtilityMenu>> MENU=MENUS.register("utility_device",()->new MenuType<>(UtilityMenu::new,FeatureFlags.VANILLA_SET));
 private static BlockBehaviour.Properties props(String n){return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id(n)));}
 public static void register(net.minecraftforge.eventbus.api.bus.BusGroup bus){TYPES.register(bus);MENUS.register(bus);}
}
