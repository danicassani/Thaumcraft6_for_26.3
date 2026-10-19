package thaumcraft.thaumaturgy;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import thaumcraft.aura.AuraManager;
import thaumcraft.crystals.CrystalContent;
import thaumcraft.devices.AuraDeviceEntity;
import thaumcraft.research.ResearchService;
public final class FocalEntity extends AuraDeviceEntity implements MenuProvider {
    public final SimpleContainer inventory=new SimpleContainer(10){public void setChanged(){super.setChanged();FocalEntity.this.setChanged();}};
    public FocusProgram draft=new FocusProgram("touch",List.of(new FocusProgram.Effect("fire",1,1)));
    private FocusProgram pending;
    private int remaining,total;
    private UUID owner;
    public FocalEntity(BlockPos p,BlockState s){super(ThaumaturgyContent.TABLE_TYPE.get(),p,s);}
    public boolean working(){return pending!=null;}
    public int remaining(){return remaining;} public int total(){return total;}
    public int capacity(){return inventory.getItem(0).getItem() instanceof FocusItem f?f.capacity:0;}
    public boolean valid(Player p){return level!=null&&p.level()==level&&level.getBlockEntity(worldPosition)==this&&p.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(worldPosition))<=64;}
    public boolean knows(Player p,FocusProgram program){return ResearchService.knows(p,program.mediumResearch())&&program.effects().stream().allMatch(e->ResearchService.knows(p,e.research()));}
    public boolean start(Player p){
        if(!(level instanceof ServerLevel)||working()||!valid(p)||draft.complexity()>capacity()||capacity()==0||!knows(p,draft)||(!p.getAbilities().instabuild&&p.experienceLevel<draft.levels()))return false;
        var costs=new TreeMap<>(draft.crystals());
        for(int i=1;i<10;i++){var s=inventory.getItem(i);if(s.is(CrystalContent.ESSENCE.get()))costs.computeIfPresent(CrystalContent.aspect(s),(a,n)->n-s.getCount());}
        if(costs.values().stream().anyMatch(n->n>0))return false;
        costs=new TreeMap<>(draft.crystals());for(int i=1;i<10;i++){var s=inventory.getItem(i);if(!s.is(CrystalContent.ESSENCE.get()))continue;String a=CrystalContent.aspect(s);int n=Math.min(s.getCount(),Math.max(0,costs.getOrDefault(a,0)));inventory.removeItem(i,n);costs.computeIfPresent(a,(key,value)->value-n);}
        if(!p.getAbilities().instabuild)p.giveExperienceLevels(-draft.levels());
        pending=draft;remaining=total=draft.craftingVis(capacity());owner=p.getUUID();sync();return true;
    }
    @Override public void serverTick(){
        if(!(level instanceof ServerLevel l)||pending==null||l.getGameTime()%20!=0)return;
        // The focus is inaccessible during a job. A broken table drops the unfinished focus, never a free spell.
        if(!(inventory.getItem(0).getItem() instanceof FocusItem f)||pending.complexity()>f.capacity){pending=null;remaining=total=0;sync();return;}
        int radius=l.getBlockState(worldPosition.above()).is(thaumcraft.crafting.ArcaneContent.CHARGER.get())?1:0;
        int budget=Math.min(20,remaining);for(int x=-radius;x<=radius&&budget>0;x++)for(int z=-radius;z<=radius&&budget>0;z++){var pos=worldPosition.offset(x*16,0,z*16);int take=(int)AuraManager.drainVis(l,pos,budget,true);if(take>0){int paid=(int)AuraManager.drainVis(l,pos,take,false);remaining-=paid;budget-=paid;}}
        if(remaining<=0){inventory.getItem(0).set(ThaumaturgyContent.PROGRAM.get(),pending);var p=owner==null?null:l.getServer().getPlayerList().getPlayer(owner);if(p!=null)ResearchService.recordCraft(p,inventory.getItem(0));pending=null;remaining=0;}
        sync();
    }
    public boolean edit(Player p,int button){if(!valid(p)||working())return false;var effects=new ArrayList<>(draft.effects());String medium=draft.medium();var last=effects.getLast();switch(button){case 0->{return start(p);}case 1->medium=FocusProgram.MEDIA.get((FocusProgram.MEDIA.indexOf(medium)+1)%FocusProgram.MEDIA.size());case 2->effects.set(effects.size()-1,new FocusProgram.Effect(FocusProgram.EFFECTS.get((FocusProgram.EFFECTS.indexOf(last.kind())+1)%FocusProgram.EFFECTS.size()),last.power(),last.duration()));case 3->effects.set(effects.size()-1,new FocusProgram.Effect(last.kind(),last.power()%5+1,last.duration()));case 4->effects.set(effects.size()-1,new FocusProgram.Effect(last.kind(),last.power(),last.duration()%5+1));case 5->{if(effects.size()<8)effects.add(new FocusProgram.Effect("fire",1,1));}case 6->{if(effects.size()>1)effects.removeLast();}default->{return false;}}draft=new FocusProgram(medium,effects);sync();return true;}
    public Component getDisplayName(){return Component.translatable("block.thaumcraft.wand_workbench");}
    public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new FocalMenu(id,inv,this);}
    @Override public void preRemoveSideEffects(BlockPos pos,BlockState state){super.preRemoveSideEffects(pos,state);if(level instanceof ServerLevel)for(int i=0;i<10;i++)Block.popResource(level,pos,inventory.removeItemNoUpdate(i));}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);for(int i=0;i<10;i++)if(!inventory.getItem(i).isEmpty())out.store("slot"+i,ItemStack.CODEC,inventory.getItem(i));out.store("draft",FocusProgram.CODEC,draft);if(pending!=null)out.store("pending",FocusProgram.CODEC,pending);out.putInt("remaining",remaining);out.putInt("total",total);if(owner!=null)out.putString("owner",owner.toString());}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);for(int i=0;i<10;i++)inventory.setItem(i,in.read("slot"+i,ItemStack.CODEC).orElse(ItemStack.EMPTY));draft=in.read("draft",FocusProgram.CODEC).orElse(draft);pending=in.read("pending",FocusProgram.CODEC).orElse(null);remaining=Math.clamp(in.getIntOr("remaining",0),0,100000);total=Math.clamp(in.getIntOr("total",remaining),remaining,100000);try{owner=UUID.fromString(in.getStringOr("owner",""));}catch(IllegalArgumentException e){owner=null;}}
}
