package thaumcraft.thaumaturgy;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;

/** Immutable, bounded spell instructions. Costs and permissions are recomputed on the server. */
public record FocusProgram(String medium,List<Effect> effects) {
    public static final List<String> MEDIA=List.of("touch","bolt","projectile");
    public static final List<String> EFFECTS=List.of("fire","air","earth","frost","flux","heal","break","exchange","curse");
    public record Effect(String kind,int power,int duration) {
        public static final Codec<Effect> CODEC=RecordCodecBuilder.create(i->i.group(Codec.STRING.validate(s->EFFECTS.contains(s)?DataResult.success(s):DataResult.error(()->"Unknown focus effect")).fieldOf("kind").forGetter(Effect::kind),Codec.intRange(1,5).fieldOf("power").forGetter(Effect::power),Codec.intRange(1,5).fieldOf("duration").forGetter(Effect::duration)).apply(i,Effect::new));
        public Effect {if(!EFFECTS.contains(kind)||power<1||power>5||duration<1||duration>5)throw new IllegalArgumentException("Invalid focus effect");}
        public int complexity(){return switch(kind){case "fire","frost"->power*2+duration;case "air"->power*2;case "heal"->power*4;case "curse"->power*3+duration;case "exchange"->5+power;default->power*3;};}
        public String aspect(){return switch(kind){case "fire"->"ignis";case "frost"->"gelum";case "air"->"aer";case "earth"->"terra";case "flux"->"vitium";case "heal"->"victus";case "break"->"perditio";case "exchange"->"permutatio";default->"mortuus";};}
        public String research(){return switch(kind){case "fire"->"BASEAUROMANCY";case "air","earth","frost"->"FOCUSELEMENTAL";default->"FOCUS"+kind.toUpperCase(Locale.ROOT);};}
    }
    public static final Codec<FocusProgram> CODEC=RecordCodecBuilder.create(i->i.group(Codec.STRING.validate(s->MEDIA.contains(s)?DataResult.success(s):DataResult.error(()->"Unknown focus medium")).fieldOf("medium").forGetter(FocusProgram::medium),Effect.CODEC.listOf(1,8).fieldOf("effects").forGetter(FocusProgram::effects)).apply(i,FocusProgram::new));
    public FocusProgram {if(!MEDIA.contains(medium)||effects.isEmpty()||effects.size()>8)throw new IllegalArgumentException("Invalid focus program");effects=List.copyOf(effects);}
    public int complexity(){int c=switch(medium){case "touch"->2;case "bolt"->5;default->4;};var repeats=new HashMap<String,Integer>();for(var e:effects){int n=repeats.merge(e.kind,1,Integer::sum);c+=Math.round(e.complexity()*(n+1)*.5F);}return c;}
    public float vis(){return complexity()/5F;}
    public int cooldown(){int c=complexity();return Math.max(5,c/5*(c/4));}
    public int craftingVis(int capacity){return complexity()*10+capacity/5;}
    public int levels(){return Math.max(1,Math.round((float)Math.sqrt(complexity())));}
    public String mediumResearch(){return switch(medium){case "touch"->"BASEAUROMANCY";case "bolt"->"FOCUSBOLT";default->"FOCUSPROJECTILE@2";};}
    public Map<String,Integer> crystals(){var result=new TreeMap<String,Integer>();result.merge(switch(medium){case "touch"->"aversio";case "bolt"->"potentia";default->"motus";},1,Integer::sum);for(var e:effects)result.merge(e.aspect(),1,Integer::sum);return Map.copyOf(result);}
}
