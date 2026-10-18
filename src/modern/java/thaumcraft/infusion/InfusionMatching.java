package thaumcraft.infusion;
import java.util.Arrays;
/** Bipartite matching handles overlapping tags, duplicate ingredients and arbitrary pedestal order. */
public final class InfusionMatching{
    private InfusionMatching(){}
    public static int[] assign(boolean[][] accepts){int n=accepts.length;for(var row:accepts)if(row.length!=n)return null;int[] owner=new int[n];Arrays.fill(owner,-1);for(int r=0;r<n;r++)if(!visit(r,accepts,owner,new boolean[n]))return null;int[] slots=new int[n];for(int s=0;s<n;s++)slots[owner[s]]=s;return slots;}
    private static boolean visit(int r,boolean[][] accepts,int[] owner,boolean[] seen){for(int s=0;s<owner.length;s++)if(accepts[r][s]&&!seen[s]){seen[s]=true;if(owner[s]<0||visit(owner[s],accepts,owner,seen)){owner[s]=r;return true;}}return false;}
}
