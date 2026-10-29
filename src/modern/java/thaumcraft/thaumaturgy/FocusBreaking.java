package thaumcraft.thaumaturgy;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import thaumcraft.aura.AuraManager;

/** Server-only, transient work. Never loads terrain or repeats a harvest after restart. */
public final class FocusBreaking {
    private static final Map<ServerLevel,Map<BlockPos,Job>> LEVELS = new IdentityHashMap<>();
    private static int nextId = -1_000_000;
    private static final class Job {
        final ServerPlayer owner;
        final BlockPos pos;
        final Direction face;
        final BlockState source;
        final net.minecraft.world.level.block.entity.BlockEntity entity;
        final FocusProgram.Effect effect;
        final float strength,total;
        final long start,expires;
        final int id;
        float remaining;
        int cracks = -1;
        Job(ServerPlayer owner,BlockPos pos,Direction face,BlockState source,FocusProgram.Effect effect,float strength,int index) {
            this.owner=owner;this.pos=pos.immutable();this.face=face;this.source=source;this.effect=effect;this.strength=strength;this.entity=owner.level().getBlockEntity(pos);
            total=remaining=BreakWork.durability(source.getDestroySpeed(owner.level(),pos));
            start=owner.level().getGameTime()+BreakWork.delay(total,strength,index);
            expires=start+1200;id=nextId--;if(nextId==Integer.MIN_VALUE)nextId=-1_000_000;
        }
    }
    private FocusBreaking() {}
    public static void register() {
        TickEvent.LevelTickEvent.Post.BUS.addListener(e->{if(e.level() instanceof ServerLevel l&&l.tickRateManager().runsNormally())tick(l);});
        PlayerEvent.PlayerLoggedOutEvent.BUS.addListener(e->{if(e.getEntity() instanceof ServerPlayer p)cancel(p);});
        LevelEvent.Unload.BUS.addListener(e->{if(e.getLevel() instanceof ServerLevel l){var jobs=LEVELS.remove(l);if(jobs!=null)jobs.values().forEach(j->clear(l,j));}});
        net.minecraftforge.event.server.ServerStoppedEvent.BUS.addListener(e->LEVELS.clear());
    }
    private static boolean eligible(ServerPlayer p,ServerLevel l,BlockPos pos,Direction face,BlockState source,net.minecraft.world.level.block.entity.BlockEntity entity) {
        return p.level()==l&&p.isAlive()&&!p.isRemoved()&&!p.isSpectator()&&!l.isOutsideBuildHeight(pos)
            &&l.getWorldBorder().isWithinBounds(pos)&&l.hasChunkAt(pos)&&l.getBlockState(pos).equals(source)
            &&!source.isAir()&&!(source.getBlock() instanceof net.minecraft.world.level.block.GameMasterBlock)
            &&l.getBlockEntity(pos)==entity&&(!source.hasBlockEntity()||entity!=null)
            &&(!(entity instanceof thaumcraft.devices.AuraDeviceEntity device)||device.supported())
            &&Float.isFinite(source.getDestroySpeed(l,pos))&&source.getDestroySpeed(l,pos)>=0
            &&l.mayInteract(p,pos)&&p.mayUseItemAt(pos,face,p.getMainHandItem());
    }
    public static boolean start(ServerPlayer p,BlockPos pos,Direction face,FocusProgram.Effect effect,float strength,int index) {
        var l=p.level();if(!l.getServer().isSameThread())throw new IllegalStateException("Rotura outside server thread");
        if(!effect.valid()||!effect.kind().equals("break")||!Float.isFinite(strength)||strength<=0||strength>effect.power()||index<0||index>342||!l.hasChunkAt(pos))return false;
        var source=l.getBlockState(pos);if(!eligible(p,l,pos,face,source,l.getBlockEntity(pos)))return false;
        var jobs=LEVELS.computeIfAbsent(l,k->new LinkedHashMap<>());
        if(jobs.containsKey(pos)||jobs.size()>=2048||jobs.values().stream().filter(j->j.owner==p).count()>=512)return false;
        if(AuraManager.drainVis(l,pos,BreakWork.cost(effect),true)<BreakWork.cost(effect))return false;
        var job=new Job(p,pos,face,source,effect,strength,index);jobs.put(job.pos,job);return true;
    }
    private static void clear(ServerLevel l,Job j) {if(j.cracks>=0)l.destroyBlockProgress(j.id,j.pos,-1);}
    private static void cancel(ServerPlayer p) {
        for(var entry:LEVELS.entrySet())entry.getValue().values().removeIf(j->{if(j.owner!=p)return false;clear(entry.getKey(),j);return true;});
    }
    private static void tick(ServerLevel l) {
        var jobs=LEVELS.get(l);if(jobs==null)return;
        // Remove before callbacks: Forge harvest listeners may enqueue more spells.
        for(var j:List.copyOf(jobs.values())) {
            if(jobs.get(j.pos)!=j)continue;
            long now=l.getGameTime();float cost=BreakWork.cost(j.effect);
            if(now>j.expires||!eligible(j.owner,l,j.pos,j.face,j.source,j.entity)||AuraManager.drainVis(l,j.pos,cost,true)<cost){jobs.remove(j.pos,j);clear(l,j);continue;}
            if(now<j.start)continue;
            j.remaining-=j.strength;int stage=BreakWork.cracks(j.remaining,j.total);
            if(stage!=j.cracks){j.cracks=stage;l.destroyBlockProgress(j.id,j.pos,stage);}
            if(j.remaining>0)continue;
            jobs.remove(j.pos,j);clear(l,j);
            float paid=AuraManager.drainVis(l,j.pos,cost,false);boolean harvested=false;
            try {if(paid>=cost)harvested=FocusHarvest.destroy(j.owner,j.pos,j.effect);}
            finally {if(!harvested&&paid>0)AuraManager.addVis(l,j.pos,paid);}
        }
        if(jobs.isEmpty())LEVELS.remove(l);
    }
}
