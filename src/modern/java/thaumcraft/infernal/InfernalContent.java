package thaumcraft.infernal;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.*;
import thaumcraft.*;
import thaumcraft.content.SimpleContent;
public final class InfernalContent{
 private static final DeferredRegister<BlockEntityType<?>> TYPES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,Thaumcraft.MODID);
 public static final RegistryObject<InfernalBlock> FURNACE=Registrar.BLOCKS.register("infernal_furnace",()->new InfernalBlock(props("infernal_furnace").strength(3,20).lightLevel(s->14).noOcclusion()));
 // Catalog icon only: the formed machine is obtained by Salis, never by placing this item.
 public static final RegistryObject<Item> ICON=Registrar.ITEMS.register("infernal_furnace",()->new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM,SimpleContent.id("infernal_furnace"))).useBlockDescriptionPrefix()));
 public static final RegistryObject<InfernalFrameBlock> BRICKS=Registrar.BLOCKS.register("infernal_nether_bricks",()->new InfernalFrameBlock(props("infernal_nether_bricks").requiresCorrectToolForDrops().strength(2,6)));
 public static final RegistryObject<InfernalFrameBlock> OBSIDIAN=Registrar.BLOCKS.register("infernal_obsidian",()->new InfernalFrameBlock(props("infernal_obsidian").requiresCorrectToolForDrops().strength(50,1200)));
 public static final RegistryObject<BlockEntityType<InfernalFurnace>> TYPE=TYPES.register("infernal_furnace",()->new BlockEntityType<>(InfernalFurnace::new,Set.of(FURNACE.get())));
 public static final RegistryObject<BlockEntityType<InfernalFrame>> FRAME_TYPE=TYPES.register("infernal_frame",()->new BlockEntityType<>(InfernalFrame::new,Set.of(BRICKS.get(),OBSIDIAN.get())));
 private static BlockBehaviour.Properties props(String id){return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id(id))).sound(SoundType.STONE);}
 public static void register(BusGroup bus){TYPES.register(bus);}
}
