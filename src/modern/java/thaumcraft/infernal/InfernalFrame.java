package thaumcraft.infernal;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
public final class InfernalFrame extends thaumcraft.devices.AuraDeviceEntity{
 public BlockPos controller;
 public InfernalFrame(BlockPos p,BlockState s){super(InfernalContent.FRAME_TYPE.get(),p,s);}
 public BlockState original(){return (getBlockState().is(InfernalContent.OBSIDIAN.get())?Blocks.OBSIDIAN:Blocks.NETHER_BRICKS).defaultBlockState();}
 public void configure(BlockPos p){controller=p.immutable();sync();}
 public void serverTick(){if(controller!=null&&level.hasChunkAt(controller)&&!(level.getBlockEntity(controller) instanceof InfernalFurnace)){controller=null;level.setBlockAndUpdate(worldPosition,original());}}
 public void preRemoveSideEffects(BlockPos p,BlockState s){super.preRemoveSideEffects(p,s);if(controller!=null&&level instanceof ServerLevel l)InfernalStructure.dismantle(l,controller,p);}
 protected void saveAdditional(ValueOutput o){super.saveAdditional(o);if(controller!=null)o.putLong("controller",controller.asLong());}
 protected void loadAdditional(ValueInput i){super.loadAdditional(i);controller=i.getLong("controller").map(BlockPos::of).filter(p->Math.abs(p.getX()-worldPosition.getX())<=1&&Math.abs(p.getY()-worldPosition.getY())<=1&&Math.abs(p.getZ()-worldPosition.getZ())<=1).orElse(null);}
}
