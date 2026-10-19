package thaumcraft.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.crystals.CrystalContent;

public final class ArcaneMenu extends AbstractContainerMenu {
    private final ArcaneWorkbenchEntity table;
    private final Container inventory;
    private final SimpleContainer preview=new SimpleContainer(1);
    private final ContainerData data;
    public ArcaneMenu(int id,Inventory playerInv) { this(id,playerInv,null); }
    public ArcaneMenu(int id,Inventory playerInv,ArcaneWorkbenchEntity table) {
        super(ArcaneContent.MENU.get(),id);this.table=table;this.inventory=table==null?new SimpleContainer(16):table.inventory;
        data=table==null?new SimpleContainerData(11):new ContainerData() {
            public int get(int i) { var match=table.match();return switch(i) {case 0->Math.min(32766,table.availableVis());case 1->table.visCost(playerInv.player,match);case 2->table.canCraft(playerInv.player,match)?1:0;case 3->table.charged()?9:1;case 10->match!=null&&!thaumcraft.research.ResearchService.knows(playerInv.player,match.research())?1:0;default->match==null?0:match.crystals().getOrDefault(ArcaneRecipe.PRIMALS.get(i-4),0);}; }
            public void set(int i,int value) {} public int getCount() { return 11; }
        };
        for(int y=0;y<3;y++)for(int x=0;x<3;x++)addSlot(new Slot(inventory,x+y*3,16+x*20,28+y*20));
        for(int i=0;i<6;i++) { final String aspect=ArcaneRecipe.PRIMALS.get(i);addSlot(new Slot(inventory,9+i,16+i*24,104) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(CrystalContent.ESSENCE.get()) && CrystalContent.aspect(stack).equals(aspect); }
        }); }
        addSlot(new Slot(inventory,15,148,56) { @Override public boolean mayPlace(ItemStack stack) { return false; } });
        addSlot(new Slot(preview,0,104,56) { @Override public boolean mayPlace(ItemStack stack) {return false;} @Override public boolean mayPickup(Player player) {return false;} });
        addStandardInventorySlots(playerInv,8,146);addDataSlots(data);
    }
    public int available() {return data.get(0);} public int cost() {return data.get(1);} public boolean ready() {return data.get(2)==1;} public int chunks() {return data.get(3);}
    public int crystalCost(int primal) {return data.get(4+primal);}
    public boolean researchLocked(){return data.get(10)==1;}
    @Override public void broadcastChanges() {
        if(table!=null) {var match=table.match();var stack=match==null?ItemStack.EMPTY:match.result();if(!ItemStack.matches(preview.getItem(0),stack))preview.setItem(0,stack);}
        super.broadcastChanges();
    }
    @Override public boolean clickMenuButton(Player player,int button) { if(button!=0||table==null||!stillValid(player))return false;boolean crafted=table.craft(player);broadcastChanges();return crafted; }
    @Override public boolean stillValid(Player player) {return table==null || table.valid(player);}
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if(index==16)return ItemStack.EMPTY;
        var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var copy=stack.copy();
        if(index<16) {if(!moveItemStackTo(stack,17,53,true))return ItemStack.EMPTY;}
        else {boolean moved=false;if(stack.is(CrystalContent.ESSENCE.get())) {int i=ArcaneRecipe.PRIMALS.indexOf(CrystalContent.aspect(stack));if(i>=0)moved=moveItemStackTo(stack,9+i,10+i,false);}if(!moved&&!moveItemStackTo(stack,0,9,false))return ItemStack.EMPTY;}
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return copy;
    }
}
