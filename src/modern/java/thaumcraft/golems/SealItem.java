package thaumcraft.golems;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import thaumcraft.research.ResearchService;
public final class SealItem extends BlockItem {
    public SealItem(SealBlock b,Properties p){super(b,p);}
    public InteractionResult useOn(UseOnContext c){if(!c.getLevel().isClientSide()&&(c.getPlayer()==null||!ResearchService.knows(c.getPlayer(),((SealBlock)getBlock()).kind.research)))return InteractionResult.FAIL;return super.useOn(c);}
}
