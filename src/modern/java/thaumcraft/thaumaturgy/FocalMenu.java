package thaumcraft.thaumaturgy;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.crystals.CrystalContent;
public final class FocalMenu extends AbstractContainerMenu {
    private int selectedBranch;
    private int selected(){return selectedBranch==1&&table!=null&&table.draft.split()?1:0;}
    private final SimpleContainer preview=new SimpleContainer(1);
    private final FocalEntity table;private final Container inventory;private final ContainerData data;
    public FocalMenu(int id,Inventory inv){this(id,inv,null);}
    public FocalMenu(int id,Inventory inv,FocalEntity f){super(ThaumaturgyContent.MENU.get(),id);table=f;inventory=f==null?new SimpleContainer(10):f.inventory;data=f==null?new SimpleContainerData(17):new ContainerData(){public int get(int i){var d=f.draft;var path=selected()==0?d.root():d.branches().getFirst();var e=path.effects().getLast();return switch(i){case 0->f.working()?1:0;case 1->f.remaining();case 2->f.total();case 3->FocusProgram.MEDIA.indexOf(path.medium());case 4->FocusProgram.EFFECTS.indexOf(e.kind());case 5->e.power();case 6->e.duration();case 7->d.complexity();case 8->d.totalEffects();case 9->f.capacity();case 10->selected();case 11->FocusProgram.MODIFIERS.indexOf(d.modifier());case 12->d.forks();case 13->d.cone();case 14->path.radius();case 15->path.seconds();case 16->path.friendly()?1:0;default->0;};}public void set(int i,int v){}public int getCount(){return 17;}};
        addSlot(new Slot(inventory,0,16,25){public boolean mayPlace(ItemStack s){return !working()&&s.getItem() instanceof FocusItem;}public boolean mayPickup(Player p){return !working();}});
        for(int i=1;i<10;i++)addSlot(new Slot(inventory,i,8+(i-1)*24,112){public boolean mayPlace(ItemStack s){return !working()&&s.is(CrystalContent.ESSENCE.get());}public boolean mayPickup(Player p){return !working();}});
        addSlot(new Slot(preview,0,16,66){public boolean mayPlace(ItemStack s){return false;}public boolean mayPickup(Player p){return false;}});
        addStandardInventorySlots(inv,8,146);addDataSlots(data);
    }
    public int value(int i){return data.get(i);}public boolean working(){return value(0)==1;}
    public boolean clickMenuButton(Player p,int b){if(table==null||!stillValid(p)||table.working())return false;if(b==8){if(!table.draft.split())return false;selectedBranch=1-selected();broadcastChanges();return true;}boolean result=table.edit(p,b,selected());broadcastChanges();return result;}
    public boolean stillValid(Player p){return table==null||table.valid(p);}
    public void broadcastChanges(){if(table!=null){var stack=new ItemStack(net.minecraft.world.item.Items.PAPER);stack.set(ThaumaturgyContent.PROGRAM.get(),table.draft);preview.setItem(0,stack);}super.broadcastChanges();}
    public ItemStack quickMoveStack(Player p,int i){if(i==10)return ItemStack.EMPTY;var slot=slots.get(i);if(!slot.hasItem()||i<10&&working())return ItemStack.EMPTY;var s=slot.getItem();var copy=s.copy();if(i<10){if(!moveItemStackTo(s,11,47,true))return ItemStack.EMPTY;}else if(s.getItem() instanceof FocusItem){if(working()||!moveItemStackTo(s,0,1,false))return ItemStack.EMPTY;}else if(s.is(CrystalContent.ESSENCE.get())){if(working()||!moveItemStackTo(s,1,10,false))return ItemStack.EMPTY;}else return ItemStack.EMPTY;if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return copy;}
}
