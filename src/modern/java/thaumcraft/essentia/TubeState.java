package thaumcraft.essentia;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import thaumcraft.alchemy.AspectAmounts;

public record TubeState(int version,Map<String,Integer> aspects,int open,List<Integer> chokes,String filter,boolean flow,boolean powered,int vent){
    public static final Codec<String> FILTER=Codec.STRING.validate(s->s.isEmpty()||thaumcraft.crystals.CrystalAspects.COLORS.containsKey(s)?DataResult.success(s):DataResult.error(()->"Unknown essentia filter"));
    private static final Codec<List<Integer>> CHOKES=Codec.intRange(0,2).listOf().validate(s->s.size()==6?DataResult.success(s):DataResult.error(()->"Expected six choke settings"));
    public static final Codec<TubeState> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.intRange(1,1).fieldOf("version").forGetter(TubeState::version),
        AspectAmounts.CODEC.fieldOf("aspects").forGetter(TubeState::aspects),
        Codec.intRange(0,63).fieldOf("open").forGetter(TubeState::open),
        CHOKES.fieldOf("chokes").forGetter(TubeState::chokes),
        FILTER.optionalFieldOf("filter","").forGetter(TubeState::filter),
        Codec.BOOL.fieldOf("flow").forGetter(TubeState::flow),
        Codec.BOOL.fieldOf("powered").forGetter(TubeState::powered),
        Codec.intRange(0,40).fieldOf("vent").forGetter(TubeState::vent)
    ).apply(i,TubeState::new));
    public static final TubeState EMPTY=new TubeState(1,Map.of(),63,List.of(0,0,0,0,0,0),"",true,false,0);
    public TubeState{aspects=Map.copyOf(aspects);chokes=List.copyOf(chokes);}
}
