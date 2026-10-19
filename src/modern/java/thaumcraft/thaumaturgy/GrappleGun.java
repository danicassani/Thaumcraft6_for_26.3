package thaumcraft.thaumaturgy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import thaumcraft.api.items.*;
import thaumcraft.research.ResearchService;
public final class GrappleGun extends Item implements IRechargable {
    public GrappleGun(Properties p){super(p.stacksTo(1));}
    public int getMaxCharge(ItemStack s,LivingEntity e){return 100;}
    public InteractionResult use(Level level,Player player,InteractionHand hand){if(player instanceof ServerPlayer p){var gun=p.getItemInHand(hand);if(!ResearchService.knows(p,"GRAPPLEGUN"))return InteractionResult.FAIL;boolean released=false;for(var e:p.level().getAllEntities())if(e instanceof GrappleProjectile g&&g.getOwner()==p){g.discard();released=true;}if(released||p.isShiftKeyDown())return InteractionResult.SUCCESS;if(p.getCooldowns().isOnCooldown(gun)||RechargeHelper.getCharge(gun)<1)return InteractionResult.FAIL;var g=new GrappleProjectile(ThaumaturgyContent.GRAPPLE.get(),p.level());g.setOwner(p);g.setPos(p.getEyePosition());g.shootFromRotation(p,p.getXRot(),p.getYRot(),0,1.5F,0);if(!p.level().addFreshEntity(g))return InteractionResult.FAIL;RechargeHelper.consumeCharge(gun,p,1);p.getCooldowns().addCooldown(gun,10);}return InteractionResult.SUCCESS;}
}
