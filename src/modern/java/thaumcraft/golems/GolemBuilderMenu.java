package thaumcraft.golems;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public final class GolemBuilderMenu extends AbstractContainerMenu {
    private final GolemBuilder builder;private final ContainerData data;
    public GolemBuilderMenu(int id,Inventory inv){this(id,inv,null);}
    public GolemBuilderMenu(int id,Inventory inv,GolemBuilder b){super(GolemContent.BUILDER_MENU.get(),id);builder=b;data=b==null?new SimpleContainerData(8):new ContainerData(){public int get(int i){return switch(i){case 0->b.draft.material();case 1->b.draft.head();case 2->b.draft.arms();case 3->b.draft.addon();case 4->b.working()?1:0;case 5->b.remaining();case 6->b.total();case 7->b.draft.legs();default->0;};}public void set(int i,int v){}public int getCount(){return 8;}};addSlot(new Slot(b==null?new SimpleContainer(1):b.output,0,144,110){public boolean mayPlace(ItemStack s){return false;}});addStandardInventorySlots(inv,8,156);addDataSlots(data);}
    public int value(int i){return data.get(i);}public GolemDesign design(){return new GolemDesign(value(0),value(1),value(2),value(3),value(7));}
    public boolean clickMenuButton(Player p,int b){return builder!=null&&stillValid(p)&&builder.edit(p,b);}
    public boolean stillValid(Player p){return builder==null||builder.valid(p);}
    public ItemStack quickMoveStack(Player p,int i){if(i!=0)return ItemStack.EMPTY;var slot=slots.get(0);var s=slot.getItem();var old=s.copy();if(s.isEmpty()||!moveItemStackTo(s,1,37,true))return ItemStack.EMPTY;if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return old;}
}
