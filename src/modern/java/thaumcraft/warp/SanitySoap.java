package thaumcraft.warp;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import thaumcraft.persistence.PlayerWarpStore;
import thaumcraft.taint.TaintContent;
public final class SanitySoap extends Item {
 public SanitySoap(Properties p){super(p);}
 public int getUseDuration(ItemStack s,LivingEntity e){return 100;}
 public ItemUseAnimation getUseAnimation(ItemStack s){return ItemUseAnimation.BLOCK;}
 public InteractionResult use(Level l,Player p,InteractionHand h){p.startUsingItem(h);return InteractionResult.CONSUME;}
 public void onUseTick(Level l,LivingEntity e,ItemStack s,int remaining){if(l instanceof net.minecraft.server.level.ServerLevel server&&remaining%4==0)server.sendParticles(net.minecraft.core.particles.ParticleTypes.BUBBLE_POP,e.getX(),e.getY()+.8,e.getZ(),6,.4,.7,.4,.01);}
 public static boolean clean(ServerPlayer p,ItemStack s){if(!(s.getItem() instanceof SanitySoap)||s.isEmpty()||!p.isAlive())return false;var old=PlayerWarpStore.read(p).result();if(old.isEmpty())return false;var l=p.level();boolean bath=l.hasChunkAt(p.blockPosition())&&l.getBlockState(p.blockPosition()).is(TaintContent.PURE.block.get());if(PlayerWarpStore.write(p,WarpRules.clean(old.get(),p.hasEffect(TaintContent.WARD.getHolder().orElseThrow()),bath)).result().isEmpty())return false;s.shrink(1);p.getInventory().setChanged();l.sendParticles(net.minecraft.core.particles.ParticleTypes.BUBBLE_POP,p.getX(),p.getY()+.8,p.getZ(),40,.5,.8,.5,.03);p.playSound(net.minecraft.sounds.SoundEvents.CHORUS_FLOWER_DEATH,.25F,1.5F);return true;}
 public boolean releaseUsing(ItemStack s,Level l,LivingEntity e,int remaining){return 100-remaining>=96&&e instanceof ServerPlayer p&&clean(p,s);}
 public ItemStack finishUsingItem(ItemStack s,Level l,LivingEntity e){if(e instanceof ServerPlayer p)clean(p,s);return s;}
}
