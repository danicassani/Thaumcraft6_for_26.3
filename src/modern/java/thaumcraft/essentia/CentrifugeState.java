package thaumcraft.essentia;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import thaumcraft.research.AspectCatalog;
/** One paid input, one output, and the exact remaining processing time. */
public record CentrifugeState(int version,String input,String output,int remaining){
 public static final CentrifugeState EMPTY=new CentrifugeState(1,"","",0);
 public static boolean compound(String aspect){return AspectCatalog.components(aspect).size()==2;}
 public static final Codec<CentrifugeState> CODEC=RecordCodecBuilder.<CentrifugeState>create(i->i.group(
  Codec.intRange(1,1).fieldOf("version").forGetter(CentrifugeState::version),
  TubeState.FILTER.fieldOf("input").forGetter(CentrifugeState::input),
  TubeState.FILTER.fieldOf("output").forGetter(CentrifugeState::output),
  Codec.intRange(0,39).fieldOf("remaining").forGetter(CentrifugeState::remaining)
 ).apply(i,CentrifugeState::new)).validate(s->(s.input().isEmpty()?s.remaining()==0:compound(s.input()))?DataResult.success(s):DataResult.error(()->"Invalid centrifuge input or progress"));
 public CentrifugeState accept(String aspect){return new CentrifugeState(1,aspect,output,39);}
 public CentrifugeState advance(boolean powered,int choice){if(powered||input.isEmpty())return this;int left=Math.max(0,remaining-1);return left==0&&output.isEmpty()?new CentrifugeState(1,"",AspectCatalog.components(input).get(Math.floorMod(choice,2)),0):new CentrifugeState(1,input,output,left);}
 public CentrifugeState drain(){return new CentrifugeState(1,input,"",remaining);}
}
