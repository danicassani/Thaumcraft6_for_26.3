package thaumcraft.theory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
public final class ScribingToolsItem extends Item {
    public ScribingToolsItem(Properties p){super(p);}
    @Override public InteractionResult useOn(UseOnContext c){return ResearchTableBlock.convert(c);}
}
