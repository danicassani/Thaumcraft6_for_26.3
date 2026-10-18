package thaumcraft.research;

import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** Book actions use the native menu button packet; no client-provided points, flags or stages. */
public final class ResearchMenu extends AbstractContainerMenu {
    private static final int SIZE=ResearchCatalog.ENTRIES.size();
    private final Player owner;
    private final ContainerData data;
    private int selected=ResearchCatalog.ENTRIES.indexOf(ResearchCatalog.BY_KEY.get("FIRSTSTEPS"));
    public ResearchMenu(int id,Inventory inventory){
        super(ResearchContent.MENU.get(),id);owner=inventory.player;
        data=new SimpleContainerData(SIZE*2+18);data.set(SIZE,selected);addDataSlots(data);refresh();
    }
    private void refresh(){
        if(owner.level().isClientSide())return;
        var state=ResearchService.state(owner);data.set(SIZE+17,state==null?0:1);if(state==null){data.set(SIZE+1,0);return;}
        for(int i=0;i<SIZE;i++){
            var e=ResearchCatalog.ENTRIES.get(i);
            data.set(i,ResearchCatalog.knows(state,e.key())?e.stages().size()+1:state.stages().getOrDefault(e.key(),0));
            data.set(SIZE+18+i,!e.hidden()||state.stages().containsKey(e.key())||state.flags().contains(e.key())||!e.parents().isEmpty()&&ResearchService.parents(state,e)?1:0);
        }
        data.set(SIZE,selected);data.set(SIZE+1,ResearchService.canAdvance(owner,ResearchCatalog.ENTRIES.get(selected))?1:0);data.set(SIZE+16,state.scans().size());
        for(int i=0;i<14;i++)data.set(SIZE+2+i,Math.min(32766,state.raw(i<7?"OBSERVATION":"THEORY",ResearchCatalog.CATEGORIES.get(i%7))));
    }
    @Override public void broadcastChanges(){refresh();super.broadcastChanges();}
    public int stage(int index){return data.get(index);}
    public int selected(){return data.get(SIZE);}
    public boolean ready(){return data.get(SIZE+1)==1;}
    public int points(boolean theory,int category){return data.get(SIZE+2+(theory?7:0)+category);}
    public int scans(){return data.get(SIZE+16);}
    public boolean validData(){return data.get(SIZE+17)==1;}
    public boolean visible(int index){return data.get(SIZE+18+index)==1;}
    @Override public boolean stillValid(Player player){return player==owner&&ResearchService.hasBook(player);}
    @Override public ItemStack quickMoveStack(Player player,int index){return ItemStack.EMPTY;}
    @Override public boolean clickMenuButton(Player player,int button){
        if(!stillValid(player)||player.level().isClientSide())return false;
        if(button>=1000&&button<1000+SIZE){selected=button-1000;broadcastChanges();return true;}
        if(button==0){boolean changed=ResearchService.advance(player,ResearchCatalog.ENTRIES.get(selected).key());broadcastChanges();return changed;}
        return false;
    }
}
