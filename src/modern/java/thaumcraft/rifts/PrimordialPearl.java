package thaumcraft.rifts;

import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomModelData;
import java.util.List;
import net.minecraft.world.item.*;
import thaumcraft.content.SimpleContent;

/** The original eight states; consuming a reagent returns one progressively worn pearl. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="thaumcraft")
public final class PrimordialPearl extends Item {
    @net.minecraftforge.eventbus.api.listener.SubscribeEvent
    public static boolean anvil(net.minecraftforge.event.AnvilUpdateEvent e){return e.getLeft().getItem() instanceof PrimordialPearl||e.getRight().getItem() instanceof PrimordialPearl;}
    public PrimordialPearl(Properties p) {super(p.stacksTo(1).rarity(Rarity.UNCOMMON));}
    public static ItemStack worn(int wear) {
        if(wear<0||wear>7)throw new IllegalArgumentException("Pearl wear outside 0..7");
        var stack=new ItemStack(SimpleContent.ITEMS.get("primordial_pearl").get());setWear(stack,wear);return stack;
    }
    public static int wear(ItemStack s){var data=s.get(DataComponents.CUSTOM_MODEL_DATA);if(data==null)return 0;Float f=data.getFloat(0);return f!=null&&Float.isFinite(f)&&f>=0&&f<=7&&f==(int)(float)f?(int)(float)f:7;}
    private static void setWear(ItemStack s,int n){s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(List.of((float)n),List.of(),List.of(),List.of()));}
    public static int remaining(ItemStack stack) {return 8-wear(stack);}
    @Override public ItemStackTemplate getCraftingRemainder(ItemStack stack) {
        if(wear(stack)>=7)return null;
        var result=stack.copyWithCount(1);setWear(result,wear(stack)+1);
        return ItemStackTemplate.fromNonEmptyStack(result);
    }
    @Override public boolean isBarVisible(ItemStack s){return wear(s)>0;}
    @Override public int getBarWidth(ItemStack s){return Math.round(13F*remaining(s)/8);}
    @Override public int getBarColor(ItemStack s){return 0xb45eff;}
    @Override public Component getName(ItemStack s) {return Component.translatable("item.thaumcraft.primordial_pearl."+(wear(s)<3?"pearl":wear(s)<6?"nodule":"mote"));}
}
