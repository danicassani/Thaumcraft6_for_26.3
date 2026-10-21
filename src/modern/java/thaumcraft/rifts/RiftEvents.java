package thaumcraft.rifts;
/** Original 50:10:20:20:1 distribution, with stable boundaries for saves and tests. */
public final class RiftEvents{
 private RiftEvents(){}
 public enum Kind{WISP,SEED,EXHAUST,CLOUD,COLLAPSE}
 public static Kind select(int roll){if(roll<0||roll>100)throw new IllegalArgumentException("Rift roll outside 0..100");return roll<50?Kind.WISP:roll<60?Kind.SEED:roll<80?Kind.EXHAUST:roll<100?Kind.CLOUD:Kind.COLLAPSE;}
 public static int seconds(int size,int sample){int low=Math.min(Math.clamp(size,1,100)/2,30),high=Math.min(Math.clamp(size,1,100),120);return Math.clamp(sample,Math.max(5,low),Math.max(5,Math.min(30,high)));}
}
