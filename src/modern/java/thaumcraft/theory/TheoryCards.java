package thaumcraft.theory;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import thaumcraft.crystals.CrystalContent;
import thaumcraft.research.*;

/** Cards whose dependencies already exist in this port. No placeholder items in the draw pool. */
public final class TheoryCards {
    public enum Kind { EXPERIMENTATION, ANALYZE, BALANCE, PONDER, RETHINK, REJECT, INSPIRED,
        CALIBRATE, FOCUS, MEASURE, SCRIPTING, CONCENTRATE, REACTIONS, SYNTHESIS,
        TINKER, MINDMATTER, CELESTIAL, SPELLBINDING, AWARENESS, REALIZATION, REVELATION,
        TRUTH, SYNERGY, ENCHANTMENT, BEACON, GLYPHS, PORTAL }
    public static final List<String> ASPECTS=List.copyOf(thaumcraft.crystals.CrystalAspects.COLORS.keySet());
    private static final List<String> MACHINES=List.of("thaumcraft:vis_resonator","thaumcraft:thaumometer","minecraft:anvil","minecraft:activator_rail","minecraft:dispenser","minecraft:dropper","minecraft:enchanting_table","minecraft:ender_chest","minecraft:jukebox","minecraft:daylight_detector","minecraft:piston","minecraft:hopper","minecraft:sticky_piston","minecraft:map","minecraft:compass","minecraft:tnt_minecart","minecraft:comparator","minecraft:clock");
    public static final Map<String,List<Kind>> AIDS=Map.of(
        "thaumcraft:arcane_workbench",List.of(Kind.CALIBRATE,Kind.TINKER,Kind.MINDMATTER),
        "minecraft:enchanting_table",List.of(Kind.ENCHANTMENT),"minecraft:beacon",List.of(Kind.BEACON),
        "thaumcraft:stone_ancient_glyphed",List.of(Kind.GLYPHS),"minecraft:nether_portal",List.of(Kind.PORTAL),"minecraft:end_portal",List.of(Kind.PORTAL));
    public static final List<String> AID_KEYS=AIDS.keySet().stream().sorted().toList();
    public record Card(Kind kind,int category,int a,int b,int amount,long seed,boolean aid){
        public static final Codec<Card> CODEC=RecordCodecBuilder.create(i->i.group(
            Codec.STRING.comapFlatMap(s->{try{return DataResult.success(Kind.valueOf(s));}catch(Exception e){return DataResult.error(()->"Unknown theory card");}},Kind::name).fieldOf("kind").forGetter(Card::kind),
            Codec.intRange(0,6).fieldOf("category").forGetter(Card::category),Codec.intRange(0,36).fieldOf("a").forGetter(Card::a),Codec.intRange(0,36).fieldOf("b").forGetter(Card::b),
            Codec.intRange(0,100000).fieldOf("amount").forGetter(Card::amount),Codec.LONG.fieldOf("seed").forGetter(Card::seed),Codec.BOOL.fieldOf("aid").forGetter(Card::aid)
        ).apply(i,Card::new));
        public String cat(){return ResearchCatalog.CATEGORIES.get(category);}
        public int cost(){return switch(kind){case EXPERIMENTATION,ANALYZE,PONDER,INSPIRED->2;case REJECT->0;case RETHINK,PORTAL->-1;case BEACON->-2;default->1;};}
        public List<Need> needs(){return switch(kind){
            case CONCENTRATE->List.of(new Need(CrystalContent.essence(ASPECTS.get(a),1),false));
            case REACTIONS->List.of(new Need(CrystalContent.essence(ASPECTS.get(a),1),false),new Need(CrystalContent.essence(ASPECTS.get(b),1),false));
            case SYNTHESIS->{var cs=AspectCatalog.components(ASPECTS.get(a));yield cs.size()==2?List.of(new Need(CrystalContent.essence(cs.get(0),1),true),new Need(CrystalContent.essence(cs.get(1),1),true)):List.of();}
            case TINKER,MINDMATTER->List.of(new Need(new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(MACHINES.get(a%MACHINES.size())))),kind==Kind.MINDMATTER));
            case CELESTIAL->List.of(new Need(TheoryContent.note(a%13),true),new Need(TheoryContent.note(b%13),true));default->List.of();};}
    }
    public record Need(ItemStack stack,boolean consume){}
    private TheoryCards(){}
    public static boolean validAidCard(String s){int pos=s.indexOf('#');if(pos<0)return false;var kinds=AIDS.get(s.substring(0,pos));return kinds!=null&&kinds.stream().anyMatch(k->k.name().equals(s.substring(pos+1)));}
    public static List<String> available(Player p,TheorySession s){return ResearchCatalog.CATEGORIES.stream().filter(c->!s.blocked.contains(c)&&(c.equals("BASICS")||ResearchService.knows(p,"UNLOCK"+(c.equals("AUROMANCY")?"AUROMANCY":c)))).toList();}
    private static String fixed(Kind k){return switch(k){case CALIBRATE,TINKER,MINDMATTER->"ARTIFICE";case FOCUS,SPELLBINDING,AWARENESS->"AUROMANCY";case MEASURE->"INFUSION";case SCRIPTING,SYNERGY->"GOLEMANCY";case CONCENTRATE,REACTIONS,SYNTHESIS->"ALCHEMY";case REALIZATION,REVELATION,TRUTH,GLYPHS,PORTAL->"ELDRITCH";default->"";};}
    public static Card generate(Kind kind,Player player,TheorySession session,long seed,boolean aid){
        var random=new Random(seed);var cats=available(player,session);String fixed=fixed(kind);String cat=fixed.isEmpty()?"BASICS":fixed;int a=0,b=0,amount=0;
        if(!aid&&!fixed.isEmpty()&&!cats.contains(cat))return null;
        switch(kind){
            case ANALYZE->{var state=ResearchService.state(player);var choices=cats.stream().filter(c->!c.equals("BASICS")&&state.raw("OBSERVATION",c)>=16).toList();if(choices.isEmpty())return null;cat=choices.get(random.nextInt(choices.size()));}
            case BALANCE->{if(session.totals.keySet().stream().filter(c->!session.blocked.contains(c)).count()<2)return null;}
            case PONDER->{if(session.totals.keySet().stream().noneMatch(c->!session.blocked.contains(c)))return null;}
            case RETHINK->{if(session.totals.values().stream().mapToInt(Integer::intValue).sum()<10)return null;}
            case REJECT->{var choices=session.totals.keySet().stream().filter(c->!session.blocked.contains(c)).toList();if(choices.isEmpty())return null;cat=choices.get(random.nextInt(choices.size()));}
            case INSPIRED,CELESTIAL->{if(session.totals.isEmpty()||kind==Kind.CELESTIAL&&!ResearchService.knows(player,"CELESTIALSCANNING"))return null;cat=session.totals.entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey())).findFirst().get().getKey();amount=10+session.total(cat)/2;if(kind==Kind.CELESTIAL){a=random.nextInt(13);do{b=random.nextInt(13);}while(a==b);}}
            case CONCENTRATE,REACTIONS,SYNTHESIS->{var compounds=ASPECTS.stream().filter(x->AspectCatalog.components(x).size()==2).toList();a=ASPECTS.indexOf(compounds.get(random.nextInt(compounds.size())));b=a;if(kind==Kind.REACTIONS)while(b==a)b=ASPECTS.indexOf(compounds.get(random.nextInt(compounds.size())));}
            case TINKER,MINDMATTER->{a=random.nextInt(MACHINES.size());var stack=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(MACHINES.get(a))));amount=(int)Math.sqrt(AspectCatalog.item(stack).values().stream().mapToInt(Integer::intValue).sum());}
            case SPELLBINDING->{if(player.experienceLevel<=0)return null;}
            case SYNERGY->{if(session.total("ARTIFICE")+session.total("ALCHEMY")+session.total("INFUSION")<15)return null;}
            case ENCHANTMENT,BEACON,GLYPHS,PORTAL->{if(!aid)return null;}
            default->{}
        }
        if((!fixed.isEmpty()||kind==Kind.ANALYZE||kind==Kind.INSPIRED||kind==Kind.CELESTIAL)&&session.blocked.contains(cat))return null;
        var card=new Card(kind,ResearchCatalog.CATEGORIES.indexOf(cat),a,b,amount,seed,aid);return card.cost()<=session.inspiration?card:null;
    }
    public static List<Card> draw(Player p,TheorySession s,int count,long seed){
        var random=new Random(seed);var out=new ArrayList<Card>();var used=EnumSet.noneOf(Kind.class);
        for(int tries=0;tries<512&&out.size()<count;tries++){
            boolean aid=!s.aids.isEmpty()&&random.nextFloat()<=.25F;Kind kind;String aidCard=null;
            if(aid){aidCard=s.aids.get(random.nextInt(s.aids.size()));kind=Kind.valueOf(aidCard.substring(aidCard.indexOf('#')+1));}
            else kind=Kind.values()[random.nextInt(Kind.values().length)];
            if(used.contains(kind))continue;var c=generate(kind,p,s,random.nextLong(),aid);if(c==null)continue;used.add(kind);out.add(c);
            // Aid cards are a finite bonus for the session, as in TC6.
            if(aid)s.aids.remove(aidCard);
        }
        return List.copyOf(out);
    }
    public record Effect(KnowledgeState knowledge,int normalWarp,int temporaryWarp,int levels,ItemStack reward){}
    /** Changes only the proposed session. Caller validates and commits all costs atomically. */
    public static Effect apply(Card card,TheorySession s,Player player,KnowledgeState knowledge){
        var r=new Random(card.seed());String c=card.cat();int normal=0,temp=0,levels=0;ItemStack reward=ItemStack.EMPTY;
        switch(card.kind()){
            case EXPERIMENTATION->{s.add(ResearchCatalog.CATEGORIES.get(r.nextInt(7)),15+r.nextInt(16));s.add("BASICS",1+r.nextInt(10));}
            case ANALYZE->{if(knowledge.raw("OBSERVATION",c)<16)return null;knowledge=knowledge.addPoints("OBSERVATION",c,-16);s.add("BASICS",5);s.add(c,25+r.nextInt(26));}
            case BALANCE->{var keys=s.totals.keySet().stream().filter(k->!s.blocked.contains(k)).toList();if(keys.size()<2)return null;int avg=keys.stream().mapToInt(s::total).sum()/keys.size();keys.forEach(k->s.totals.put(k,avg));s.add("BASICS",5);s.penalty++;}
            case PONDER->{var keys=s.totals.keySet().stream().filter(k->!s.blocked.contains(k)).toList();if(keys.isEmpty())return null;for(int n=0;n<25;n++)s.add(keys.get(n%keys.size()),1);s.add("BASICS",5);s.bonus++;}
            case RETHINK->{if(s.totals.values().stream().mapToInt(Integer::intValue).sum()<10)return null;for(int n=0;n<10;n++){var keys=new ArrayList<>(s.totals.keySet());s.add(keys.get(n%keys.size()),-1);}s.add("BASICS",1+r.nextInt(10));s.bonus++;}
            case REJECT->{if(s.blocked.contains(c)||s.total(c)<=0)return null;s.add("BASICS",5);s.blocked.add(c);}
            case INSPIRED->s.add(c,card.amount());
            case CALIBRATE,FOCUS,MEASURE->{s.add(c,15);s.bonus++;}
            case SCRIPTING->s.add(c,25);
            case CONCENTRATE->{s.add(c,15);s.bonus++;if(r.nextFloat()<.33)s.inspire(1);}
            case REACTIONS->{s.add(c,25);if(r.nextFloat()<.33)s.inspire(1);}
            case SYNTHESIS->{s.add(c,40);if(r.nextFloat()<.33)s.inspire(1);reward=CrystalContent.essence(ASPECTS.get(card.a()),1);}
            case TINKER->s.add(c,card.amount()*2+r.nextInt(11));
            case MINDMATTER->s.add(c,10+card.amount());
            case SPELLBINDING->{if(player.experienceLevel<=0)return null;levels=Math.min(5,player.experienceLevel);s.add(c,levels*5);}
            case AWARENESS->{s.add(c,20);if(r.nextFloat()<.33){s.add("ELDRITCH",1+r.nextInt(5));normal=1;}}
            case REALIZATION,REVELATION->{s.add(ResearchCatalog.CATEGORIES.get(r.nextInt(7)),5+r.nextInt(6));if(card.kind()==Kind.REALIZATION)s.add(ResearchCatalog.CATEGORIES.get(r.nextInt(7)),5+r.nextInt(6));s.add("ELDRITCH",card.kind()==Kind.REVELATION?30:15);temp=5;normal=card.kind()==Kind.REVELATION||r.nextBoolean()?1:0;if(card.kind()==Kind.REVELATION)s.penalty++;}
            case TRUTH->{s.add("ELDRITCH",10+r.nextInt(16));s.bonus++;temp=3;}
            case SYNERGY->{var keys=List.of("ARTIFICE","ALCHEMY","INFUSION");if(keys.stream().mapToInt(s::total).sum()<15)return null;for(int n=0;n<15;){for(var key:keys)if(s.total(key)>0){s.add(key,-1);if(++n==15)break;}}s.add("GOLEMANCY",30);s.penalty++;}
            case CELESTIAL->{s.add(c,25+r.nextInt(26));boolean sun=card.a()==0||card.b()==0,moon=card.a()>4||card.b()>4,stars=card.a()>0&&card.a()<5||card.b()>0&&card.b()<5;if(stars){temp=r.nextInt(6);s.add("ELDRITCH",temp*2);}if(sun)s.penalty++;if(moon)s.bonus++;}
            case ENCHANTMENT->{if(player.experienceLevel<5)return null;levels=5;s.add("INFUSION",15+r.nextInt(6));s.add("AUROMANCY",15+r.nextInt(6));}
            case BEACON->{s.bonus++;s.penalty++;}
            case GLYPHS->{s.add(ResearchCatalog.CATEGORIES.get(r.nextInt(7)),10+r.nextInt(11));s.add("ELDRITCH",10+r.nextInt(11));temp=5;}
            case PORTAL->{for(int n=0;n<2;n++)s.add(ResearchCatalog.CATEGORIES.get(r.nextInt(7)),5+r.nextInt(6));s.add("ELDRITCH",5+r.nextInt(6));s.bonus+=2;temp=5;normal=1;}
        }
        s.inspire(-card.cost());s.played++;s.choices=List.of();return new Effect(knowledge,normal,temp,levels,reward);
    }
}
