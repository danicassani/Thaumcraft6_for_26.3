package thaumcraft.banners;

import java.util.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.*;
import thaumcraft.*;
import thaumcraft.content.*;

public final class BannerContent {
 private static final DeferredRegister<BlockEntityType<?>> TYPES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,Thaumcraft.MODID);
 private static final DeferredRegister<DataComponentType<?>> COMPONENTS=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,Thaumcraft.MODID);
 public static final RegistryObject<DataComponentType<String>> ASPECT=COMPONENTS.register("banner_aspect",()->DataComponentType.<String>builder().persistent(thaumcraft.essentia.TubeState.FILTER).networkSynchronized(ByteBufCodecs.fromCodec(thaumcraft.essentia.TubeState.FILTER)).build());
 public static final Map<DyeColor,RegistryObject<AspectBannerBlock>> COLOURS=new LinkedHashMap<>();
 static {for(var dye:DyeColor.values())COLOURS.put(dye,block(id(dye),dye));}
 public static final RegistryObject<AspectBannerBlock> CULT=block("banner_crimson_cult",null);
 public static final RegistryObject<BlockEntityType<AspectBannerEntity>> TYPE=TYPES.register("banner",()->{var blocks=new HashSet<net.minecraft.world.level.block.Block>();COLOURS.values().forEach(b->blocks.add(b.get()));blocks.add(CULT.get());return new BlockEntityType<>(AspectBannerEntity::new,blocks);});
 public static String id(DyeColor d){return BannerDesign.id(d);}
 private static RegistryObject<AspectBannerBlock> block(String id,DyeColor dye){var b=Registrar.BLOCKS.register(id,()->new AspectBannerBlock(dye,BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id(id))).strength(1).mapColor(dye==null?net.minecraft.world.level.material.MapColor.COLOR_RED:dye.getMapColor()).sound(SoundType.WOOD).noOcclusion().noCollision()));var item=Registrar.ITEMS.register(id,()->new BlockItem(b.get(),new Item.Properties().setId(ResourceKey.create(Registries.ITEM,SimpleContent.id(id))).useBlockDescriptionPrefix()));BasicContent.display(item);return b;}
 public static void register(BusGroup bus){TYPES.register(bus);COMPONENTS.register(bus);net.minecraftforge.event.entity.player.ItemTooltipEvent.BUS.addListener(e->{var a=e.getItemStack().get(ASPECT.get());if(a!=null&&!a.isEmpty())e.getToolTip().add(net.minecraft.network.chat.Component.translatable("tc.aspect."+a));});}
 private BannerContent(){}
}
