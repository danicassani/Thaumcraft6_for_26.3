package thaumcraft.alchemy;

import com.mojang.serialization.*;
import java.util.*;
import thaumcraft.crystals.CrystalAspects;

/** Bounded, immutable amounts. Operations reserve all aspects before subtracting. */
public final class AspectAmounts {
    public static final int LIMIT=20000;
    public static final Codec<String> ASPECT=Codec.STRING.validate(s->CrystalAspects.COLORS.containsKey(s)?DataResult.success(s):DataResult.error(()->"Unknown aspect: "+s));
    public static final Codec<Map<String,Integer>> CODEC=Codec.unboundedMap(ASPECT,Codec.intRange(1,LIMIT)).validate(m->total(m)<=LIMIT?DataResult.success(Map.copyOf(m)):DataResult.error(()->"Excess aspect total"));
    private AspectAmounts(){}
    public static int total(Map<String,Integer> m){return m.values().stream().mapToInt(Integer::intValue).sum();}
    public static boolean contains(Map<String,Integer> available,Map<String,Integer> required){return required.entrySet().stream().allMatch(e->available.getOrDefault(e.getKey(),0)>=e.getValue());}
    public static Map<String,Integer> add(Map<String,Integer> first,Map<String,Integer> second){if((long)total(first)+total(second)>LIMIT)throw new IllegalArgumentException("Aspect capacity exceeded");var m=new TreeMap<>(first);second.forEach((k,v)->m.merge(k,v,Integer::sum));return Map.copyOf(m);}
    public static Map<String,Integer> subtract(Map<String,Integer> available,Map<String,Integer> required){if(!contains(available,required))throw new IllegalArgumentException("Missing aspects");var m=new TreeMap<>(available);required.forEach((k,v)->{int n=m.get(k)-v;if(n==0)m.remove(k);else m.put(k,n);});return Map.copyOf(m);}
    public static Map<String,Integer> crafting(Map<String,Integer> input,int count){if(count<1)throw new IllegalArgumentException("Invalid result count");var m=new TreeMap<String,Integer>();input.forEach((k,v)->{double n=v*.75/count;int amount=n>.75&&n<1?1:(int)n;if(amount>0)m.put(k,Math.min(500,amount));});return Map.copyOf(m);}
}
