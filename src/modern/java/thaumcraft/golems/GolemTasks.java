package thaumcraft.golems;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import thaumcraft.thaumaturgy.SpellEngine;

/** Bounded loaded-world searches. No task forces chunks or caches a list of victims. */
public final class GolemTasks {
    public static AABB area(SealEntity s){return new AABB(s.getBlockPos()).inflate(s.radius);}
    public static boolean adult(Animal a){return a.isAlive()&&!a.isBaby()&&!(a instanceof Enemy)&&!(a instanceof TamableAnimal t&&t.isTame())&&!a.isLeashed();}
    public static boolean butcher(ServerLevel l,SealEntity s,ServerPlayer owner,Animal a){
        if(!adult(a)||!area(s).contains(a.position())||SpellEngine.protectedTarget(owner,a))return false;
        return l.getEntitiesOfClass(Animal.class,area(s),other->other.getType()==a.getType()&&adult(other)&&!SpellEngine.protectedTarget(owner,other)).size()>2;
    }
    public static boolean inTimberArea(SealEntity s,BlockPos q){return inTimberArea(s.getBlockPos(),s.radius,q);}
    public static boolean inTimberArea(BlockPos p,int radius,BlockPos q){return Math.abs((long)q.getX()-p.getX())<=radius&&Math.abs((long)q.getZ()-p.getZ())<=radius&&(long)q.getY()>=p.getY()-1L&&(long)q.getY()<=p.getY()+radius*2L;}
    public static BlockPos root(ServerLevel l,SealEntity s,BlockPos worker){BlockPos best=null;double distance=Double.MAX_VALUE;
        for(var q:BlockPos.betweenClosed(s.getBlockPos().offset(-s.radius,-1,-s.radius),s.getBlockPos().offset(s.radius,s.radius*2,s.radius))){if(!l.hasChunkAt(q)||!l.getBlockState(q).is(BlockTags.LOGS))continue;if(l.hasChunkAt(q.below())&&l.getBlockState(q.below()).is(BlockTags.LOGS))continue;double d=q.distSqr(worker);if(d<distance){distance=d;best=q.immutable();}}
        return best;
    }
    public static BlockPos furthest(ServerLevel l,SealEntity s,BlockPos root){if(!l.hasChunkAt(root)||!l.getBlockState(root).is(BlockTags.LOGS))return null;
        var kind=l.getBlockState(root).getBlock();var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();seen.add(root);queue.add(root);BlockPos best=root;
        while(!queue.isEmpty()){var p=queue.remove();if(p.distSqr(root)>best.distSqr(root))best=p;
            for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++){var q=p.offset(x,y,z);if(seen.contains(q)||seen.size()>=256||!inTimberArea(s,q)||!l.hasChunkAt(q)||!l.getBlockState(q).is(kind))continue;seen.add(q);queue.add(q);}}
        return best;
    }
    public static boolean seesTimber(ServerLevel l,WorkerGolem g,BlockPos root){var end=net.minecraft.world.phys.Vec3.atCenterOf(root);if(!SpellEngine.loadedLine(l,g.getEyePosition(),end))return false;var hit=l.clip(new net.minecraft.world.level.ClipContext(g.getEyePosition(),end,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,g));return hit.getType()==net.minecraft.world.phys.HitResult.Type.MISS||hit.getBlockPos().equals(root)||hit.getBlockPos().distSqr(root)<=2&&l.getBlockState(hit.getBlockPos()).is(l.getBlockState(root).getBlock());}
    private GolemTasks(){}
}
