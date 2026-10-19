package thaumcraft.thaumaturgy;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
public final class PouchMenu extends AbstractContainerMenu {
    private final ItemStack pouch;private final int locked;private final Inventory playerInventory;private boolean ready;private final SimpleContainer storage;
    public PouchMenu(int id,Inventory inv){this(id,inv,inv.getSelectedItem(),inv.getSelectedSlot());}
    public PouchMenu(int id,Inventory inv,ItemStack pouch,int locked){super(ThaumaturgyContent.POUCH_MENU.get(),id);this.pouch=pouch;this.locked=locked;playerInventory=inv;storage=new SimpleContainer(18){public void setChanged(){super.setChanged();if(ready&&pouch.getItem() instanceof FocusPouch&&playerInventory.getItem(locked)==pouch){var all=new java.util.ArrayList<ItemStack>();for(int i=0;i<18;i++)all.add(getItem(i).copy());FocusPouch.save(pouch,all);}}};var saved=FocusPouch.contents(pouch);for(int i=0;i<18;i++)storage.setItem(i,saved.get(i));ready=true;
        for(int i=0;i<18;i++)addSlot(new Slot(storage,i,8+i%9*18,18+i/9*18){public boolean mayPlace(ItemStack s){return s.getItem() instanceof FocusItem;}public int getMaxStackSize(){return 1;}});
        addStandardInventorySlots(inv,8,68);
    }
    public boolean stillValid(Player p){return pouch.getItem() instanceof FocusPouch&&playerInventory.getItem(locked)==pouch;}
    public void clicked(int slot,int button,ContainerInput type,Player p){if(!stillValid(p)||slot>=18&&slot<slots.size()&&slots.get(slot).getContainerSlot()==locked||type==ContainerInput.SWAP&&button==locked)return;super.clicked(slot,button,type,p);}
    public boolean canTakeItemForPickAll(ItemStack s,Slot slot){return !(slot.container==playerInventory&&slot.getContainerSlot()==locked)&&super.canTakeItemForPickAll(s,slot);}
    public ItemStack quickMoveStack(Player p,int i){if(i<0||i>=slots.size()||!stillValid(p))return ItemStack.EMPTY;var slot=slots.get(i);if(i>=18&&slot.getContainerSlot()==locked||!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var old=stack.copy();if(i<18){if(!moveItemStackTo(stack,18,54,true))return ItemStack.EMPTY;}else if(!(stack.getItem() instanceof FocusItem)||!moveItemStackTo(stack,0,18,false))return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();storage.setChanged();return old;}
}
