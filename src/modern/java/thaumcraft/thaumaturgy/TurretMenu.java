package thaumcraft.thaumaturgy;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
public final class TurretMenu extends AbstractContainerMenu {
 private final ContainerData data;private final CrossbowTurret turret;
 public TurretMenu(int id,Inventory inv){this(id,inv,false);}
 public TurretMenu(int id,Inventory inv,boolean advanced){this(id,inv,new SimpleContainer(1),null,advanced);}
 public TurretMenu(int id,Inventory inv,Container ammo,CrossbowTurret turret){this(id,inv,ammo,turret,turret.advanced());}
 private TurretMenu(int id,Inventory inv,Container ammo,CrossbowTurret turret,boolean advanced){super((advanced?TurretContent.ADVANCED_MENU:TurretContent.MENU).get(),id);this.turret=turret;data=turret==null?new SimpleContainerData(3):new ContainerData(){public int get(int n){return switch(n){case 0->turret.advanced()?1:0;case 1->turret.targets();default->Math.round(100*turret.getHealth()/turret.getMaxHealth());};}public void set(int n,int v){}public int getCount(){return 3;}};addDataSlots(data);addSlot(new Slot(ammo,0,advanced?42:80,29){public boolean mayPlace(ItemStack s){return CrossbowTurret.validAmmo(s);}});addStandardInventorySlots(inv,8,84);}
 public int value(int n){return data.get(n);}
 public boolean stillValid(Player p){return turret==null||turret.isAlive()&&turret.owned(p)&&p.distanceToSqr(turret)<64;}
 public boolean clickMenuButton(Player p,int button){if(!stillValid(p)||turret==null||!turret.advanced()||button<0||button>2)return false;turret.targets(turret.targets()^(1<<button));broadcastChanges();return true;}
 public ItemStack quickMoveStack(Player p,int i){if(!stillValid(p)||i<0||i>=slots.size())return ItemStack.EMPTY;var slot=slots.get(i);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var old=s.copy();if(i==0?!moveItemStackTo(s,1,37,true):!CrossbowTurret.validAmmo(s)||!moveItemStackTo(s,0,1,false))return ItemStack.EMPTY;if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return old;}
}
