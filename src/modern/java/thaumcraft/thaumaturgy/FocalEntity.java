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
    public boolean knows(Player p,FocusProgram program){return program.knows(p);}
    public boolean start(Player p){
        if(!(level instanceof ServerLevel)||working()||!valid(p)||draft.complexity()>capacity()||capacity()==0||!knows(p,draft)||(!p.getAbilities().instabuild&&p.experienceLevel<draft.levels()))return false;
        var costs=new TreeMap<>(draft.crystals());
        record Payment(Container inventory,int slot,int amount){}
        var payments=new ArrayList<Payment>();
        // Reserve exact costs from the table first, then the player's main inventory. Nothing is paid on failure.
        for(Container source:List.of(inventory,p.getInventory()))for(int i=source==inventory?1:0;i<(source==inventory?10:36);i++){
            var stack=source.getItem(i);if(!stack.is(CrystalContent.ESSENCE.get()))continue;String aspect=CrystalContent.aspect(stack);int take=Math.min(stack.getCount(),Math.max(0,costs.getOrDefault(aspect,0)));if(take>0){payments.add(new Payment(source,i,take));costs.put(aspect,costs.get(aspect)-take);}
        }
        if(costs.values().stream().anyMatch(n->n>0))return false;
        for(var payment:payments)payment.inventory.removeItem(payment.slot,payment.amount);
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
    public boolean edit(Player p,int button){return edit(p,button,0);}
    public boolean edit(Player p,int button,int selected){return edit(p,button,selected,List.of());}
    public boolean edit(Player p,int button,int selected,List<Integer> address){
        if(!valid(p)||working())return false;if(button==0)return start(p);
        var node=draft.at(address);if(node==null||selected<0||selected>node.branches().size())return false;
        var edited=editProgram(node,button,selected);if(edited==null)return false;
        var graph=draft.replaceAt(address,edited);if(!graph.valid())return false;draft=graph;sync();return true;
    }
    private FocusProgram editProgram(FocusProgram draft,int button,int selected){
        FocusProgram next=draft;var path=selected==0?draft.root():draft.branches().getFirst();var effects=new ArrayList<>(path.effects());var last=effects.getLast();String medium=path.medium();int radius=path.radius(),seconds=path.seconds();boolean friendly=path.friendly();var projectile=path.projectile();
        switch(button){
            case 1->{if(selected==1&&draft.modifier().equals("split_target"))return null;do{medium=FocusProgram.MEDIA.get((FocusProgram.MEDIA.indexOf(medium)+1)%FocusProgram.MEDIA.size());}while(medium.equals("plan")&&(selected==1||draft.modifier().equals("split_trajectory")||draft.modifier().equals("scatter")));if(medium.equals("projectile")&&projectile.speed()==0)projectile=new FocusProgram.ProjectileSettings(1,projectile.option());}
            case 2->effects.set(effects.size()-1,new FocusProgram.Effect(FocusProgram.EFFECTS.get((FocusProgram.EFFECTS.indexOf(last.kind())+1)%FocusProgram.EFFECTS.size()),last.power(),FocusProgram.EFFECTS.get((FocusProgram.EFFECTS.indexOf(last.kind())+1)%FocusProgram.EFFECTS.size()).equals("rift")?Math.max(2,last.duration()):Math.min(5,last.duration()),last.depth()));
            case 3->effects.set(effects.size()-1,new FocusProgram.Effect(last.kind(),last.kind().equals("rift")?last.power():last.power()%5+1,last.duration(),last.kind().equals("rift")?(last.depth()==32?8:last.depth()+8):last.depth(),last.fortune(),last.silk()));
            case 4->effects.set(effects.size()-1,new FocusProgram.Effect(last.kind(),last.power(),last.kind().equals("rift")?(last.duration()==10?2:last.duration()+1):last.duration()%5+1,last.depth(),last.fortune(),last.silk()));
            case 5->{if(draft.totalEffects()>=8)return null;effects.add(new FocusProgram.Effect("fire",1,1));}
            case 6->{if(effects.size()<=1)return null;effects.removeLast();}
            case 7->{int index=FocusProgram.MODIFIERS.indexOf(draft.modifier());do{next=draft.modifier(FocusProgram.MODIFIERS.get(++index%FocusProgram.MODIFIERS.size()));}while(!next.valid()&&index<FocusProgram.MODIFIERS.size()*2);}
            case 9->next=new FocusProgram(draft.medium(),draft.effects(),draft.modifier(),draft.forks()==10?2:draft.forks()+1,draft.cone(),draft.radius(),draft.seconds(),draft.friendly(),draft.branches(),draft.plan(),draft.projectile(),draft.next());
            case 10->next=new FocusProgram(draft.medium(),draft.effects(),draft.modifier(),draft.forks(),FocusProgram.CONES.get((FocusProgram.CONES.indexOf(draft.cone())+1)%FocusProgram.CONES.size()),draft.radius(),draft.seconds(),draft.friendly(),draft.branches(),draft.plan(),draft.projectile(),draft.next());
            case 11->radius=radius%3+1;
            case 12->seconds=seconds>=30?5:seconds+5;
            case 13->friendly=!friendly;
            case 14,15,16,17->{if(selected!=0||!draft.medium().equals("plan"))return null;var a=draft.plan();var plan=new FocusProgram.PlanSettings(button==14?(a.x()+1)%4:a.x(),button==15?(a.y()+1)%4:a.y(),button==16?(a.z()+1)%4:a.z(),button==17?!a.surface():a.surface());next=new FocusProgram(draft.medium(),draft.effects(),draft.modifier(),draft.forks(),draft.cone(),draft.radius(),draft.seconds(),draft.friendly(),draft.branches(),plan,draft.projectile(),draft.next());}
            case 18,19->{if(!medium.equals("projectile"))return null;projectile=new FocusProgram.ProjectileSettings(button==18?projectile.speed()%5+1:Math.max(1,projectile.speed()),button==19?(projectile.option()+1)%4:projectile.option());}
            case 20,21->{if(!last.harvest())return null;effects.set(effects.size()-1,new FocusProgram.Effect(last.kind(),last.power(),last.duration(),last.depth(),button==20?(last.fortune()+1)%5:last.fortune(),button==21?!last.silk():last.silk()));}
            case 22->next=draft.withChild(selected,new FocusProgram("touch",List.of(new FocusProgram.Effect("fire",1,1))));
            case 24->next=draft.withChild(selected,null);
            default->{return null;}
        }
        if(button<=6||button>=11&&button<=13||button>=18&&button<=21)next=draft.replace(selected,new FocusProgram.Branch(medium,effects,radius,seconds,friendly,projectile,path.next()));
        return next.valid()?next:null;
    }
    public Component getDisplayName(){return Component.translatable("block.thaumcraft.wand_workbench");}
    public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new FocalMenu(id,inv,this);}
    @Override public void preRemoveSideEffects(BlockPos pos,BlockState state){super.preRemoveSideEffects(pos,state);if(level instanceof ServerLevel)for(int i=0;i<10;i++)Block.popResource(level,pos,inventory.removeItemNoUpdate(i));}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);for(int i=0;i<10;i++)if(!inventory.getItem(i).isEmpty())out.store("slot"+i,ItemStack.CODEC,inventory.getItem(i));out.store("draft",FocusProgram.CODEC,draft);if(pending!=null)out.store("pending",FocusProgram.CODEC,pending);out.putInt("remaining",remaining);out.putInt("total",total);if(owner!=null)out.putString("owner",owner.toString());}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);for(int i=0;i<10;i++)inventory.setItem(i,in.read("slot"+i,ItemStack.CODEC).orElse(ItemStack.EMPTY));draft=in.read("draft",FocusProgram.CODEC).orElse(draft);pending=in.read("pending",FocusProgram.CODEC).orElse(null);remaining=Math.clamp(in.getIntOr("remaining",0),0,100000);total=Math.clamp(in.getIntOr("total",remaining),remaining,100000);try{owner=UUID.fromString(in.getStringOr("owner",""));}catch(IllegalArgumentException e){owner=null;}}
}
