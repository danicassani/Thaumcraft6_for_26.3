package thaumcraft.essentia;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
/** One-unit escrow protects a committed extraction when the receiver changes. */
public record TransfuserState(int version,int phase,String pending,long transferred){
 public static final TransfuserState EMPTY=new TransfuserState(1,0,"",0);
 public static final Codec<TransfuserState> CODEC=RecordCodecBuilder.create(i->i.group(Codec.intRange(1,1).fieldOf("version").forGetter(TransfuserState::version),Codec.intRange(0,4).fieldOf("phase").forGetter(TransfuserState::phase),TubeState.FILTER.fieldOf("pending").forGetter(TransfuserState::pending),Codec.LONG.validate(v->v>=0?DataResult.success(v):DataResult.error(()->"Negative transfer count")).fieldOf("transferred").forGetter(TransfuserState::transferred)).apply(i,TransfuserState::new));
 public TransfuserState tick(){return new TransfuserState(1,(phase+1)%5,pending,transferred);}
 public TransfuserState reserve(String aspect){return new TransfuserState(1,phase,aspect,transferred);}
 public TransfuserState delivered(){return new TransfuserState(1,phase,"",transferred==Long.MAX_VALUE?transferred:transferred+1);}
}
