package thaumcraft.condenser;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import net.minecraft.core.*;
import org.junit.jupiter.api.Test;
class LatticeNetworkTest{
 private static final BlockPos P=new BlockPos(0,100,0);
 private static LatticeNetwork.Result scan(Map<BlockPos,Integer> cells){return LatticeNetwork.scan(P,p->cells.getOrDefault(p,0));}
 @Test void oneTwoAndThreeOriginalVerticalLattices(){var cells=new HashMap<BlockPos,Integer>();for(int n=1;n<=3;n++){cells.put(P.above(n),1);var r=scan(cells);assertTrue(r.active());assertEquals(n,r.cells().size());assertEquals(n==1?.85F:n==2?1.7F:2.4F,r.efficiency(),.00001);assertEquals(n==1?587:n==2?575:564,r.interval());assertEquals(5,r.cost());}}
 @Test void missingOrCloggedRootDisablesAllBranches(){assertFalse(scan(Map.of()).active());assertFalse(scan(Map.of(P.above(),2,P.above(2),1)).active());}
 @Test void dirtyBranchKeepsOtherBranchesAndAllMaterialCosts(){var cells=new HashMap<BlockPos,Integer>();for(int n=1;n<=4;n++)cells.put(P.above(n),1);float full=scan(cells).efficiency();cells.put(P.above(2),2);var clogged=scan(cells);assertTrue(clogged.active());assertTrue(clogged.efficiency()<full);assertEquals(4,clogged.cells().size());assertEquals(6,clogged.cost());assertEquals(3,clogged.clean().size());}
 @Test void sharedAndUnknownNetworksCannotWork(){assertTrue(scan(Map.of(P.above(),1,P.above().east(),1,P.east(),3)).shared());assertFalse(scan(Map.of(P.above(),1,P.above().west(),-1)).complete());}
 @Test void ignoresDisconnectedBelowAndOutOfRangeCells(){var cells=new HashMap<BlockPos,Integer>();for(int i=1;i<=10;i++)cells.put(P.above(i),1);cells.put(P.below(),1);var r=scan(cells);assertEquals(8,r.cells().size());assertTrue(r.cells().stream().allMatch(p->p.getY()>P.getY()&&P.distSqr(p)<=74));}
 @Test void hugeLoadedGridCapsEfficiencyAndAvoidsRecursiveOverflow(){var cells=new HashMap<BlockPos,Integer>();for(int x=-8;x<=8;x++)for(int y=1;y<=8;y++)for(int z=-8;z<=8;z++){var p=P.offset(x,y,z);if(P.distSqr(p)<=74)cells.put(p,1);}var r=scan(cells);assertTrue(r.cells().size()>900);assertEquals(40,r.efficiency());assertEquals(5,r.interval());assertEquals((int)(4+Math.sqrt(r.cells().size())),r.cost());}
 @Test void seededBranchingGraphsMatchOriginalOrderedRecursiveCalculation(){var random=new Random(41);for(int sample=0;sample<100;sample++){var cells=new HashMap<BlockPos,Integer>();cells.put(P.above(),1);for(int x=-2;x<=2;x++)for(int y=1;y<=5;y++)for(int z=-2;z<=2;z++)if(random.nextBoolean())cells.put(P.offset(x,y,z),random.nextInt(4)==0?2:1);var original=new Reference(cells);original.visit(P,true,false);var r=scan(cells);assertEquals(Math.clamp(original.score,0,40),r.efficiency(),.00001);assertEquals(original.cells,r.cells().size());}}
 private static final class Reference{final Map<BlockPos,Integer> map;final Set<BlockPos> history=new HashSet<>();float score;int cells;Reference(Map<BlockPos,Integer> m){map=m;}void visit(BlockPos p,boolean skip,boolean clogged){history.add(p);boolean found=false;int sides=0;for(var d:Direction.values()){if(skip&&d!=Direction.UP)continue;var q=p.relative(d);int k=map.getOrDefault(q,0);boolean lattice=k==1||k==2;if(skip&&k==2)clogged=true;if(lattice)sides++;if(!history.contains(q)&&q.getY()>P.getY()&&P.distSqr(q)<=74&&lattice){cells++;found=true;visit(q,false,clogged||k==2);if(skip)break;}}if(found&&!clogged)score+=1-.15F*sides;}}
}
