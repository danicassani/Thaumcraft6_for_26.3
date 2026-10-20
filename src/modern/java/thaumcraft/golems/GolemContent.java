package thaumcraft.golems;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.*;
import thaumcraft.*;
import thaumcraft.content.SimpleContent;
public final class GolemContent {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,Thaumcraft.MODID);
    private static final DeferredRegister<BlockEntityType<?>> TYPES=DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,Thaumcraft.MODID);
    private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(ForgeRegistries.MENU_TYPES,Thaumcraft.MODID);
    public static final RegistryObject<DataComponentType<GolemDesign>> DESIGN=COMPONENTS.register("golem_design",()->DataComponentType.<GolemDesign>builder().persistent(GolemDesign.CODEC).networkSynchronized(ByteBufCodecs.fromCodec(GolemDesign.CODEC)).build());
    public static final RegistryObject<DataComponentType<GolemProgress>> PROGRESS=COMPONENTS.register("golem_progress",()->DataComponentType.<GolemProgress>builder().persistent(GolemProgress.CODEC).networkSynchronized(ByteBufCodecs.fromCodec(GolemProgress.CODEC)).build());
    public static final RegistryObject<DataComponentType<Float>> HEALTH=COMPONENTS.register("golem_health",()->DataComponentType.<Float>builder().persistent(com.mojang.serialization.Codec.floatRange(.001F,1F)).networkSynchronized(ByteBufCodecs.FLOAT).build());
    public static final RegistryObject<DataComponentType<Integer>> CHANNEL=COMPONENTS.register("golem_channel",()->DataComponentType.<Integer>builder().persistent(com.mojang.serialization.Codec.intRange(0,15)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final RegistryObject<EntityType<GolemDart>> DART=Registrar.ENTITIES.register("golem_dart",()->EntityType.Builder.<GolemDart>of(GolemDart::new,MobCategory.MISC).sized(.2F,.2F).clientTrackingRange(8).updateInterval(1).build(ResourceKey.create(Registries.ENTITY_TYPE,SimpleContent.id("golem_dart"))));
    public static final RegistryObject<EntityType<WorkerGolem>> GOLEM=Registrar.ENTITIES.register("golem",()->EntityType.Builder.<WorkerGolem>of(WorkerGolem::new,MobCategory.CREATURE).sized(1.4F,2.7F).clientTrackingRange(10).build(ResourceKey.create(Registries.ENTITY_TYPE,SimpleContent.id("golem"))));
    public static final RegistryObject<GolemBuilderBlock> BUILDER=Registrar.BLOCKS.register("golem_builder",()->new GolemBuilderBlock(props("golem_builder").strength(4)));
    public static final RegistryObject<BlockEntityType<GolemBuilder>> BUILDER_TYPE=TYPES.register("golem_builder",()->new BlockEntityType<>(GolemBuilder::new,Set.of(BUILDER.get())));
    public static final RegistryObject<MenuType<GolemBuilderMenu>> BUILDER_MENU=MENUS.register("golem_builder",()->new MenuType<>(GolemBuilderMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static final Map<SealKind,RegistryObject<SealBlock>> SEALS=new LinkedHashMap<>();
    static {for(var k:SealKind.values())SEALS.put(k,Registrar.BLOCKS.register("seal_"+k.id(),()->new SealBlock(props("seal_"+k.id()).strength(.5F).noOcclusion(),k)));}
    public static final RegistryObject<BlockEntityType<SealEntity>> SEAL_TYPE=TYPES.register("seal",()->new BlockEntityType<>(SealEntity::new,new HashSet<>(SEALS.values().stream().map(RegistryObject::get).toList())));
    public static final RegistryObject<MenuType<SealMenu>> SEAL_MENU=MENUS.register("seal",()->new MenuType<>(SealMenu::new,FeatureFlags.DEFAULT_FLAGS));
    private static BlockBehaviour.Properties props(String id){return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id(id)));}
    public static void register(BusGroup bus){SimpleContent.item("golem_builder",p->new BlockItem(BUILDER.get(),p.useBlockDescriptionPrefix()));SimpleContent.item("golem",GolemPlacer::new);SimpleContent.item("golem_bell",GolemBell::new);SimpleContent.material("seal_blank");for(var entry:SEALS.entrySet()){var kind=entry.getKey();SimpleContent.item("seal_"+kind.id(),p->new SealItem(entry.getValue().get(),p));}
        net.minecraftforge.event.entity.EntityAttributeCreationEvent.BUS.addListener(e->e.put(GOLEM.get(),net.minecraft.world.entity.animal.golem.IronGolem.createAttributes().add(net.minecraft.world.entity.ai.attributes.Attributes.SCALE,.4).add(net.minecraft.world.entity.ai.attributes.Attributes.FLYING_SPEED,.25).build()));
        net.minecraftforge.event.level.LevelEvent.Unload.BUS.addListener(e->SealEntity.unload(e.getLevel() instanceof net.minecraft.world.level.Level l?l:null));
        net.minecraftforge.event.entity.player.ItemTooltipEvent.BUS.addListener(e->{var d=e.getItemStack().get(DESIGN.get());if(d!=null){e.getToolTip().add(e.getItemStack().getOrDefault(PROGRESS.get(),GolemProgress.EMPTY).description());e.getToolTip().add(net.minecraft.network.chat.Component.translatable("golem.thaumcraft."+GolemDesign.MATERIALS.get(d.material())).append(" • ").append(net.minecraft.network.chat.Component.translatable("golem.thaumcraft."+GolemDesign.HEADS.get(d.head()))));e.getToolTip().add(net.minecraft.network.chat.Component.translatable("golem.thaumcraft."+GolemDesign.ARMS.get(d.arms())).append(" • ").append(net.minecraft.network.chat.Component.translatable("golem.thaumcraft."+GolemDesign.ADDONS.get(d.addon())).append(" • ").append(net.minecraft.network.chat.Component.translatable("golem.thaumcraft."+GolemDesign.LEGS.get(d.legs())))));}});
        COMPONENTS.register(bus);TYPES.register(bus);MENUS.register(bus);
    }
    public static ItemStack item(GolemDesign d){var s=new ItemStack(SimpleContent.ITEMS.get("golem").get());s.set(DESIGN.get(),d);return s;}
}
