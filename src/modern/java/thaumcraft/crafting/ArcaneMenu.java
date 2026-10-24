package thaumcraft.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.crystals.CrystalContent;

/** A single native result slot. Only the server can turn its preview into a paid stack. */
public final class ArcaneMenu extends AbstractContainerMenu {
    public static final int RESULT=15,PLAYER_START=16,PLAYER_END=52;
    private final ArcaneWorkbenchEntity table;
    private final Container inventory;
    private final SimpleContainer preview=new SimpleContainer(1);
    private final ContainerData data;
    public ArcaneMenu(int id,Inventory playerInv){this(id,playerInv,null);}
    public ArcaneMenu(int id,Inventory playerInv,ArcaneWorkbenchEntity table){
        super(ArcaneContent.MENU.get(),id);this.table=table;inventory=table==null?new SimpleContainer(16):table.inventory;
        data=table==null?new SimpleContainerData(12):new ContainerData(){
            public int get(int i){var match=table.match();return switch(i){case 0->Math.min(32766,table.availableVis());case 1->table.visCost(playerInv.player,match);case 2->!inventory.getItem(15).isEmpty()||table.canCraft(playerInv.player,match)?1:0;case 3->table.charged()?9:1;case 10->match!=null&&!thaumcraft.research.ResearchService.knows(playerInv.player,match.research())?1:0;case 11->table.reason(playerInv.player,match);default->match==null?0:match.crystals().getOrDefault(ArcaneRecipe.PRIMALS.get(i-4),0);};}
            public void set(int i,int v){}public int getCount(){return 12;}
        };
        for(int y=0;y<3;y++)for(int x=0;x<3;x++)addSlot(new Slot(inventory,x+y*3,16+x*20,28+y*20));
        for(int i=0;i<6;i++){final String aspect=ArcaneRecipe.PRIMALS.get(i);addSlot(new Slot(inventory,9+i,16+i*24,104){public boolean mayPlace(ItemStack s){return s.is(CrystalContent.ESSENCE.get())&&CrystalContent.aspect(s).equals(aspect);}});}
        addSlot(new Slot(preview,0,132,48){
            public boolean mayPlace(ItemStack s){return false;}
            public boolean mayPickup(Player p){return table==null?ready():stillValid(p)&&(!inventory.getItem(15).isEmpty()||table.canCraft(p,table.match()));}
            public ItemStack remove(int amount){
                if(table==null)return super.remove(amount);
                if(amount<=0||!mayPickup(playerInv.player))return ItemStack.EMPTY;
                if(inventory.getItem(15).isEmpty()&&!table.craft(playerInv.player))return ItemStack.EMPTY;
                var result=inventory.removeItem(15,amount);refreshOutput();return result;
            }
            public void onTake(Player p,ItemStack s){super.onTake(p,s);refreshOutput();}
        });
        addStandardInventorySlots(playerInv,8,146);addDataSlots(data);refreshOutput();
    }
    private void refreshOutput(){if(table==null)return;var stored=inventory.getItem(15);var match=table.match();var result=!stored.isEmpty()?stored.copy():match==null?ItemStack.EMPTY:match.result().copy();if(!ItemStack.matches(preview.getItem(0),result))preview.setItem(0,result);}
    public int available(){return data.get(0);}public int cost(){return data.get(1);}public boolean ready(){return data.get(2)==1;}public int chunks(){return data.get(3);}public int crystalCost(int p){return data.get(4+p);}public boolean researchLocked(){return data.get(10)==1;}public int reason(){return data.get(11);}
    public void broadcastChanges(){refreshOutput();super.broadcastChanges();}
    public boolean clickMenuButton(Player p,int button){if(button!=0||table==null||!stillValid(p))return false;boolean crafted=table.craft(p);broadcastChanges();return crafted;}
    public boolean stillValid(Player p){return table==null||table.valid(p);}
    public boolean canTakeItemForPickAll(ItemStack s,Slot slot){return slot.container!=preview&&super.canTakeItemForPickAll(s,slot);}
    public ItemStack quickMoveStack(Player p,int index){
        if(index<0||index>=slots.size()||!stillValid(p))return ItemStack.EMPTY;
        var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        if(index==RESULT){
            if(!slot.mayPickup(p))return ItemStack.EMPTY;var shown=slot.getItem().copy();int room=0;
            for(int i=PLAYER_START;i<PLAYER_END;i++){var target=slots.get(i);var current=target.getItem();if(target.mayPlace(shown)&&(current.isEmpty()||ItemStack.isSameItemSameComponents(current,shown)))room+=Math.max(0,Math.min(target.getMaxStackSize(shown),shown.getMaxStackSize())-current.getCount());}
            if(room<=0)return ItemStack.EMPTY;
            var taken=slot.remove(Math.min(room,shown.getCount()));if(taken.isEmpty())return ItemStack.EMPTY;var copy=taken.copy();
            if(!moveItemStackTo(taken,PLAYER_START,PLAYER_END,true))throw new IllegalStateException("Result capacity changed during server craft");
            if(!taken.isEmpty())throw new IllegalStateException("Incomplete paid result transfer");slot.onTake(p,copy);broadcastChanges();return copy;
        }
        var stack=slot.getItem();var copy=stack.copy();
        if(index<RESULT){if(!moveItemStackTo(stack,PLAYER_START,PLAYER_END,true))return ItemStack.EMPTY;}
        else{boolean moved=false;if(stack.is(CrystalContent.ESSENCE.get())){int i=ArcaneRecipe.PRIMALS.indexOf(CrystalContent.aspect(stack));if(i>=0)moved=moveItemStackTo(stack,9+i,10+i,false);}if(!moved&&!moveItemStackTo(stack,0,9,false))return ItemStack.EMPTY;}
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);refreshOutput();return copy;
    }
}
