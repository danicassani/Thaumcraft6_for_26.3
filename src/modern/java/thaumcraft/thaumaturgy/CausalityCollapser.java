package thaumcraft.thaumaturgy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import thaumcraft.research.ResearchService;
public final class CausalityCollapser extends Item {
    public CausalityCollapser(Properties p){super(p);}
    public InteractionResult use(Level level,Player p,InteractionHand hand){if(level instanceof ServerLevel l){var item=p.getItemInHand(hand);if(!ResearchService.knows(p,"RIFTCLOSER")||p.getCooldowns().isOnCooldown(item))return InteractionResult.FAIL;var shot=new CausalityProjectile(ThaumaturgyContent.COLLAPSER.get(),l);shot.setOwner(p);shot.setItem(item);shot.setPos(p.getX(),p.getEyeY()-.1,p.getZ());shot.shootFromRotation(p,p.getXRot(),p.getYRot(),0,.8F,0);if(!l.addFreshEntity(shot))return InteractionResult.FAIL;p.getCooldowns().addCooldown(item,20);item.consume(1,p);}return InteractionResult.SUCCESS;}
}
