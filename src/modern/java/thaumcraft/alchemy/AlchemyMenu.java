package thaumcraft.alchemy;
import java.util.List;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import thaumcraft.crystals.CrystalAspects;
public final class AlchemyMenu extends AbstractContainerMenu {
    public static final List<String> ASPECTS=List.copyOf(CrystalAspects.COLORS.keySet());
    private final AlchemyMachine machine;
    private final Container inventory;
    private final ContainerData data=new SimpleContainerData(45);
    public AlchemyMenu(int id,Inventory inv){this(id,inv,null);}
    public AlchemyMenu(int id,Inventory inv,AlchemyMachine machine){
        super(AlchemyContent.MENU.get(),id);this.machine=machine;inventory=machine==null?new SimpleContainer(3):machine.inventory;
        for(int i=0;i<3;i++){final int slot=i;addSlot(new Slot(inventory,i,216+i*24,24){@Override public boolean isActive(){return value(0)==AlchemyBlock.Kind.SMELTER.ordinal();}@Override public boolean mayPlace(ItemStack stack){return slot!=2&&(machine==null||slot==0||machine.fuelTime(stack)>0);}});}
        addStandardInventorySlots(inv,79,146);addDataSlots(data);refresh();
    }
    private void refresh(){if(machine==null)return;data.set(0,machine.kind().ordinal());data.set(1,machine.supported()?1:0);data.set(2,machine.water.getFluidAmount());data.set(3,machine.heat());data.set(4,Math.min(32766,machine.burn()));data.set(5,Math.min(32766,machine.burnTotal()));data.set(6,machine.cook());data.set(7,machine.needed());for(int i=0;i<ASPECTS.size();i++)data.set(8+i,machine.aspects().getOrDefault(ASPECTS.get(i),0));}
    public int value(int i){return data.get(i);}
    @Override public void broadcastChanges(){refresh();super.broadcastChanges();}
    @Override public boolean stillValid(Player p){return machine==null||machine.valid(p);}
    @Override public ItemStack quickMoveStack(Player p,int index){if(index<0||index>=slots.size()||value(0)!=AlchemyBlock.Kind.SMELTER.ordinal())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var copy=s.copy();if(index<3){if(!moveItemStackTo(s,3,39,true))return ItemStack.EMPTY;}else if(machine!=null&&machine.fuelTime(s)>0){if(!moveItemStackTo(s,1,2,false))return ItemStack.EMPTY;}else if(!moveItemStackTo(s,0,1,false))return ItemStack.EMPTY;if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return copy;}
}
