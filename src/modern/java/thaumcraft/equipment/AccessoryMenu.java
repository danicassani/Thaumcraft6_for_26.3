package thaumcraft.equipment;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public final class AccessoryMenu extends AbstractContainerMenu {
 private final Container storage;
 public AccessoryMenu(int id,Inventory inv){this(id,inv,new SimpleContainer(7));}
 public AccessoryMenu(int id,Inventory inv,Container storage){super(AccessoryMenuAccess.MENU.get(),id);this.storage=storage;for(int i=0;i<7;i++){final int index=i;addSlot(new Slot(storage,i,26+i*18,28){public boolean mayPlace(ItemStack s){return AccessorySlots.accepts(index,s);}public int getMaxStackSize(){return 1;}});}addStandardInventorySlots(inv,8,74);}
 public boolean stillValid(Player p){return p.isAlive()&&!p.isSpectator()&&(!(p instanceof net.minecraft.server.level.ServerPlayer real)||storage instanceof AccessorySlots.Storage s&&s.current()&&AccessorySlots.storage(real)==s);}
 public ItemStack quickMoveStack(Player p,int i){if(i<0||i>=slots.size()||!stillValid(p))return ItemStack.EMPTY;var slot=slots.get(i);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var old=stack.copy();if(i<7){if(!moveItemStackTo(stack,7,43,true))return ItemStack.EMPTY;}else{boolean moved=false;for(int n=0;n<7;n++)if(AccessorySlots.accepts(n,stack)&&!slots.get(n).hasItem()){moved=moveItemStackTo(stack,n,n+1,false);if(moved)break;}if(!moved)return ItemStack.EMPTY;}if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();storage.setChanged();return old;}
 public void clicked(int slot,int button,ContainerInput type,Player p){if(stillValid(p))super.clicked(slot,button,type,p);}
}
