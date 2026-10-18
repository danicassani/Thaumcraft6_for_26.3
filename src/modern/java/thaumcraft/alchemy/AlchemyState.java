package thaumcraft.alchemy;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
public record AlchemyState(int version,Map<String,Integer> aspects,int water,int heat,int burn,int burnTotal,int cook,int decay){
    public static final AlchemyState EMPTY=new AlchemyState(1,Map.of(),0,0,0,0,0,-100);
    public static final Codec<AlchemyState> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.intRange(1,1).fieldOf("version").forGetter(AlchemyState::version),AspectAmounts.CODEC.fieldOf("aspects").forGetter(AlchemyState::aspects),
        Codec.intRange(0,1000).fieldOf("water").forGetter(AlchemyState::water),Codec.intRange(0,200).fieldOf("heat").forGetter(AlchemyState::heat),
        Codec.intRange(0,1000000).fieldOf("burn").forGetter(AlchemyState::burn),Codec.intRange(0,1000000).fieldOf("burn_total").forGetter(AlchemyState::burnTotal),
        Codec.intRange(0,100000).fieldOf("cook").forGetter(AlchemyState::cook),Codec.intRange(-250,100).fieldOf("decay").forGetter(AlchemyState::decay)
    ).apply(i,AlchemyState::new));
    public AlchemyState{aspects=Map.copyOf(aspects);}
}
