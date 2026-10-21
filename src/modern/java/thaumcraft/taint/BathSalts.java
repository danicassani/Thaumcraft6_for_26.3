package thaumcraft.taint;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
public final class BathSalts extends Item{
 public BathSalts(Properties p){super(p);}
 public boolean onEntityItemUpdate(ItemStack stack,ItemEntity item){if(item.level() instanceof ServerLevel l&&item.tickCount>=200){var p=item.blockPosition();var s=l.getBlockState(p);if(s.is(Blocks.WATER)&&s.getFluidState().isSource()&&TaintWorld.place(l,p,p,TaintContent.PURE.block.get().defaultBlockState(),null)){if(stack.getCount()>1){stack.shrink(1);item.setItem(stack);item.tickCount=0;return false;}item.discard();return true;}}return false;}
 public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext c){var l=c.getLevel();var p=c.getClickedPos();if(!l.getBlockState(p).is(Blocks.WATER)||!l.getFluidState(p).isSource())return net.minecraft.world.InteractionResult.PASS;if(l instanceof ServerLevel s&&c.getPlayer()!=null&&TaintWorld.place(s,p,p,TaintContent.PURE.block.get().defaultBlockState(),c.getPlayer()))c.getItemInHand().consume(1,c.getPlayer());return net.minecraft.world.InteractionResult.SUCCESS;}
}
