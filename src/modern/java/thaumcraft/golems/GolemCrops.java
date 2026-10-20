package thaumcraft.golems;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
/** Native crop maturity and paid replanting, including tall plants, wart, cocoa and berries. */
public final class GolemCrops {
 private GolemCrops(){}
 public static boolean mature(ServerLevel l,BlockPos p){var s=l.getBlockState(p);var b=s.getBlock();if(b instanceof CropBlock c)return c.isMaxAge(s);if(b instanceof NetherWartBlock)return s.getValue(NetherWartBlock.AGE)==3;if(b instanceof CocoaBlock)return s.getValue(CocoaBlock.AGE)==2;if(b instanceof SweetBerryBushBlock)return s.getValue(SweetBerryBushBlock.AGE)==3;if(b==Blocks.CACTUS||b==Blocks.SUGAR_CANE||b==Blocks.BAMBOO)return l.hasChunkAt(p.below())&&l.getBlockState(p.below()).is(b);return b==Blocks.MELON||b==Blocks.PUMPKIN;}
 public static Item seed(BlockState s){if(s.getBlock() instanceof CropBlock c)return c.asItem();if(s.getBlock() instanceof NetherWartBlock)return Items.NETHER_WART;if(s.getBlock() instanceof CocoaBlock)return Items.COCOA_BEANS;return Items.AIR;}
 public static BlockState young(BlockState s){if(s.getBlock() instanceof CropBlock c)return c.getStateForAge(0);if(s.getBlock() instanceof NetherWartBlock)return s.setValue(NetherWartBlock.AGE,0);if(s.getBlock() instanceof CocoaBlock)return s.setValue(CocoaBlock.AGE,0);return Blocks.AIR.defaultBlockState();}
 public static BlockPos pending(SealEntity s,ItemStack wanted){if(!s.replant||s.getLevel()==null)return null;return s.replants.entrySet().stream().filter(e->s.getLevel().hasChunkAt(e.getKey())&&s.getLevel().isEmptyBlock(e.getKey())&&GolemTasks.inHarvestArea(s,e.getKey())&&(wanted.isEmpty()||wanted.is(seed(e.getValue())))).map(Map.Entry::getKey).findFirst().orElse(null);}
 public static boolean plant(ServerLevel l,ServerPlayer owner,WorkerGolem worker,SealEntity s,BlockPos at){var desired=s.replants.get(at);if(desired==null||!s.replant||!l.isEmptyBlock(at)||!GolemTasks.inHarvestArea(s,at))return false;var seed=seed(desired);for(int i=0;i<worker.design().capacity();i++)if(worker.cargo.getItem(i).is(seed)){boolean done;if(desired.getBlock() instanceof CocoaBlock){var face=desired.getValue(CocoaBlock.FACING);done=GolemInteraction.plant(l,owner,worker,i,at,at.relative(face),face.getOpposite());}else done=GolemInteraction.plant(l,owner,worker,i,at);if(done&&!l.isEmptyBlock(at)){s.replants.remove(at);s.setChanged();return true;}return false;}return false;}
}
