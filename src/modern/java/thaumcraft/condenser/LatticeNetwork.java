package thaumcraft.condenser;
import java.util.*;
import net.minecraft.core.*;
/** Original ordered depth-first efficiency calculation, bounded and without world/chunk access. */
public final class LatticeNetwork{
 public static final int AIR=0,CLEAN=1,DIRTY=2,BASE=3,UNLOADED=-1;
 public interface View{int kind(BlockPos p);}
 public record Result(List<BlockPos> cells,List<BlockPos> clean,float efficiency,int interval,int cost,boolean complete,boolean shared){
  public boolean active(){return complete&&!shared&&efficiency>0;}
 }
 private static final class Frame{final BlockPos pos;final boolean root;boolean clogged,found;int side,sides;Frame(BlockPos p,boolean root,boolean clogged){pos=p;this.root=root;this.clogged=clogged;}}
 private LatticeNetwork(){}
 public static Result scan(BlockPos base,View view){var cells=new ArrayList<BlockPos>();var clean=new ArrayList<BlockPos>();var visited=new HashSet<BlockPos>();var stack=new ArrayDeque<Frame>();visited.add(base);stack.push(new Frame(base,true,false));float efficiency=0;
  while(!stack.isEmpty()){var f=stack.peek();if(f.side==6){if(f.found&&!f.clogged)efficiency+=1-.15F*f.sides;stack.pop();continue;}var d=Direction.values()[f.side++];if(f.root&&d!=Direction.UP)continue;var p=f.pos.relative(d);int kind=view.kind(p);if(kind==UNLOADED)return new Result(List.copyOf(cells),List.copyOf(clean),0,0,0,false,false);boolean lattice=kind==CLEAN||kind==DIRTY;if(f.root&&kind==DIRTY)f.clogged=true;if(lattice)f.sides++;if(visited.contains(p))continue;if(d==Direction.DOWN&&kind==BASE)return new Result(List.copyOf(cells),List.copyOf(clean),0,0,0,true,true);
   if(p.getY()>base.getY()&&base.distSqr(p)<=74&&lattice){visited.add(p);cells.add(p);if(kind==CLEAN)clean.add(p);f.found=true;stack.push(new Frame(p,false,f.clogged||kind==DIRTY));}
  }
  efficiency=Math.clamp(efficiency,0,40);return new Result(List.copyOf(cells),List.copyOf(clean),efficiency,efficiency>0?Math.max(5,Math.round(600-efficiency*15)):0,efficiency>0?(int)(4+Math.sqrt(cells.size())):0,true,false);
 }
}
