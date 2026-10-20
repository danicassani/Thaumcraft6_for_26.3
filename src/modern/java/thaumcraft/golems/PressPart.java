package thaumcraft.golems;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import thaumcraft.devices.AuraDeviceEntity;
public final class PressPart extends AuraDeviceEntity {
 public BlockPos controller;public BlockState original=Blocks.IRON_BARS.defaultBlockState();
 public PressPart(BlockPos p,BlockState s){super(GolemContent.PRESS_PART_TYPE.get(),p,s);}
 public void configure(BlockPos p,BlockState old){controller=p.immutable();original=old;sync();}
 public void serverTick(){if(controller!=null&&level.hasChunkAt(controller)&&!(level.getBlockEntity(controller) instanceof GolemBuilder)){controller=null;level.setBlockAndUpdate(worldPosition,original);}}
 public void preRemoveSideEffects(BlockPos p,BlockState s){super.preRemoveSideEffects(p,s);if(controller!=null&&level instanceof ServerLevel l){var center=controller;controller=null;Block.popResource(l,p,new net.minecraft.world.item.ItemStack(original.getBlock()));GolemPress.dismantle(l,center,p);}}
 protected void saveAdditional(ValueOutput o){super.saveAdditional(o);if(controller!=null)o.putLong("controller",controller.asLong());o.store("original",BlockState.CODEC,original);}
 protected void loadAdditional(ValueInput i){super.loadAdditional(i);controller=i.getLong("controller").map(BlockPos::of).orElse(null);original=i.read("original",BlockState.CODEC).filter(s->s.is(Blocks.IRON_BARS)||s.is(Blocks.CAULDRON)||s.is(Blocks.ANVIL)||s.is(thaumcraft.content.SimpleContent.BLOCKS.get("table_stone").get())).orElse(Blocks.IRON_BARS.defaultBlockState());}
}
