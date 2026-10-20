package thaumcraft.infernal;
/** Original infernal furnace timing, capped at four bellows. */
public final class InfernalRules{
 private InfernalRules(){}
 public static int cookTicks(boolean aura,int bellows){int b=Math.clamp(bellows,0,4);return Math.max(10,(aura?80:140)-b*(21-b));}
 public static int bonusRolls(int bellows){return 1+Math.clamp(bellows,0,4);}
}
