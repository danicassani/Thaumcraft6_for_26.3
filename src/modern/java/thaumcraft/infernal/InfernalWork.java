package thaumcraft.infernal;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
/** Exact timer, selected physical input, prepaid aura credits and earned, unreleased XP. */
public record InfernalWork(int version,int remaining,int maximum,int slot,int speed,int xp){
 public static final InfernalWork EMPTY=new InfernalWork(1,0,0,-1,0,0);
 public static final Codec<InfernalWork> CODEC=RecordCodecBuilder.<InfernalWork>create(i->i.group(Codec.intRange(1,1).fieldOf("version").forGetter(InfernalWork::version),Codec.intRange(0,140).fieldOf("remaining").forGetter(InfernalWork::remaining),Codec.intRange(0,140).fieldOf("maximum").forGetter(InfernalWork::maximum),Codec.intRange(-1,31).fieldOf("slot").forGetter(InfernalWork::slot),Codec.intRange(0,20).fieldOf("speed").forGetter(InfernalWork::speed),Codec.intRange(0,1000000).fieldOf("xp").forGetter(InfernalWork::xp)).apply(i,InfernalWork::new)).validate(s->(s.slot<0?s.remaining==0&&s.maximum==0:s.maximum>0&&s.remaining<=s.maximum)?DataResult.success(s):DataResult.error(()->"Inconsistent infernal work"));
}
