package thaumcraft.mirrors;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
public final class HandMirrorItem extends Item {
 public HandMirrorItem(Properties p){super(p);}
 public boolean isFoil(ItemStack stack){return stack.has(MirrorContent.LINK.get())||super.isFoil(stack);}

 public InteractionResult onItemUseFirst(ItemStack stack,UseOnContext c){if(!(c.getLevel().getBlockEntity(c.getClickedPos()) instanceof MirrorEntity mirror)||mirror.essence())return InteractionResult.PASS;var p=c.getPlayer();if(p==null||!c.getLevel().mayInteract(p,c.getClickedPos())||!p.mayUseItemAt(c.getClickedPos(),c.getClickedFace(),stack))return InteractionResult.FAIL;if(!c.getLevel().isClientSide()){stack.set(MirrorContent.LINK.get(),mirror.address());p.getInventory().setChanged();}return InteractionResult.SUCCESS;}
 public InteractionResult use(Level level,Player p,InteractionHand hand){if(hand!=InteractionHand.MAIN_HAND||p.isSpectator()||!p.isAlive())return InteractionResult.PASS;var stack=p.getItemInHand(hand);if(!stack.has(MirrorContent.LINK.get()))return InteractionResult.FAIL;if(p instanceof ServerPlayer real)real.openMenu(new SimpleMenuProvider((id,inv,user)->new HandMirrorMenu(id,inv,stack,inv.getSelectedSlot()),Component.translatable("item.thaumcraft.hand_mirror")));return InteractionResult.SUCCESS;}
}
