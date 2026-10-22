package thaumcraft.mirrors;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
public final class MirrorItem extends BlockItem {
 public MirrorItem(Block b,Properties p){super(b,p);}
 public boolean isFoil(ItemStack stack){return stack.has(MirrorContent.LINK.get())||super.isFoil(stack);}

 public InteractionResult onItemUseFirst(ItemStack stack,UseOnContext c){if(!(c.getLevel().getBlockEntity(c.getClickedPos()) instanceof MirrorEntity target)||target.essence()!=((MirrorBlock)getBlock()).essence)return InteractionResult.PASS;var p=c.getPlayer();if(p==null||!c.getLevel().mayInteract(p,c.getClickedPos())||!p.mayUseItemAt(c.getClickedPos(),c.getClickedFace(),stack))return InteractionResult.FAIL;if(c.getLevel().isClientSide())return InteractionResult.SUCCESS;if(!target.available())return InteractionResult.FAIL;stack.set(MirrorContent.LINK.get(),target.address());p.getInventory().setChanged();return InteractionResult.SUCCESS;}
 public InteractionResult useOn(UseOnContext c){return super.useOn(c);}
}
