package thaumcraft.thaumaturgy;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Direction;
public final class TurretItem extends BlockItem {
 private final boolean advanced;
 public TurretItem(Properties p,boolean advanced){super((advanced?ThaumaturgyContent.TURRET_ADVANCED:ThaumaturgyContent.TURRET).get(),p.stacksTo(1).useBlockDescriptionPrefix());this.advanced=advanced;}
 public InteractionResult useOn(UseOnContext c){var p=c.getPlayer();var l=c.getLevel();var clicked=c.getClickedPos();var q=l.getBlockState(clicked).canBeReplaced()?clicked:clicked.relative(c.getClickedFace());if(p==null||c.getClickedFace()==Direction.DOWN||!l.hasChunkAt(q)||!l.hasChunkAt(q.above())||!l.mayInteract(p,q)||!p.mayUseItemAt(q,c.getClickedFace(),c.getItemInHand()))return InteractionResult.FAIL;
  if(l instanceof ServerLevel server){if(!thaumcraft.research.ResearchService.knows(p,advanced?"ADVANCEDTURRET":"BASICTURRET"))return InteractionResult.FAIL;var t=new CrossbowTurret((advanced?TurretContent.ADVANCED:TurretContent.BASIC).get(),server);t.setPos(q.getX()+.5,q.getY(),q.getZ()+.5);t.configure(p.getUUID(),2);t.setYRot(p.getYRot());t.setYHeadRot(p.getYRot());if(!l.noCollision(t,t.getBoundingBox())||!l.getEntities(t,t.getBoundingBox()).isEmpty()||!server.addFreshEntity(t))return InteractionResult.FAIL;c.getItemInHand().consume(1,p);server.playSound(null,q,net.minecraft.sounds.SoundEvents.ARMOR_STAND_PLACE,net.minecraft.sounds.SoundSource.BLOCKS,.75F,.8F);}return InteractionResult.SUCCESS;
 }
}
