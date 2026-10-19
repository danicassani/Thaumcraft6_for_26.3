package thaumcraft.thaumaturgy;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.crystals.CrystalContent;
public final class FocalMenu extends AbstractContainerMenu {
    private int selectedBranch;
    private final java.util.List<Integer> address=new java.util.ArrayList<>();
    private FocusProgram node(){var n=table.draft.at(address);if(n==null){address.clear();selectedBranch=0;return table.draft;}return n;}
    private int selected(){return selectedBranch==1&&table!=null&&node().split()?1:0;}
    private final SimpleContainer preview=new SimpleContainer(1);
    private final FocalEntity table;private final Container inventory;private final ContainerData data;
    public FocalMenu(int id,Inventory inv){this(id,inv,null);}
    public FocalMenu(int id,Inventory inv,FocalEntity f){super(ThaumaturgyContent.MENU.get(),id);table=f;inventory=f==null?new SimpleContainer(10):f.inventory;data=f==null?new SimpleContainerData(27):new ContainerData(){public int get(int i){var d=node();var path=selected()==0?d.root():d.branches().getFirst();var e=path.effects().getLast();return switch(i){case 0->f.working()?1:0;case 1->f.remaining();case 2->f.total();case 3->FocusProgram.MEDIA.indexOf(path.medium());case 4->FocusProgram.EFFECTS.indexOf(e.kind());case 5->e.kind().equals("rift")?e.depth():e.power();case 6->e.duration();case 7->f.draft.complexity();case 8->f.draft.totalEffects();case 9->f.capacity();case 10->selected();case 11->FocusProgram.MODIFIERS.indexOf(d.modifier());case 12->d.forks();case 13->d.cone();case 14->path.radius();case 15->path.seconds();case 16->path.friendly()?1:0;case 17->d.plan().x();case 18->d.plan().y();case 19->d.plan().z();case 20->d.plan().surface()?1:0;case 21->path.projectile().speed();case 22->path.projectile().option();case 23->e.fortune();case 24->e.silk()?1:0;case 25->address.size();case 26->d.child(selected())==null?0:1;default->0;};}public void set(int i,int v){}public int getCount(){return 27;}};
        addSlot(new Slot(inventory,0,16,25){public boolean mayPlace(ItemStack s){return !working()&&s.getItem() instanceof FocusItem;}public boolean mayPickup(Player p){return !working();}});
        for(int i=1;i<10;i++)addSlot(new Slot(inventory,i,8+(i-1)*24,112){public boolean mayPlace(ItemStack s){return !working()&&s.is(CrystalContent.ESSENCE.get());}public boolean mayPickup(Player p){return !working();}});
        addSlot(new Slot(preview,0,16,66){public boolean mayPlace(ItemStack s){return false;}public boolean mayPickup(Player p){return false;}});
        addStandardInventorySlots(inv,8,146);addDataSlots(data);
    }
    public int value(int i){return data.get(i);}public boolean working(){return value(0)==1;}
    public boolean clickMenuButton(Player p,int b){
        if(table==null||!stillValid(p)||table.working())return false;
        if(b==8){if(!node().split())return false;selectedBranch=1-selected();}
        else if(b==23){if(address.isEmpty())return false;address.removeLast();selectedBranch=0;}
        else if(b==22){int branch=selected();if(node().child(branch)==null&&!table.edit(p,22,branch,address))return false;address.add(branch);selectedBranch=0;}
        else if(!table.edit(p,b,selected(),address))return false;
        broadcastChanges();return true;
    }
    public boolean stillValid(Player p){return table==null||table.valid(p);}
    public void broadcastChanges(){if(table!=null){var stack=new ItemStack(net.minecraft.world.item.Items.PAPER);stack.set(ThaumaturgyContent.PROGRAM.get(),table.draft);preview.setItem(0,stack);}super.broadcastChanges();}
    public ItemStack quickMoveStack(Player p,int i){if(i==10)return ItemStack.EMPTY;var slot=slots.get(i);if(!slot.hasItem()||i<10&&working())return ItemStack.EMPTY;var s=slot.getItem();var copy=s.copy();if(i<10){if(!moveItemStackTo(s,11,47,true))return ItemStack.EMPTY;}else if(s.getItem() instanceof FocusItem){if(working()||!moveItemStackTo(s,0,1,false))return ItemStack.EMPTY;}else if(s.is(CrystalContent.ESSENCE.get())){if(working()||!moveItemStackTo(s,1,10,false))return ItemStack.EMPTY;}else return ItemStack.EMPTY;if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return copy;}
}
