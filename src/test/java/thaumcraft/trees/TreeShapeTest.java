package thaumcraft.trees;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import java.util.*;

class TreeShapeTest {
    @Test void shapesAreDeterministicAndBoundedAcrossSeeds() {
        for (int seed=0;seed<128;seed++) for (boolean silver:new boolean[]{false,true}) {
            var a=silver?TreeShape.silverwood(seed):TreeShape.greatwood(seed);
            var b=silver?TreeShape.silverwood(seed):TreeShape.greatwood(seed);
            assertEquals(a.logs,b.logs);assertEquals(a.leaves,b.leaves);
            assertTrue(a.logs.size()>35&&a.logs.size()<500);
            assertTrue(a.leaves.size()>100&&a.leaves.size()<4000);
            assertTrue(Collections.disjoint(a.logs.keySet(),a.leaves));
            for (var p:a.logs.keySet()) {assertTrue(Math.abs(p.getX())<16&&Math.abs(p.getZ())<16);assertTrue(p.getY()>=-1&&p.getY()<40);}
        }
    }
    @Test void allBranchesConnectToTheTrunk() {
        for (int seed=0;seed<64;seed++) {
            var shape=TreeShape.greatwood(seed);var visited=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();
            queue.add(BlockPos.ZERO);visited.add(BlockPos.ZERO);
            while (!queue.isEmpty()) {
                var p=queue.remove();
                // Legacy diagonal branch lines are connected through adjacent edge/corner voxels.
                for (var q:BlockPos.betweenClosed(p.offset(-1,-1,-1),p.offset(1,1,1))) if (shape.logs.containsKey(q)&&visited.add(q.immutable())) queue.add(q.immutable());
            }
            assertEquals(shape.logs.size(),visited.size());
            assertTrue(shape.logs.values().stream().anyMatch(a->a!=Direction.Axis.Y));
        }
    }
    @Test void silverwoodHasCrossTrunkRootsAndUpperArms() {
        for (int seed=0;seed<64;seed++) {
            var shape=TreeShape.silverwood(seed);
            for (int y=0;y<8;y++) for (var p:List.of(new BlockPos(0,y,0),new BlockPos(-1,y,0),new BlockPos(1,y,0),new BlockPos(0,y,-1),new BlockPos(0,y,1))) assertTrue(shape.logs.containsKey(p));
            assertEquals(Direction.Axis.Y,shape.logs.get(new BlockPos(2,-1,0)));
            assertEquals(Direction.Axis.X,shape.logs.get(new BlockPos(2,0,0)));
        }
    }
}
