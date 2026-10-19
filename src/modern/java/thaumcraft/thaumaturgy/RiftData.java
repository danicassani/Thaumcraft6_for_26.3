package thaumcraft.thaumaturgy;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import java.nio.file.Files;
import net.minecraft.core.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.*;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.TickEvent;

/** Authoritative restoration journal, independent of block entities and loaded chunks. */
public final class RiftData extends SavedData {
    public static final int MAX_HOLES=1024,MAX_OWNER=288;
    public static final TagKey<Block> IMMUNE=TagKey.create(Registries.BLOCK,Identifier.parse("thaumcraft:rift_immune"));
    public record Entry(long pos,BlockState original,long deadline,UUID owner){
        static final Codec<Entry> CODEC=RecordCodecBuilder.<Entry>create(i->i.group(Codec.LONG.fieldOf("pos").forGetter(Entry::pos),BlockState.CODEC.fieldOf("original").forGetter(Entry::original),Codec.LONG.fieldOf("deadline").forGetter(Entry::deadline),UUIDUtil.STRING_CODEC.fieldOf("owner").forGetter(Entry::owner)).apply(i,Entry::new)).validate(e->e.deadline>=0&&!e.original.isAir()&&!e.original.hasBlockEntity()&&!e.original.is(ThaumaturgyContent.HOLE.get())&&e.original.getFluidState().isEmpty()?DataResult.success(e):DataResult.error(()->"Invalid rift restoration"));
    }
    private static final Codec<RiftData> FORMAT=RecordCodecBuilder.<RiftData>create(i->i.group(Codec.intRange(1,1).fieldOf("version").forGetter(d->1),Entry.CODEC.listOf(0,MAX_HOLES).fieldOf("holes").forGetter(d->List.copyOf(d.entries.values()))).apply(i,(v,list)->new RiftData(list))).validate(d->d.entries.size()==d.decodedSize&&d.counts.values().stream().allMatch(n->n<=MAX_OWNER)?DataResult.success(d):DataResult.error(()->"Duplicate or excessive rift journal entries"));
    static final Codec<RiftData> CODEC=Codec.of(FORMAT,new Decoder<>(){public <T> DataResult<com.mojang.datafixers.util.Pair<RiftData,T>> decode(DynamicOps<T> ops,T input){var r=FORMAT.decode(ops,input);return r.result().map(DataResult::success).orElseGet(()->DataResult.error(()->r.error().orElseThrow().message()));}});
    public static final SavedDataType<RiftData> TYPE=new SavedDataType<>(Identifier.parse("thaumcraft:rift_holes"),RiftData::new,CODEC,null);
    private final Map<Long,Entry> entries=new LinkedHashMap<>();private final Map<UUID,Integer> counts=new HashMap<>();private int decodedSize;
    public RiftData(){this(List.of());}
    private RiftData(List<Entry> list){decodedSize=list.size();for(var e:list){entries.put(e.pos,e);counts.merge(e.owner,1,Integer::sum);}}
    public Collection<Entry> entries(){return List.copyOf(entries.values());}
    public int available(UUID owner){return Math.min(MAX_HOLES-entries.size(),MAX_OWNER-counts.getOrDefault(owner,0));}
    public boolean reserve(Entry e){if(e.original==null||e.owner==null||e.deadline<0||e.original.isAir()||e.original.hasBlockEntity()||e.original.is(ThaumaturgyContent.HOLE.get())||!e.original.getFluidState().isEmpty())return false;if(entries.containsKey(e.pos)||available(e.owner)<=0)return false;entries.put(e.pos,e);counts.merge(e.owner,1,Integer::sum);decodedSize=entries.size();setDirty();return true;}
    private void remove(long pos){var e=entries.remove(pos);if(e!=null){counts.computeIfPresent(e.owner,(u,n)->n<=1?null:n-1);decodedSize=entries.size();setDirty();}}
    public static RiftData get(ServerLevel l){var d=l.getDataStorage().get(TYPE);if(d!=null)return d;var path=DimensionType.getStorageFolder(l.dimension(),l.getServer().getWorldPath(LevelResource.ROOT)).resolve("data/thaumcraft/rift_holes.dat");if(Files.exists(path))throw new IllegalStateException("Cannot decode rift restoration journal; preserving "+path);return l.getDataStorage().computeIfAbsent(TYPE);}
    public static void register(){net.minecraftforge.event.level.BlockEvent.BreakEvent.BUS.addListener(e->{if(e.getState().is(ThaumaturgyContent.HOLE.get()))e.setResult(net.minecraftforge.common.util.Result.DENY);});TickEvent.ServerTickEvent.Post.BUS.addListener(e->{for(var l:e.server().getAllLevels())if(l.getGameTime()%5==0){var data=l.getDataStorage().get(TYPE);if(data!=null)data.restore(l);}});}
    public void restore(ServerLevel l){long now=l.getGameTime();for(var entry:List.copyOf(entries.values())){var pos=BlockPos.of(entry.pos);if(!l.hasChunkAt(pos))continue;if(!l.getBlockState(pos).is(ThaumaturgyContent.HOLE.get())){remove(entry.pos);continue;}if(now>=entry.deadline&&l.setBlock(pos,entry.original,Block.UPDATE_ALL))remove(entry.pos);}}
    private static boolean eligible(ServerLevel l,BlockPos q){if(l.isOutsideBuildHeight(q)||!l.getWorldBorder().isWithinBounds(q)||!l.hasChunkAt(q))return false;var s=l.getBlockState(q);return !s.is(IMMUNE)&&!s.is(ThaumaturgyContent.HOLE.get())&&!s.hasBlockEntity()&&l.getBlockEntity(q)==null&&s.getFluidState().isEmpty()&&s.isSolidRender()&&s.isCollisionShapeFullBlock(l,q)&&s.getDestroySpeed(l,q)>=0&&!(s.getBlock() instanceof FallingBlock)&&!(s.getBlock() instanceof net.minecraft.world.level.block.piston.PistonBaseBlock)&&!(s.getBlock() instanceof GameMasterBlock);}
    private boolean replace(ServerPlayer p,BlockPos q,Direction side,long deadline){var l=p.level();if(available(p.getUUID())<=0||!eligible(l,q)||!l.mayInteract(p,q)||!p.mayUseItemAt(q,side,p.getMainHandItem()))return false;
        if(net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(l,p.gameMode.getGameModeForPlayer(),p,q)<0)return false;
        var state=l.getBlockState(q);if(!eligible(l,q))return false;var snapshot=net.minecraftforge.common.util.BlockSnapshot.create(l.dimension(),l,q);var entry=new Entry(q.asLong(),state,deadline,p.getUUID());if(!reserve(entry))return false;
        if(!l.setBlock(q,ThaumaturgyContent.HOLE.get().defaultBlockState(),Block.UPDATE_CLIENTS)){remove(q.asLong());return false;}
        if(net.minecraftforge.event.ForgeEventFactory.onBlockPlace(p,snapshot,side)){if(l.getBlockState(q).is(ThaumaturgyContent.HOLE.get()))l.setBlock(q,state,Block.UPDATE_ALL);remove(q.asLong());return false;}
        if(!l.getBlockState(q).is(ThaumaturgyContent.HOLE.get())){remove(q.asLong());return false;}
        l.updateNeighborsAt(q,ThaumaturgyContent.HOLE.get());return true;
    }
    public static int open(ServerPlayer p,BlockHitResult hit,FocusProgram.Effect effect,float strength){if(!effect.valid()||!effect.kind().equals("rift")||!Float.isFinite(strength)||strength<=0||strength>1||p.isSpectator()||!p.getAbilities().mayBuild)return 0;var l=p.level();var journal=get(l);int opened=0;int depth=Math.min(effect.depth(),(int)Math.ceil(effect.depth()*strength));long deadline=l.getGameTime()+effect.duration()*20L;var face=hit.getDirection();
        for(int step=0;step<depth&&journal.available(p.getUUID())>0;step++){var center=hit.getBlockPos().relative(face.getOpposite(),step);if(!journal.replace(p,center,face,deadline))break;opened++;
            for(int a=-1;a<=1;a++)for(int b=-1;b<=1;b++)if(a!=0||b!=0){var q=switch(face.getAxis()){case X->center.offset(0,a,b);case Y->center.offset(a,0,b);case Z->center.offset(a,b,0);};if(journal.replace(p,q,face,deadline))opened++;}
        }return opened;
    }
}
