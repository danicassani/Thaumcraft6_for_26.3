package thaumcraft.golems;
import java.util.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import thaumcraft.content.SimpleContent;
import thaumcraft.research.ResearchService;
/** Bounded supported designs. Opposing traits cancel in part order; legs default to the original walker when absent in old saves. */
public record GolemDesign(int material,int head,int arms,int addon,int legs) {
    public static final List<String> MATERIALS=List.of("wood","iron","clay","brass","thaumium","void");
    public static final List<String> HEADS=List.of("basic","smart","smart_armored");
    public static final List<String> ARMS=List.of("basic","fine","claws","breakers");
    public static final List<String> ADDONS=List.of("none","armored","fighter","hauler");
    public static final List<String> LEGS=List.of("walker","roller","climber","flyer");
    public GolemDesign(int material,int head,int arms,int addon){this(material,head,arms,addon,0);}
    public static final GolemDesign BASIC=new GolemDesign(0,0,0,0);
    public static final Codec<GolemDesign> CODEC=RecordCodecBuilder.create(i->i.group(Codec.intRange(0,5).fieldOf("material").forGetter(GolemDesign::material),Codec.intRange(0,2).fieldOf("head").forGetter(GolemDesign::head),Codec.intRange(0,3).fieldOf("arms").forGetter(GolemDesign::arms),Codec.intRange(0,3).fieldOf("addon").forGetter(GolemDesign::addon),Codec.intRange(0,3).optionalFieldOf("legs",0).forGetter(GolemDesign::legs)).apply(i,GolemDesign::new));
    public GolemDesign{if(material<0||material>5||head<0||head>2||arms<0||arms>3||addon<0||addon>3||legs<0||legs>3)throw new IllegalArgumentException("Invalid golem design");}
    private static void trait(Set<String> set,String t){String opposite=switch(t){case "light"->"heavy";case "heavy"->"light";case "fragile"->"armored";case "armored"->"fragile";case "deft"->"clumsy";case "clumsy"->"deft";default->"";};if(!set.remove(opposite))set.add(t);}
    public Set<String> traits(){var t=new LinkedHashSet<String>();for(var s:switch(material){case 0,3->List.of("light");case 1,4->List.of("heavy","fireproof","blastproof");case 2->List.of("fireproof");default->List.of("repair");})trait(t,s);if(head>0){trait(t,"smart");if(head==1)trait(t,"fragile");}for(var s:switch(arms){case 1->List.of("deft","fragile");case 2->List.of("fighter","clumsy","brutal");case 3->List.of("breaker","clumsy","brutal");default->List.<String>of();})trait(t,s);for(var s:switch(legs){case 1->List.of("wheeled");case 2->List.of("climber");case 3->List.of("flyer","fragile");default->List.<String>of();})trait(t,s);for(var s:switch(addon){case 1->List.of("armored","heavy");case 2->List.of("fighter");case 3->List.of("hauler");default->List.<String>of();})trait(t,s);return Collections.unmodifiableSet(t);}
    public boolean has(String trait){return traits().contains(trait);}
    public List<String> research(){var out=new ArrayList<>(List.of("MINDCLOCKWORK","MATSTUD"+MATERIALS.get(material).toUpperCase(Locale.ROOT)));if(head>0)out.add("MINDBIOTHAUMIC");if(head==2||arms==2||addon==1)out.add("GOLEMCOMBATADV");if(arms==1)out.add("MATSTUDBRASS");if(arms==3)out.add("GOLEMBREAKER");if(addon==2)out.add("SEALGUARD");if(legs==2)out.add("GOLEMCLIMBER");if(legs==3)out.add("GOLEMFLYER");return out;}
    public boolean knows(Player p){return research().stream().allMatch(k->ResearchService.knows(p,k));}
    public Map<String,Integer> components(){String base=switch(material){case 0->"thaumcraft:plank_greatwood";case 2->"minecraft:terracotta";default->"thaumcraft:plate_"+MATERIALS.get(material);};var c=new LinkedHashMap<String,Integer>();c.put(base,legs==1||legs==3?2:3);c.put("thaumcraft:mechanism_simple",legs==2?3:2);if(legs==1){c.put("minecraft:bowl",2);c.put("minecraft:leather",1);}if(legs==2)c.put("minecraft:flint",4);if(legs==3){c.put("thaumcraft:levitator",1);c.merge("thaumcraft:plate_brass",4,Integer::sum);c.put("minecraft:slime_ball",1);}c.put("thaumcraft:mind_"+(head==0?"clockwork":"biothaumic"),1);if(head==2){c.merge("thaumcraft:plate_brass",1,Integer::sum);c.put("minecraft:white_wool",1);c.merge(base,1,Integer::sum);}if(arms==1){c.merge(base,1,Integer::sum);c.merge("thaumcraft:mechanism_simple",1,Integer::sum);}if(arms==2){c.put("thaumcraft:module_aggression",1);c.put("minecraft:shears",2);c.merge(base,1,Integer::sum);}if(arms==3){c.put("minecraft:diamond",2);c.put("minecraft:piston",2);c.merge(base,1,Integer::sum);}if(addon==1)c.merge(base,4,Integer::sum);if(addon==2){c.put("thaumcraft:module_aggression",1);c.merge("thaumcraft:mechanism_simple",1,Integer::sum);}if(addon==3){c.merge("minecraft:leather",1,Integer::sum);c.put("minecraft:chest",1);}return Collections.unmodifiableMap(c);}
    public int cost(){return components().values().stream().mapToInt(Integer::intValue).sum()+traits().size()*2;}
    public double health(){return new int[]{6,20,10,16,24,20}[material];}
    public double armor(){return Math.max(0,new int[]{2,8,4,6,10,6}[material]+(has("armored")?4:0)-(has("fragile")?2:0));}
    public double speed(){double base=has("light")?.32:has("heavy")?.23:.27;return base*(legs==1?1.25:legs==3?.67:1);}
    public float damage(){return new int[]{1,3,2,3,4,4}[material]+(has("brutal")?2:0);}
    public int capacity(){return has("hauler")?2:1;}
    public GolemDesign cycle(int part){return switch(part){case 0->new GolemDesign((material+1)%6,head,arms,addon,legs);case 1->new GolemDesign(material,(head+1)%3,arms,addon,legs);case 2->new GolemDesign(material,head,(arms+1)%4,addon,legs);case 3->new GolemDesign(material,head,arms,(addon+1)%4,legs);case 5->new GolemDesign(material,head,arms,addon,(legs+1)%4);default->this;};}
}
