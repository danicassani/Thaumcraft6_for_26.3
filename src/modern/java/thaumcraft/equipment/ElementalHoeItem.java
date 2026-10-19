package thaumcraft.equipment;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
/** Growth is an actual bonemeal operation; Forge bonemeal/protection rules still apply. */
public final class ElementalHoeItem extends Item {
    public ElementalHoeItem(Properties p){super(p);}
    @Override public InteractionResult useOn(UseOnContext c){var p=c.getPlayer();var l=c.getLevel();var q=c.getClickedPos();if(p!=null&&p.isShiftKeyDown()&&l.mayInteract(p,q)&&p.mayUseItemAt(q,c.getClickedFace(),c.getItemInHand())){if(l.isClientSide())return InteractionResult.SUCCESS;if(l instanceof ServerLevel&&BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL),l,q)){c.getItemInHand().hurtAndBreak(1,p,c.getHand());l.levelEvent(1505,q,0);return InteractionResult.SUCCESS;}}return super.useOn(c);}
}
