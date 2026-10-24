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
        data=new SimpleContainerData(SIZE*2+89);data.set(SIZE,selected);addDataSlots(data);refresh();
    }
    private void refresh(){
        if(owner.level().isClientSide())return;
        var state=ResearchService.state(owner);data.set(SIZE+17,state==null?0:1);if(state==null){data.set(SIZE+1,0);return;}
        for(int i=0;i<SIZE;i++){
            var e=ResearchCatalog.ENTRIES.get(i);
            data.set(i,ResearchCatalog.knows(state,e.key())?e.stages().size()+1:state.stages().getOrDefault(e.key(),0));
            data.set(SIZE+18+i,state.stages().getOrDefault(e.key(),0)>0||state.flags().contains(e.key())||(!e.hidden()||!e.parents().isEmpty())&&ResearchService.parents(state,e)?1:0);
        }
        for(int c=0;c<7;c++)data.set(SIZE*2+18+c,ResearchCatalog.categoryUnlocked(state,ResearchCatalog.CATEGORIES.get(c))?1:0);
        if(!ResearchCatalog.categoryUnlocked(state,ResearchCatalog.ENTRIES.get(selected).category()))selected=ResearchCatalog.ENTRIES.indexOf(ResearchCatalog.BY_KEY.get("FIRSTSTEPS"));
        for(int i=0;i<64;i++)data.set(SIZE*2+25+i,0);
        var entry=ResearchCatalog.ENTRIES.get(selected);int current=state.stages().getOrDefault(entry.key(),0),goal=0;
        if(current>0&&current<=entry.stages().size()){
            var page=entry.stages().get(current-1);
            for(var r:page.crafts())data.set(SIZE*2+25+goal++,state.crafts().contains(r.proof())?1:0);
            for(var r:page.research())data.set(SIZE*2+25+goal++,ResearchCatalog.knows(state,r)?1:0);
            for(var r:page.items()){int count=0;for(int n=0;n<owner.getInventory().getContainerSize();n++)if(r.matches(owner.getInventory().getItem(n)))count+=owner.getInventory().getItem(n).getCount();data.set(SIZE*2+25+goal++,count>=r.count()?1:0);}
            for(var r:page.costs())data.set(SIZE*2+25+goal++,state.raw(r.kind(),r.category())>=r.amount()*r.progression()?1:0);
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
    public boolean requirementDone(int index){return index>=0&&index<64&&data.get(SIZE*2+25+index)==1;}
    public boolean categoryUnlocked(int index){return index==0||data.get(SIZE*2+18+index)==1;}
    public boolean visible(int index){return data.get(SIZE+18+index)==1;}
    @Override public boolean stillValid(Player player){return player==owner&&ResearchService.hasBook(player);}
    @Override public ItemStack quickMoveStack(Player player,int index){return ItemStack.EMPTY;}
    @Override public boolean clickMenuButton(Player player,int button){
        if(!stillValid(player)||player.level().isClientSide())return false;
        if(button>=1000&&button<1000+SIZE){int target=button-1000;if(!visible(target)||!categoryUnlocked(ResearchCatalog.CATEGORIES.indexOf(ResearchCatalog.ENTRIES.get(target).category())))return false;selected=target;var chosen=ResearchCatalog.ENTRIES.get(target);var state=ResearchService.state(owner);if(state!=null&&state.stages().getOrDefault(chosen.key(),0)==0&&ResearchService.canAdvance(owner,chosen))ResearchService.advance(owner,chosen.key());broadcastChanges();return true;}
        if(button==0){boolean changed=ResearchService.advance(player,ResearchCatalog.ENTRIES.get(selected).key());broadcastChanges();return changed;}
        return false;
    }
}
