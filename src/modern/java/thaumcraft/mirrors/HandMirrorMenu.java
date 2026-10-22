package thaumcraft.mirrors;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public final class HandMirrorMenu extends AbstractContainerMenu {
 private final Inventory inventory;private final ItemStack mirror;private final int locked;private final MirrorAddress link;private final SimpleContainer input=new SimpleContainer(1);
 public HandMirrorMenu(int id,Inventory inv){this(id,inv,inv.getSelectedItem(),inv.getSelectedSlot());}
 public HandMirrorMenu(int id,Inventory inv,ItemStack mirror,int slot){super(MirrorContent.MENU.get(),id);inventory=inv;this.mirror=mirror;locked=slot;link=mirror.get(MirrorContent.LINK.get());addSlot(new Slot(input,0,80,28));addStandardInventorySlots(inv,8,84);}
 public boolean stillValid(Player p){if(p.level().isClientSide())return true;return p.isAlive()&&!p.isSpectator()&&mirror.getItem() instanceof HandMirrorItem&&inventory.getItem(locked)==mirror&&link!=null&&link.equals(mirror.get(MirrorContent.LINK.get()));}
 public void broadcastChanges(){var p=inventory.player;if(p instanceof ServerPlayer real&&stillValid(p)){var target=MirrorEntity.resolve(real.level(),link);var offered=input.getItem(0);if(target!=null&&!target.essence()&&!offered.isEmpty()){var rest=target.accept(offered,false);if(rest.getCount()!=offered.getCount())input.setItem(0,rest);}}super.broadcastChanges();}
 public void removed(Player p){super.removed(p);if(!p.level().isClientSide())clearContainer(p,input);}
 public void clicked(int slot,int button,ContainerInput type,Player p){if(!stillValid(p)||slot>=1&&slot<slots.size()&&slots.get(slot).getContainerSlot()==locked||type==ContainerInput.SWAP&&button==locked)return;super.clicked(slot,button,type,p);}
 public boolean canTakeItemForPickAll(ItemStack stack,Slot slot){return !(slot.container==inventory&&slot.getContainerSlot()==locked)&&super.canTakeItemForPickAll(stack,slot);}
 public ItemStack quickMoveStack(Player p,int index){if(index<0||index>=slots.size()||!stillValid(p))return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem()||index>0&&slot.getContainerSlot()==locked)return ItemStack.EMPTY;var stack=slot.getItem();var copy=stack.copy();if(index==0){if(!moveItemStackTo(stack,1,37,true))return ItemStack.EMPTY;}else if(!moveItemStackTo(stack,0,1,false))return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return copy;}
}
