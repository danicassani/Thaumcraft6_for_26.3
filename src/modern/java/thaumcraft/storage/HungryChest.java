package thaumcraft.storage;
import java.util.Set;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.*;
import thaumcraft.Registrar;
import thaumcraft.content.*;
/** A single native chest; only physically touching dropped stacks are offered to its inventory. */
public final class HungryChest extends ChestBlockEntity {
 private static final DeferredRegister<BlockEntityType<?>> TYPES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,"thaumcraft");
 public static final RegistryObject<Block> BLOCK=Registrar.BLOCKS.register("hungry_chest",()->new HungryChestBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id("hungry_chest"))).strength(2.5F).sound(SoundType.WOOD).noOcclusion().ignitedByLava()));
 public static final RegistryObject<BlockEntityType<HungryChest>> TYPE=TYPES.register("hungry_chest",()->new BlockEntityType<>(HungryChest::new,Set.of(BLOCK.get())));
 public static void register(net.minecraftforge.eventbus.api.bus.BusGroup bus){TYPES.register(bus);SimpleContent.BLOCKS.put("hungry_chest",BLOCK);SimpleContent.item("hungry_chest",p->new BlockItem(BLOCK.get(),p.useBlockDescriptionPrefix()));}
 public HungryChest(BlockPos pos,BlockState state){super(TYPE.get(),pos,state);}
 protected Component getDefaultName(){return Component.translatable("block.thaumcraft.hungry_chest");}
 public void collect(){if(!(level instanceof ServerLevel l)||isRemoved())return;var box=new AABB(worldPosition).deflate(.0625,0,.0625);for(var entity:l.getEntitiesOfClass(ItemEntity.class,box,e->e.isAlive()&&!e.getItem().isEmpty()).stream().limit(64).toList())absorb(entity);}
 public boolean absorb(ItemEntity entity){if(!(level instanceof ServerLevel)||isRemoved()||!entity.isAlive()||entity.level()!=level||!entity.getBoundingBox().intersects(new AABB(worldPosition).deflate(.0625,0,.0625)))return false;var original=entity.getItem();var handler=getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER,Direction.UP).orElse(null);if(handler==null)return false;var rest=net.minecraftforge.items.ItemHandlerHelper.insertItemStacked(handler,original.copy(),false);if(rest.getCount()==original.getCount())return false;if(rest.isEmpty())entity.discard();else entity.setItem(rest);setChanged();level.updateNeighbourForOutputSignal(worldPosition,getBlockState().getBlock());level.playSound(null,worldPosition,net.minecraft.sounds.SoundEvents.GENERIC_EAT.value(),net.minecraft.sounds.SoundSource.BLOCKS,.25F,1);return true;}
}
