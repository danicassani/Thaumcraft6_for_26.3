package thaumcraft.infusion;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
public record InfusionState(int version,float stability,int countdown,Optional<InfusionJob> job){
    public static final Codec<InfusionState> CODEC=RecordCodecBuilder.create(i->i.group(Codec.intRange(1,1).fieldOf("version").forGetter(InfusionState::version),Codec.floatRange(-100,25).fieldOf("stability").forGetter(InfusionState::stability),Codec.intRange(0,5).fieldOf("countdown").forGetter(InfusionState::countdown),InfusionJob.CODEC.optionalFieldOf("job").forGetter(InfusionState::job)).apply(i,InfusionState::new));
}
