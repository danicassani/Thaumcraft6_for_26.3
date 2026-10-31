package thaumcraft.theory;

import java.util.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import thaumcraft.research.*;

public final class TheoryMenu extends AbstractContainerMenu {
    private final ResearchTableEntity table;
    private final Player owner;
    private final Container inventory;
    private final ContainerData data=new SimpleContainerData(45);
    private int selectedAids;
    public TheoryMenu(int id,Inventory inv){this(id,inv,null);}
    public TheoryMenu(int id,Inventory inv,ResearchTableEntity table){
        super(TheoryContent.MENU.get(),id);this.table=table;owner=inv.player;inventory=table==null?new SimpleContainer(2):table.inventory;
        addSlot(new Slot(inventory,0,16,15){@Override public boolean mayPlace(ItemStack s){return s.is(TheoryContent.SCRIBE.get());}@Override public int getMaxStackSize(){return 1;}});
        addSlot(new Slot(inventory,1,224,16){@Override public boolean mayPlace(ItemStack s){return s.is(Items.PAPER);}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,77+col*18,190+row*18));
        for(int i=0;i<9;i++)addSlot(new Slot(inv,i,20+(i%3)*18,190+(i/3)*18));addDataSlots(data);refresh();
    }
    public int value(int i){return data.get(i);}
    private void refresh(){
        if(table==null)return;var s=table.session();int mask=0;var aids=table.nearbyAids();for(int i=0;i<TheoryCards.AID_KEYS.size();i++)if(aids.contains(TheoryCards.AID_KEYS.get(i)))mask|=1<<i;selectedAids&=mask;
        data.set(0,table.revision());data.set(1,!table.supported()?4:s==null?1:s.inspiration<=0?3:2);data.set(2,table.owned(owner)?1:0);
        data.set(3,s==null?ResearchTableEntity.inspiration(owner):s.inspiration);data.set(4,s==null?ResearchTableEntity.inspiration(owner):s.maximum);data.set(5,s==null?0:s.bonus);data.set(6,s==null?0:s.penalty);data.set(7,s==null?0:s.played);data.set(8,mask);data.set(9,selectedAids);
        data.set(10,s==null&&table.supported()&&ResearchService.knows(owner,"THEORYRESEARCH")&&ResearchService.state(owner)!=null&&TheoryContent.ink(inventory.getItem(0),1)&&inventory.getItem(1).is(Items.PAPER)&&Integer.bitCount(selectedAids)<ResearchTableEntity.inspiration(owner)?1:0);
        data.set(11,ResearchService.knows(owner,"THEORYRESEARCH")?1:0);data.set(12,s!=null&&s.inspiration>0&&s.choices.isEmpty()&&inventory.getItem(1).is(Items.PAPER)&&table.owned(owner)?1:0);
        int blocked=0;if(s!=null)for(int i=0;i<7;i++)if(s.blocked.contains(ResearchCatalog.CATEGORIES.get(i)))blocked|=1<<i;data.set(13,blocked);
        for(int i=0;i<7;i++)data.set(14+i,s==null?0:Math.min(32766,s.total(ResearchCatalog.CATEGORIES.get(i))));
        for(int i=0;i<3;i++){int base=21+i*8;var c=s==null||i>=s.choices.size()?null:s.choices.get(i);data.set(base,c==null?0:c.kind().ordinal()+1);if(c!=null){data.set(base+1,c.category());data.set(base+2,c.a());data.set(base+3,c.b());data.set(base+4,Math.min(32766,c.amount()));data.set(base+5,c.cost()+2);data.set(base+6,table.ready(owner,i)?1:0);data.set(base+7,c.aid()?1:0);}}
    }
    public TheoryCards.Card card(int index){int b=21+index*8,k=value(b);return k<=0?null:new TheoryCards.Card(TheoryCards.Kind.values()[k-1],value(b+1),value(b+2),value(b+3),value(b+4),0,value(b+7)==1);}
    public int packet(int action){return value(0)*32+action;}
    @Override public void broadcastChanges(){refresh();super.broadcastChanges();}
    @Override public boolean stillValid(Player p){return p==owner&&(table==null||table.valid(p));}
    @Override public boolean clickMenuButton(Player p,int button){
        if(table==null||!stillValid(p)||button<0||button/32!=table.revision())return false;int action=button%32;boolean changed;
        if(action>=10){if(table.session()!=null||action-10>=TheoryCards.AID_KEYS.size())return false;selectedAids^=1<<(action-10);refresh();return true;}
        changed=switch(action){case 1->{var selected=new ArrayList<String>();for(int i=0;i<TheoryCards.AID_KEYS.size();i++)if((selectedAids&(1<<i))!=0)selected.add(TheoryCards.AID_KEYS.get(i));yield table.start(p,selected);}case 2,3->table.draw(p,action==3);case 4,5,6->table.play(p,action-4);case 7->table.finish(p);case 8->table.cancel(p);default->false;};
        broadcastChanges();return changed;
    }
    @Override public ItemStack quickMoveStack(Player p,int index){
        if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var copy=s.copy();
        if(index<2){if(!moveItemStackTo(s,2,38,true))return ItemStack.EMPTY;}else if(s.is(TheoryContent.SCRIBE.get())){if(!moveItemStackTo(s,0,1,false))return ItemStack.EMPTY;}else if(s.is(Items.PAPER)){if(!moveItemStackTo(s,1,2,false))return ItemStack.EMPTY;}else return ItemStack.EMPTY;
        if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return copy;
    }
}
