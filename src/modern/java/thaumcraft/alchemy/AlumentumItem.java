package thaumcraft.alchemy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
public final class AlumentumItem extends Item {
    public AlumentumItem(Properties p){super(p);}
    @Override public InteractionResult use(Level level,Player p,InteractionHand hand){if(level instanceof ServerLevel server){var shot=new AlumentumProjectile(AlchemyContent.PROJECTILE.get(),level);shot.setPos(p.getX(),p.getEyeY()-.1,p.getZ());shot.setOwner(p);shot.setItem(p.getItemInHand(hand));shot.shootFromRotation(p,p.getXRot(),p.getYRot(),-5,.75F,1);server.addFreshEntity(shot);p.getCooldowns().addCooldown(p.getItemInHand(hand),10);p.getItemInHand(hand).consume(1,p);}return InteractionResult.SUCCESS;}
}
