package thaumcraft.alchemy;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
public final class PhialItem extends Item {
    public PhialItem(Properties p){super(p);}
    @Override public InteractionResult useOn(UseOnContext c){if(c.getPlayer()!=null&&c.getLevel().getBlockEntity(c.getClickedPos()) instanceof AlchemyMachine m)return m.phial(c.getPlayer(),c.getHand());if(c.getPlayer()!=null&&c.getLevel().getBlockEntity(c.getClickedPos()) instanceof thaumcraft.banners.AspectBannerEntity b)return b.decorate(c.getPlayer(),c.getHand());return InteractionResult.PASS;}
}
