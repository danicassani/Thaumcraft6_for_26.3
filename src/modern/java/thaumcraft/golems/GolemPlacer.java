package thaumcraft.golems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
public final class GolemPlacer extends Item {
    public GolemPlacer(Properties p){super(p.stacksTo(1));}
    public InteractionResult useOn(UseOnContext c){if(!(c.getLevel() instanceof ServerLevel l))return InteractionResult.SUCCESS;var p=c.getPlayer();var design=c.getItemInHand().getOrDefault(GolemContent.DESIGN.get(),GolemDesign.BASIC);if(p==null||!design.knows(p))return InteractionResult.FAIL;var at=c.getClickedPos().relative(c.getClickedFace());if(!l.hasChunkAt(at)||!l.mayInteract(p,at)||!p.mayUseItemAt(at,c.getClickedFace(),c.getItemInHand()))return InteractionResult.FAIL;var g=new WorkerGolem(GolemContent.GOLEM.get(),l);g.configure(p,design,at,c.getItemInHand().getOrDefault(GolemContent.CHANNEL.get(),0));g.restoreProgress(c.getItemInHand().getOrDefault(GolemContent.PROGRESS.get(),GolemProgress.EMPTY));if(c.getItemInHand().get(net.minecraft.core.component.DataComponents.CUSTOM_NAME)!=null)g.setCustomName(c.getItemInHand().get(net.minecraft.core.component.DataComponents.CUSTOM_NAME));g.setHealth(g.getMaxHealth()*c.getItemInHand().getOrDefault(GolemContent.HEALTH.get(),1F));if(!l.noCollision(g,g.getBoundingBox())||!l.addFreshEntity(g))return InteractionResult.FAIL;c.getItemInHand().consume(1,p);return InteractionResult.SUCCESS;}
}
