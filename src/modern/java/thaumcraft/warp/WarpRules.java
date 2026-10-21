package thaumcraft.warp;
import thaumcraft.persistence.PlayerWarpStore.State;
/** TC6's trigger and severity calculations, without world or random side effects. */
public final class WarpRules {
 private WarpRules(){}
 public record Decision(int temporary,int counter,int severity,int power,boolean triggered){}
 public static State add(State w,int permanent,int normal,int temporary){int p=(int)Math.clamp((long)w.permanent()+permanent,0,500),n=(int)Math.clamp((long)w.normal()+normal,0,500),t=(int)Math.clamp((long)w.temporary()+temporary,0,500);return new State(w.version(),p,n,t,permanent>0||normal>0||temporary>0?p+n+t:w.counter());}
 public static Decision decide(State w,int gear,int roll,int sample){if(roll<0||roll>99||gear<0||gear>100)throw new IllegalArgumentException("Invalid warp roll/gear");int t=Math.max(0,w.temporary()-1),total=w.permanent()+w.normal()+t+gear,c=Math.clamp(w.counter(),0,1500);if(c==0||total==0||roll>Math.sqrt(c))return new Decision(t,c,0,0,false);int power=Math.max(1,Math.min(100,(total*2+c)/3));int next=Math.max(0,c-(int)Math.max(5,Math.sqrt(c)*2-gear*2));return new Decision(t,next,Math.floorMod(sample,power)+gear,power,true);}
 public static State clean(State w,boolean ward,boolean bath){return new State(w.version(),w.permanent(),Math.max(0,w.normal()-1-(ward?1:0)-(bath?1:0)),0,w.counter());}
 public static int band(int severity){return severity<=0?0:severity<=72?(severity+3)/4:severity==76?19:severity<=80?20:severity<=88?21:severity<=92?22:23;}
}
