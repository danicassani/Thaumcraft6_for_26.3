package thaumcraft.theory;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import thaumcraft.research.ResearchCatalog;

/** Server-owned session; persisted choices retain their random seed across reloads. */
public final class TheorySession {
    private static final Codec<String> CATEGORY=Codec.STRING.validate(s->ResearchCatalog.CATEGORIES.contains(s)?DataResult.success(s):DataResult.error(()->"Unknown theory category"));
    public static final Codec<TheorySession> CODEC=RecordCodecBuilder.<TheorySession>create(i->i.group(
        Codec.intRange(1,1).fieldOf("version").forGetter(s->1),
        Codec.STRING.validate(s->{try{UUID.fromString(s);return DataResult.success(s);}catch(Exception e){return DataResult.error(()->"Invalid theory owner");}}).fieldOf("owner").forGetter(s->s.owner),
        Codec.intRange(0,15).fieldOf("inspiration").forGetter(s->s.inspiration),
        Codec.intRange(1,15).fieldOf("maximum").forGetter(s->s.maximum),
        Codec.intRange(0,10000).fieldOf("bonus").forGetter(s->s.bonus),
        Codec.intRange(0,10000).fieldOf("penalty").forGetter(s->s.penalty),
        Codec.intRange(0,10000).fieldOf("played").forGetter(s->s.played),
        Codec.unboundedMap(CATEGORY,Codec.intRange(1,100000)).fieldOf("totals").forGetter(s->s.totals),
        CATEGORY.listOf().validate(l->new HashSet<>(l).size()==l.size()?DataResult.success(l):DataResult.error(()->"Duplicate blocked category")).fieldOf("blocked").forGetter(s->new ArrayList<>(s.blocked)),
        TheoryCards.Card.CODEC.listOf().validate(l->l.size()<=3?DataResult.success(l):DataResult.error(()->"Too many choices")).fieldOf("choices").forGetter(s->s.choices),
        Codec.STRING.listOf().validate(l->l.size()<=32&&l.stream().allMatch(TheoryCards::validAidCard)?DataResult.success(l):DataResult.error(()->"Unknown aids")).fieldOf("aids").forGetter(s->s.aids)
    ).apply(i,TheorySession::new)).validate(s->s.inspiration<=s.maximum?DataResult.success(s):DataResult.error(()->"Excess inspiration"));
    public final String owner;
    public int inspiration,maximum,bonus,penalty,played;
    public final Map<String,Integer> totals;
    public final Set<String> blocked;
    public List<TheoryCards.Card> choices;
    public final List<String> aids;
    private TheorySession(int version,String owner,int inspiration,int maximum,int bonus,int penalty,int played,Map<String,Integer> totals,List<String> blocked,List<TheoryCards.Card> choices,List<String> aids){
        this.owner=owner;this.inspiration=inspiration;this.maximum=maximum;this.bonus=bonus;this.penalty=penalty;this.played=played;
        this.totals=new TreeMap<>(totals);this.blocked=new HashSet<>(blocked);this.choices=List.copyOf(choices);this.aids=new ArrayList<>(aids);
    }
    public static TheorySession start(UUID owner,int maximum,List<String> aids){return new TheorySession(1,owner.toString(),maximum-aids.size(),maximum,0,0,0,Map.of(),List.of(),List.of(),aids.stream().flatMap(k->TheoryCards.AIDS.get(k).stream().map(v->k+"#"+v.name())).toList());}
    public TheorySession copy(){return new TheorySession(1,owner,inspiration,maximum,bonus,penalty,played,totals,new ArrayList<>(blocked),choices,aids);}
    public int total(String c){return totals.getOrDefault(c,0);}
    public void add(String c,int amount){int n=total(c)+amount;if(n<=0)totals.remove(c);else totals.put(c,Math.min(100000,n));}
    public void inspire(int amount){inspiration=Math.max(0,Math.min(maximum,inspiration+amount));}
    public Map<String,Integer> rewards(){
        var entries=new ArrayList<>(totals.entrySet());entries.sort(Map.Entry.<String,Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()));
        var result=new LinkedHashMap<String,Integer>();int rank=0;
        for(var e:entries){int raw=Math.round(e.getValue()/100F*32);if(rank++>penalty)raw=Math.max(1,(int)(raw*0.666666667));if(raw>0)result.put(e.getKey(),raw);}
        return result;
    }
}
