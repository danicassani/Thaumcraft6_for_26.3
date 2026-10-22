package thaumcraft.utility;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public final class UtilityMenu extends AbstractContainerMenu {
 private final InventoryDevice device;private final Container inventory;private final ContainerData data=new SimpleContainerData(6);
 public UtilityMenu(int id,Inventory inv){this(id,inv,null);}public UtilityMenu(int id,Inventory inv,InventoryDevice device){super(UtilityContent.MENU.get(),id);this.device=device;inventory=device==null?new SimpleContainer(1):device.inventory;addSlot(new Slot(inventory,0,80,24){public boolean mayPlace(ItemStack s){return device==null||device.supported()&&device.accepts(s);}public boolean mayPickup(Player p){return device==null||device.supported();}public int getMaxStackSize(){return value(0)==1?1:64;}});addStandardInventorySlots(inv,8,84);addDataSlots(data);refresh();}
 private void refresh(){if(device==null)return;data.set(0,device instanceof SprayerEntity?1:0);data.set(1,device.supported()?1:0);if(device instanceof WaterDevice water){data.set(2,water.amount());data.set(3,water.mix()?1:0);}else if(device instanceof SprayerEntity sprayer){data.set(2,sprayer.charges());data.set(3,thaumcraft.alchemy.AspectAmounts.total(sprayer.progress()));data.set(4,thaumcraft.alchemy.AspectAmounts.total(sprayer.recipe()));}}
 public int value(int i){return data.get(i);}public void broadcastChanges(){refresh();super.broadcastChanges();}public boolean stillValid(Player p){return device==null||device.valid(p);}public boolean clickMenuButton(Player p,int button){if(button!=0||device==null||!stillValid(p)||!(device instanceof WaterDevice water))return false;water.toggle();refresh();return true;}
 public ItemStack quickMoveStack(Player p,int index){if(index<0||index>=slots.size()||!stillValid(p)||value(1)!=1)return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var copy=s.copy();if(index==0){if(!moveItemStackTo(s,1,37,true))return ItemStack.EMPTY;}else if(!moveItemStackTo(s,0,1,false))return ItemStack.EMPTY;if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return copy;}
}
