package thaumcraft.condenser;
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
import thaumcraft.content.*;
public final class CondenserContent{
 private static final DeferredRegister<BlockEntityType<?>> TYPES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,Thaumcraft.MODID);
 public static final RegistryObject<CondenserBlock> BASE=Registrar.BLOCKS.register("condenser",()->new CondenserBlock(props("condenser").strength(2,10)));
 public static final RegistryObject<LatticeBlock> CLEAN=Registrar.BLOCKS.register("condenser_lattice",()->new LatticeBlock(false,props("condenser_lattice").strength(.5F,5).lightLevel(s->5)));
 public static final RegistryObject<LatticeBlock> DIRTY=Registrar.BLOCKS.register("condenser_lattice_dirty",()->new LatticeBlock(true,props("condenser_lattice_dirty").strength(.5F,5)));
 public static final RegistryObject<Item> ITEM=item("condenser",BASE),LATTICE_ITEM=item("condenser_lattice",CLEAN),DIRTY_ITEM=item("condenser_lattice_dirty",DIRTY);
 public static final RegistryObject<BlockEntityType<Condenser>> TYPE=TYPES.register("condenser",()->new BlockEntityType<>(Condenser::new,Set.of(BASE.get())));
 private static BlockBehaviour.Properties props(String id){return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id(id))).sound(SoundType.METAL).noOcclusion();}
 private static RegistryObject<Item> item(String id,RegistryObject<? extends Block> b){RegistryObject<Item> item=Registrar.ITEMS.register(id,()->new BlockItem(b.get(),new Item.Properties().setId(ResourceKey.create(Registries.ITEM,SimpleContent.id(id))).useBlockDescriptionPrefix()));BasicContent.display(item);return item;}
 public static void register(BusGroup b){TYPES.register(b);}
}
