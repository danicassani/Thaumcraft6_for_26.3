package thaumcraft.research;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;

/** Immutable, bounded player knowledge. Raw points retain the original 16/32 progression. */
public record KnowledgeState(int version, Map<String,Integer> stages, Set<String> scans,
                             Set<String> flags, Set<String> crafts, Map<String,Integer> points) {
    public static final int VERSION=1, MAX_POINTS=1_000_000;
    public static final KnowledgeState EMPTY=new KnowledgeState(VERSION,Map.of(),Set.of(),Set.of(),Set.of(),Map.of());
    private static final Codec<String> KEY=Codec.STRING.validate(s->!s.isEmpty()&&s.length()<=192
        ?DataResult.success(s):DataResult.error(()->"Invalid knowledge key length"));
    private static Codec<Set<String>> setCodec(int maximum) {
        return KEY.listOf().validate(list->list.size()<=maximum&&new HashSet<>(list).size()==list.size()
            ?DataResult.success(list):DataResult.error(()->"Duplicate or excessive knowledge keys"))
            .xmap(Set::copyOf,set->set.stream().sorted().toList());
    }
    private static Codec<Map<String,Integer>> mapCodec(int maxEntries,int maximum) {
        return Codec.unboundedMap(KEY,Codec.intRange(0,maximum)).validate(map->map.size()<=maxEntries
            ?DataResult.success(map):DataResult.error(()->"Excessive knowledge map entries"));
    }
    public static final Codec<KnowledgeState> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.intRange(VERSION,VERSION).fieldOf("version").forGetter(KnowledgeState::version),
        mapCodec(1024,1024).fieldOf("stages").forGetter(KnowledgeState::stages),
        setCodec(4096).fieldOf("scans").forGetter(KnowledgeState::scans),
        setCodec(4096).fieldOf("flags").forGetter(KnowledgeState::flags),
        setCodec(4096).fieldOf("crafts").forGetter(KnowledgeState::crafts),
        mapCodec(64,MAX_POINTS).fieldOf("points").forGetter(KnowledgeState::points)
    ).apply(i,KnowledgeState::new));
    public KnowledgeState {
        if(version!=VERSION)throw new IllegalArgumentException("Unsupported knowledge format");
        stages=Map.copyOf(stages);scans=Set.copyOf(scans);flags=Set.copyOf(flags);crafts=Set.copyOf(crafts);points=Map.copyOf(points);
    }
    public KnowledgeState stage(String key,int value){var copy=new HashMap<>(stages);copy.put(key,value);return new KnowledgeState(version,copy,scans,flags,crafts,points);}
    public KnowledgeState flag(String key){var copy=new HashSet<>(flags);copy.add(key);return new KnowledgeState(version,stages,scans,copy,crafts,points);}
    public KnowledgeState craft(String key){var copy=new HashSet<>(crafts);copy.add(key);return new KnowledgeState(version,stages,scans,flags,copy,points);}
    public KnowledgeState scan(String key){var copy=new HashSet<>(scans);copy.add(key);return new KnowledgeState(version,stages,copy,flags,crafts,points);}
    public int raw(String kind,String category){return points.getOrDefault(kind+":"+category,0);}
    public KnowledgeState addPoints(String kind,String category,int amount){
        String key=kind+":"+category;long next=(long)raw(kind,category)+amount;
        if(next<0)throw new IllegalArgumentException("Insufficient knowledge");
        var copy=new HashMap<>(points);copy.put(key,(int)Math.min(MAX_POINTS,next));return new KnowledgeState(version,stages,scans,flags,crafts,copy);
    }
}
