package thaumcraft.condenser;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
public record CondenserState(int version,int fuel,int output,int progress,long converted){
 public static final CondenserState EMPTY=new CondenserState(1,0,0,0,0);
 public static final Codec<CondenserState> CODEC=RecordCodecBuilder.create(i->i.group(Codec.intRange(1,1).fieldOf("version").forGetter(CondenserState::version),Codec.intRange(0,100).fieldOf("fuel").forGetter(CondenserState::fuel),Codec.intRange(0,100).fieldOf("output").forGetter(CondenserState::output),Codec.intRange(0,600).fieldOf("progress").forGetter(CondenserState::progress),Codec.LONG.validate(n->n>=0?DataResult.success(n):DataResult.error(()->"Negative condensation total")).fieldOf("converted").forGetter(CondenserState::converted)).apply(i,CondenserState::new));
 public CondenserState accept(){if(fuel>=100)throw new IllegalStateException("Full fuel buffer");return new CondenserState(1,fuel+1,output,progress,converted);}
 public CondenserState extract(){if(output<=0)throw new IllegalStateException("Empty output");return new CondenserState(1,fuel,output-1,progress,converted);}
 public CondenserState advance(boolean eligible){return eligible?new CondenserState(1,fuel,output,Math.min(600,progress+1),converted):this;}
 public CondenserState complete(int cost,int interval){if(cost<1||fuel<cost||output>=100||interval<5||progress<interval)throw new IllegalStateException("Unpaid condensation");return new CondenserState(1,fuel-cost,output+1,0,converted==Long.MAX_VALUE?converted:converted+1);}
 public CondenserState retime(int interval){return interval>0&&progress>=interval?new CondenserState(1,fuel,output,interval-1,converted):this;}
}
