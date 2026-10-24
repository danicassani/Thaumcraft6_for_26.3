package thaumcraft.bore;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
public final class BoreMenu extends AbstractContainerMenu {
 private final ContainerData data;private final Container tool;private final ArcaneBore bore;
 public BoreMenu(int id,Inventory inv){this(id,inv,new SimpleContainer(1),null);}
 public BoreMenu(int id,Inventory inv,Container tool,ArcaneBore bore){super(BoreContent.MENU.get(),id);this.tool=tool;this.bore=bore;data=bore==null?new SimpleContainerData(4):new ContainerData(){public int get(int n){return switch(n){case 0->Math.round(bore.charge()*100);case 1->bore.radius();case 2->bore.depth();default->Math.round(100*bore.getHealth()/bore.getMaxHealth());};}public void set(int n,int v){}public int getCount(){return 4;}};addDataSlots(data);addSlot(new Slot(tool,0,80,29){public boolean mayPlace(ItemStack s){return ArcaneBore.pickaxe(s);}public int getMaxStackSize(){return 1;}});addStandardInventorySlots(inv,8,84);}
 public int value(int n){return data.get(n);}
 public boolean stillValid(Player p){return bore==null||bore.isAlive()&&bore.owned(p)&&p.distanceToSqr(bore)<64;}
 public ItemStack quickMoveStack(Player p,int i){if(!stillValid(p)||i<0||i>=slots.size())return ItemStack.EMPTY;var slot=slots.get(i);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var old=s.copy();if(i==0?!moveItemStackTo(s,1,37,true):!ArcaneBore.pickaxe(s)||!moveItemStackTo(s,0,1,false))return ItemStack.EMPTY;if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return old;}
}
