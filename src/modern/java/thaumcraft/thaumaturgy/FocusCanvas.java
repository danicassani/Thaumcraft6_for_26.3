package thaumcraft.thaumaturgy;
import java.util.*;
/** Deterministic projection shared by server selection and client drawing; positions are presentation only. */
public final class FocusCanvas {
 public static final int MEDIUM=0,EFFECT=1,MODIFIER=2,FIRST_SELECT=32,LAST_SELECT=79;
 public record Node(List<Integer> address,int branch,int effect,int kind,String name,int parent,int x,int y){public Node{address=List.copyOf(address);}}
 public static List<Node> nodes(FocusProgram program){var out=new ArrayList<Node>();walk(program,List.of(),-1,0,new int[]{0},out);return List.copyOf(out);}
 private static void walk(FocusProgram p,List<Integer> path,int parent,int x,int[] row,List<Node> out){
  int connector=parent;if(!p.modifier().equals("none")){connector=out.size();out.add(new Node(path,0,-1,MODIFIER,p.modifier(),parent,x,row[0]*36));x+=36;}
  for(int branch=0;branch<=p.branches().size();branch++){var b=branch==0?p.root():p.branches().get(branch-1);int y=row[0]++*36,px=x,previous=out.size();out.add(new Node(path,branch,-1,MEDIUM,branch==1&&p.modifier().equals("split_target")?"impact":b.medium(),connector,px,y));
   for(int e=0;e<b.effects().size();e++){px+=36;int index=out.size();out.add(new Node(path,branch,e,EFFECT,b.effects().get(e).kind(),previous,px,y));previous=index;}
   var child=p.child(branch);if(child!=null){var next=new ArrayList<>(path);next.add(branch);row[0]--;walk(child,next,previous,px+36,row,out);}
  }
 }
 private FocusCanvas(){}
}
