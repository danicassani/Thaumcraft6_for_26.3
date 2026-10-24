package thaumcraft.bore;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.server.level.ServerLevel;
public final class BoreItem extends Item {
 public BoreItem(Properties p){super(p.stacksTo(1));}
 public InteractionResult useOn(UseOnContext c){var p=c.getPlayer();var q=c.getClickedPos().relative(c.getClickedFace());if(p==null||!p.mayUseItemAt(q,c.getClickedFace(),c.getItemInHand())||!c.getLevel().mayInteract(p,q))return InteractionResult.FAIL;
  if(c.getLevel() instanceof ServerLevel l){if(!thaumcraft.research.ResearchService.knows(p,"ARCANEBORE"))return InteractionResult.FAIL;var b=new ArcaneBore(BoreContent.TYPE.get(),l);b.setPos(q.getX()+.5,q.getY(),q.getZ()+.5);b.configure(p.getUUID(),p.getDirection());if(!l.noCollision(b,b.getBoundingBox())||!l.addFreshEntity(b))return InteractionResult.FAIL;if(!p.isCreative())c.getItemInHand().shrink(1);}return InteractionResult.SUCCESS;
 }
}
