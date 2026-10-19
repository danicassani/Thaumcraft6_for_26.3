package thaumcraft.thaumaturgy;
import net.minecraft.world.item.*;
public final class FocusItem extends Item {
    public final int capacity;
    public FocusItem(Properties p,int capacity){super(p.stacksTo(1));this.capacity=capacity;}
}
