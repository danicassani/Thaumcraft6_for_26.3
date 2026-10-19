package thaumcraft.golems;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
public final class SealMenu extends AbstractContainerMenu {
    private final SealEntity seal;private final ContainerData data;private final SimpleContainer preview=new SimpleContainer(1);
    public SealMenu(int id,Inventory inv){this(id,inv,null);}
    public SealMenu(int id,Inventory inv,SealEntity seal){super(GolemContent.SEAL_MENU.get(),id);this.seal=seal;data=seal==null?new SimpleContainerData(6):new ContainerData(){public int get(int i){return switch(i){case 0->seal.channel;case 1->seal.priority;case 2->seal.radius;case 3->seal.redstone?1:0;case 4->seal.blacklist?1:0;case 5->seal.kind().ordinal();default->0;};}public void set(int i,int v){}public int getCount(){return 6;}};addSlot(new Slot(preview,0,144,108){public boolean mayPlace(ItemStack s){return false;}public boolean mayPickup(Player p){return false;}});addStandardInventorySlots(inv,8,144);addDataSlots(data);}
    public int value(int i){return data.get(i);}public boolean stillValid(Player p){return seal==null||seal.valid(p);}public boolean clickMenuButton(Player p,int b){return seal!=null&&stillValid(p)&&seal.edit(p,b);}
    public void broadcastChanges(){if(seal!=null)preview.setItem(0,seal.filter.copy());super.broadcastChanges();}public ItemStack quickMoveStack(Player p,int i){return ItemStack.EMPTY;}
}
