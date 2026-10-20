package thaumcraft.golems;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
public final class SealMenu extends AbstractContainerMenu {
    private final SealEntity seal;private final ContainerData data;private final SimpleContainer preview=new SimpleContainer(9);private int selected;
    public SealMenu(int id,Inventory inv){this(id,inv,null);}
    public SealMenu(int id,Inventory inv,SealEntity seal){super(GolemContent.SEAL_MENU.get(),id);this.seal=seal;data=seal==null?new SimpleContainerData(30):new ContainerData(){public int get(int i){if(i>=6&&i<15)return seal.amounts[i-6];return switch(i){case 0->seal.channel;case 1->seal.priority;case 2->seal.radius;case 3->seal.redstone?1:0;case 4->seal.blacklist?1:0;case 5->seal.kind().ordinal();case 15->selected;case 16->seal.exact?1:0;case 17->seal.guardMode;case 18->seal.silk?1:0;case 19->seal.kind().filterSlots();case 20->seal.amounts[selected];case 21->seal.useLeft?1:0;case 22->seal.useAir?1:0;case 23->seal.useEmpty?1:0;case 24->seal.useSneak?1:0;case 25->seal.useProvision?1:0;case 26->seal.oriented()?1:0;case 27->seal.areaConfig().x();case 28->seal.areaConfig().y();case 29->seal.areaConfig().z();default->0;};}public void set(int i,int v){}public int getCount(){return 30;}};
        for(int i=0;i<9;i++)addSlot(new Slot(preview,i,8+i*18,108){public boolean mayPlace(ItemStack s){return false;}public boolean mayPickup(Player p){return false;}});addStandardInventorySlots(inv,8,144);addDataSlots(data);
    }
    public int value(int i){return data.get(i);}public SealKind kind(){return SealKind.values()[Math.clamp(value(5),0,SealKind.values().length-1)];}
    public boolean stillValid(Player p){return seal==null||seal.valid(p);}public boolean clickMenuButton(Player p,int b){return seal!=null&&stillValid(p)&&seal.edit(p,b,selected);}
    public void clicked(int slot,int button,ContainerInput type,Player p){if(!stillValid(p))return;if(slot>=0&&slot<9){if(seal!=null&&seal.kind().hasFilters()&&slot<seal.kind().filterSlots()){selected=slot;broadcastChanges();}return;}super.clicked(slot,button,type,p);}
    public void broadcastChanges(){if(seal!=null)for(int i=0;i<9;i++)preview.setItem(i,seal.sample(i).copy());super.broadcastChanges();}public ItemStack quickMoveStack(Player p,int i){return ItemStack.EMPTY;}
}
