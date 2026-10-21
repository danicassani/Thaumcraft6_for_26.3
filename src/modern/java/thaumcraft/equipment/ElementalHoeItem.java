package thaumcraft.equipment;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.*;
/** Native soil transformations in 3x3; sneaking selects one block. */
public final class ElementalHoeItem extends Item {
 public ElementalHoeItem(Properties p){super(p);}
 public InteractionResult useOn(UseOnContext c){var p=c.getPlayer();if(p==null||p.isSpectator())return InteractionResult.PASS;var l=c.getLevel();var origin=c.getClickedPos();int radius=p.isShiftKeyDown()?0:1;boolean changed=false;for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++){var q=origin.offset(x,0,z);if(c.getItemInHand().isEmpty())return changed?InteractionResult.SUCCESS:InteractionResult.FAIL;if(!l.hasChunkAt(q)||!l.mayInteract(p,q)||!p.mayUseItemAt(q,c.getClickedFace(),c.getItemInHand()))continue;var hit=new BlockHitResult(Vec3.atCenterOf(q),c.getClickedFace(),q,c.isInside());if(super.useOn(new UseOnContext(l,p,c.getHand(),c.getItemInHand(),hit)).consumesAction())changed=true;}
  if(!changed&&l.mayInteract(p,origin)&&p.mayUseItemAt(origin,c.getClickedFace(),c.getItemInHand())){if(l.isClientSide())return InteractionResult.SUCCESS;if(l instanceof ServerLevel&&BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL),l,origin)){c.getItemInHand().hurtAndBreak(3,p,c.getHand());l.levelEvent(1505,origin,0);changed=true;}}return changed?InteractionResult.SUCCESS:InteractionResult.PASS;
 }
}
