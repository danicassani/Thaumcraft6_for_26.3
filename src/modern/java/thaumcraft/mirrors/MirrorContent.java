package thaumcraft.mirrors;
import java.util.Set;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.*;
import thaumcraft.Registrar;
import thaumcraft.content.SimpleContent;
public final class MirrorContent {
 private static final DeferredRegister<BlockEntityType<?>> TYPES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,"thaumcraft");
 private static final DeferredRegister<DataComponentType<?>> COMPONENTS=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,"thaumcraft");
 private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,"thaumcraft");
 public static final RegistryObject<DataComponentType<MirrorAddress>> LINK=COMPONENTS.register("mirror_link",()->DataComponentType.<MirrorAddress>builder().persistent(MirrorAddress.CODEC).networkSynchronized(ByteBufCodecs.fromCodec(MirrorAddress.CODEC)).build());
 public static final RegistryObject<Block> MIRROR=block("mirror",false),ESSENTIA=block("mirror_essentia",true);
 public static final RegistryObject<BlockEntityType<MirrorEntity>> TYPE=TYPES.register("mirror",()->new BlockEntityType<>(MirrorEntity::new,Set.of(MIRROR.get(),ESSENTIA.get())));
 public static final RegistryObject<MenuType<HandMirrorMenu>> MENU=MENUS.register("hand_mirror",()->new MenuType<>(HandMirrorMenu::new,FeatureFlags.VANILLA_SET));
 private static RegistryObject<Block> block(String name,boolean essence){RegistryObject<Block> b=Registrar.BLOCKS.register(name,()->new MirrorBlock(essence,BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id(name))).strength(.1F).sound(SoundType.GLASS).noOcclusion().noCollision().pushReaction(net.minecraft.world.level.material.PushReaction.POPPED)));SimpleContent.BLOCKS.put(name,b);SimpleContent.item(name,p->new MirrorItem(b.get(),p.stacksTo(1).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));return b;}
 public static void register(net.minecraftforge.eventbus.api.bus.BusGroup bus){TYPES.register(bus);COMPONENTS.register(bus);MENUS.register(bus);SimpleContent.item("hand_mirror",p->new HandMirrorItem(p.stacksTo(1).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));net.minecraftforge.event.entity.player.ItemTooltipEvent.BUS.addListener(e->{var stack=e.getItemStack();if(stack.getItem() instanceof MirrorItem)e.getToolTip().add(net.minecraft.network.chat.Component.translatable("mirror.thaumcraft.controls"));if(stack.getItem() instanceof HandMirrorItem)e.getToolTip().add(net.minecraft.network.chat.Component.translatable("mirror.thaumcraft.hand_controls"));var link=stack.get(LINK.get());if(link!=null){var pos=link.location().pos();e.getToolTip().add(net.minecraft.network.chat.Component.translatable("mirror.thaumcraft.link",link.location().dimension().identifier().toString(),pos.getX(),pos.getY(),pos.getZ()));}});}
}
