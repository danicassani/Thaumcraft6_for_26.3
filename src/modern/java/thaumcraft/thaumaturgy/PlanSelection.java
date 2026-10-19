package thaumcraft.thaumaturgy;

import java.util.*;
import java.util.function.Predicate;
import net.minecraft.core.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/** The same bounded selection is used for the client preview and authoritative block effects. */
public final class PlanSelection {
    private PlanSelection(){}
    public static List<BlockPos> select(Level level,BlockHitResult hit,FocusProgram.PlanSettings settings){
        var start=hit.getBlockPos();if(!level.hasChunkAt(start)||hit.getType()!=net.minecraft.world.phys.HitResult.Type.BLOCK)return List.of();
        var state=level.getBlockState(start);
        return select(hit,settings,q->!level.isOutsideBuildHeight(q)&&level.getWorldBorder().isWithinBounds(q)&&level.hasChunkAt(q),q->!level.getBlockState(q).isAir(),q->{
            if(!level.getBlockState(q).equals(state))return false;
            for(var face:Direction.values()){var adjacent=q.relative(face);if(level.hasChunkAt(adjacent)&&!level.getBlockState(adjacent).isSolidRender())return true;}return false;
        });
    }
    static List<BlockPos> select(BlockHitResult hit,FocusProgram.PlanSettings settings,Predicate<BlockPos> loaded,Predicate<BlockPos> nonAir,Predicate<BlockPos> sameExposed){
        if(!settings.valid())return List.of();var start=hit.getBlockPos();var side=hit.getDirection();var result=new ArrayList<BlockPos>();
        if(!settings.surface()){
            var center=start.offset(-settings.x()*side.getStepX(),-settings.y()*side.getStepY(),-settings.z()*side.getStepZ());
            for(var p:BlockPos.betweenClosed(center.offset(-settings.x(),-settings.y(),-settings.z()),center.offset(settings.x(),settings.y(),settings.z())))if(loaded.test(p)&&nonAir.test(p))result.add(p.immutable());
        }else{
            var pending=new ArrayDeque<BlockPos>();var visited=new HashSet<BlockPos>();pending.add(start);
            while(!pending.isEmpty()){
                var p=pending.removeFirst();if(!visited.add(p)||!insideSurface(start,p,side,settings)||!loaded.test(p)||!nonAir.test(p)||!sameExposed.test(p))continue;
                result.add(p);for(var d:Direction.values())if(d.getAxis()!=side.getAxis())pending.add(p.relative(d));
            }
        }
        result.sort(Comparator.<BlockPos>comparingDouble(p->p.distSqr(start)).thenComparingLong(BlockPos::asLong));return List.copyOf(result);
    }
    private static boolean insideSurface(BlockPos origin,BlockPos p,Direction face,FocusProgram.PlanSettings s){int x=Math.abs(p.getX()-origin.getX()),y=Math.abs(p.getY()-origin.getY()),z=Math.abs(p.getZ()-origin.getZ());return switch(face.getAxis()){case Y->y==0&&x<=s.x()&&z<=s.z();case Z->z==0&&x<=s.z()&&y<=s.x();case X->x==0&&y<=s.x()&&z<=s.z();};}
}
