package thaumcraft.theory;

import com.mojang.serialization.Codec;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.persistence.PlayerWarpStore;
import thaumcraft.research.*;

public final class ResearchTableEntity extends thaumcraft.devices.AuraDeviceEntity implements MenuProvider {
    public final SimpleContainer inventory=new SimpleContainer(2){@Override public void setChanged(){super.setChanged();ResearchTableEntity.this.setChanged();}};
    private static final com.google.gson.JsonObject META=ResearchCatalog.load("theory_meta.json").getAsJsonObject();
    private TheorySession session;
    private Tag unsupported;
    private int revision;
    public ResearchTableEntity(BlockPos p,BlockState s){super(TheoryContent.TYPE.get(),p,s);}
    @Override public void serverTick(){}
    public TheorySession session(){return session;}
    public boolean supported(){return unsupported==null;}
    public int revision(){return revision;}
    private void commit(TheorySession value){session=value;revision=(revision+1)%32768;setChanged();}
    public boolean valid(Player p){return level instanceof ServerLevel&&level.getBlockEntity(worldPosition)==this&&p.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)<=64;}
    public boolean owned(Player p){return session==null||session.owner.equals(p.getUUID().toString());}
    @Override public Component getDisplayName(){return Component.translatable("block.thaumcraft.research_table");}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player p){return new TheoryMenu(id,inventory,this);}
    public Set<String> nearbyAids(){
        var result=new TreeSet<String>();if(level==null)return result;
        for(var pos:BlockPos.betweenClosed(worldPosition.offset(-4,-1,-4),worldPosition.offset(4,1,4))){
            if(!level.hasChunkAt(pos))continue;String id=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString();if(TheoryCards.AIDS.containsKey(id))result.add(id);
        }
        return result;
    }
    public static int inspiration(Player p){
        var state=ResearchService.state(p);if(state==null)return 5;float amount=5;
        var meta=META;
        for(var e:meta.entrySet())if(ResearchCatalog.knows(state,e.getKey()))amount+=e.getValue().getAsFloat();
        return Math.min(15,Math.round(amount));
    }
    public boolean start(Player p,List<String> aids){
        if(!valid(p)||!supported()||session!=null||ResearchService.state(p)==null||!ResearchService.knows(p,"THEORYRESEARCH")||!TheoryContent.ink(inventory.getItem(0),1)||!inventory.getItem(1).is(Items.PAPER))return false;
        var unique=new HashSet<>(aids);if(unique.size()!=aids.size()||!nearbyAids().containsAll(aids)||aids.size()>=inspiration(p))return false;
        commit(TheorySession.start(p.getUUID(),inspiration(p),aids));return true;
    }
    public boolean draw(Player p,boolean bonus){
        if(!active(p)||session.inspiration<=0||!session.choices.isEmpty()||!inventory.getItem(1).is(Items.PAPER))return false;
        var next=session.copy();int count=bonus&&next.bonus>0?3:2;if(count==3)next.bonus--;
        next.choices=TheoryCards.draw(p,next,count,level.getRandom().nextLong());if(next.choices.isEmpty())return false;
        inventory.removeItem(1,1);commit(next);return true;
    }
    private boolean active(Player p){return valid(p)&&supported()&&session!=null&&owned(p)&&ResearchService.state(p)!=null&&ResearchService.knows(p,"THEORYRESEARCH");}
    /** Reserve all requirements, including non-consumed items, before changing anything. */
    private int[] plan(Player p,TheoryCards.Card card){
        int size=p.getInventory().getContainerSize();int[] left=new int[size],take=new int[size];for(int i=0;i<size;i++)left[i]=p.getInventory().getItem(i).getCount();
        for(var need:card.needs()){int n=need.stack().getCount();for(int i=0;i<size&&n>0;i++)if(ItemStack.isSameItemSameComponents(need.stack(),p.getInventory().getItem(i))){int use=Math.min(n,left[i]);left[i]-=use;n-=use;if(need.consume())take[i]+=use;}if(n>0)return null;}
        return take;
    }
    public boolean ready(Player p,int index){
        if(!active(p)||index<0||index>=session.choices.size())return false;var card=session.choices.get(index);int ink=card.kind()==TheoryCards.Kind.SCRIPTING?2:1;
        if(card.cost()>session.inspiration||!TheoryContent.ink(inventory.getItem(0),ink)||plan(p,card)==null||card.kind()==TheoryCards.Kind.SCRIPTING&&!inventory.getItem(1).is(Items.PAPER))return false;
        return TheoryCards.apply(card,session.copy(),p,ResearchService.state(p))!=null;
    }
    public boolean play(Player p,int index){
        if(!ready(p,index))return false;var card=session.choices.get(index);var next=session.copy();var old=ResearchService.state(p);var effect=TheoryCards.apply(card,next,p,old);var take=plan(p,card);if(effect==null||take==null)return false;
        PlayerWarpStore.State warp=null;
        if((effect.normalWarp()>0||effect.temporaryWarp()>0)&&!ThaumcraftConfig.wussMode.get()){
            if(!(p instanceof ServerPlayer real))return false;var saved=PlayerWarpStore.read(real).result();if(saved.isEmpty())return false;var w=saved.get();warp=new PlayerWarpStore.State(w.version(),w.permanent(),Math.min(500,w.normal()+effect.normalWarp()),Math.min(500,w.temporary()+effect.temporaryWarp()),w.counter());
        }
        if(!KnowledgeStore.write(p,effect.knowledge()))return false;
        if(warp!=null&&PlayerWarpStore.write((ServerPlayer)p,warp).result().isEmpty()){KnowledgeStore.write(p,old);return false;}
        for(int i=0;i<take.length;i++)if(take[i]>0)p.getInventory().removeItem(i,take[i]);
        if(effect.levels()>0)p.giveExperienceLevels(-effect.levels());
        var ink=inventory.getItem(0);ink.setDamageValue(ink.getDamageValue()+(card.kind()==TheoryCards.Kind.SCRIPTING?2:1));inventory.setChanged();
        if(card.kind()==TheoryCards.Kind.SCRIPTING)inventory.removeItem(1,1);
        if(!effect.reward().isEmpty()&&!p.getInventory().add(effect.reward()))Block.popResource(level,worldPosition,effect.reward());
        commit(next);p.getInventory().setChanged();return true;
    }
    public boolean finish(Player p){
        if(!active(p)||session.inspiration>0||!session.choices.isEmpty())return false;var state=ResearchService.state(p);
        for(var reward:session.rewards().entrySet())state=state.addPoints("THEORY",reward.getKey(),reward.getValue());
        if(!KnowledgeStore.write(p,state))return false;commit(null);return true;
    }
    public boolean cancel(Player p){if(!active(p))return false;commit(null);return true;}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);for(int i=0;i<2;i++)if(!inventory.getItem(i).isEmpty())out.store("slot"+i,ItemStack.CODEC,inventory.getItem(i));if(unsupported!=null)out.store("theory",Codec.PASSTHROUGH,new com.mojang.serialization.Dynamic<>(NbtOps.INSTANCE,unsupported));else if(session!=null)out.store("theory",TheorySession.CODEC,session);}
    @Override protected void loadAdditional(ValueInput in){
        super.loadAdditional(in);for(int i=0;i<2;i++)inventory.setItem(i,in.read("slot"+i,ItemStack.CODEC).orElse(ItemStack.EMPTY));
        session=null;unsupported=null;var raw=in.read("theory",Codec.PASSTHROUGH);if(raw.isPresent()){Tag tag=raw.get().convert(NbtOps.INSTANCE).getValue();var decoded=TheorySession.CODEC.parse(NbtOps.INSTANCE,tag).result();if(decoded.isPresent())session=decoded.get();else unsupported=tag.copy();}
    }
    @Override public void preRemoveSideEffects(BlockPos pos,BlockState state){super.preRemoveSideEffects(pos,state);if(level instanceof ServerLevel)for(int i=0;i<2;i++)Block.popResource(level,pos,inventory.removeItemNoUpdate(i));}
}
