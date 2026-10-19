package thaumcraft.thaumaturgy;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.world.entity.player.Player;
import thaumcraft.research.ResearchService;

/** Bounded spell graph. Missing advanced fields decode exactly as a port.14 program. */
public record FocusProgram(String medium,List<Effect> effects,String modifier,int forks,int cone,
                           int radius,int seconds,boolean friendly,List<Branch> branches,PlanSettings plan,ProjectileSettings projectile) {
    public static final List<String> MEDIA=List.of("touch","bolt","projectile","cloud","mine","plan","spellbat");
    public static final List<String> MODIFIERS=List.of("none","scatter","split_target","split_trajectory");
    public static final List<Integer> CONES=List.of(10,30,60,90,180,270,360);
    public static final List<String> EFFECTS=List.of("fire","air","earth","frost","flux","heal","break","exchange","curse","rift");
    private static Codec<String> named(List<String> names,String label){return Codec.STRING.validate(s->names.contains(s)?DataResult.success(s):DataResult.error(()->"Unknown focus "+label));}
    /** Speed 0 preserves the velocity of already programmed port.17 projectiles. */
    public record ProjectileSettings(int speed,int option) {
        public static final ProjectileSettings DEFAULT=new ProjectileSettings(0,0);
        public static final Codec<ProjectileSettings> CODEC=RecordCodecBuilder.create(i->i.group(Codec.intRange(0,5).fieldOf("speed").forGetter(ProjectileSettings::speed),Codec.intRange(0,3).fieldOf("option").forGetter(ProjectileSettings::option)).apply(i,ProjectileSettings::new));
        public boolean valid(){return speed>=0&&speed<=5&&option>=0&&option<=3;}
        public float velocity(){return speed==0?1.5F:speed/3F;}
        public int complexity(){return (speed==0?0:(speed-1)/2)+(option==1?3:option>=2?5:0);}
    }
    public record PlanSettings(int x,int y,int z,boolean surface) {
        public static final PlanSettings DEFAULT=new PlanSettings(3,3,3,false);
        public static final Codec<PlanSettings> CODEC=RecordCodecBuilder.create(i->i.group(Codec.intRange(0,3).fieldOf("x").forGetter(PlanSettings::x),Codec.intRange(0,3).fieldOf("y").forGetter(PlanSettings::y),Codec.intRange(0,3).fieldOf("z").forGetter(PlanSettings::z),Codec.BOOL.fieldOf("surface").forGetter(PlanSettings::surface)).apply(i,PlanSettings::new));
        public boolean valid(){return x>=0&&x<=3&&y>=0&&y<=3&&z>=0&&z<=3;}
    }
    public record Effect(String kind,int power,int duration,int depth,int fortune,boolean silk) {
        public static final Codec<Effect> CODEC=RecordCodecBuilder.<Effect>create(i->i.group(named(EFFECTS,"effect").fieldOf("kind").forGetter(Effect::kind),Codec.intRange(1,5).fieldOf("power").forGetter(Effect::power),Codec.intRange(1,10).fieldOf("duration").forGetter(Effect::duration),Codec.INT.validate(n->List.of(8,16,24,32).contains(n)?DataResult.success(n):DataResult.error(()->"Invalid rift depth")).optionalFieldOf("depth",8).forGetter(Effect::depth),Codec.intRange(0,4).optionalFieldOf("fortune",0).forGetter(Effect::fortune),Codec.BOOL.optionalFieldOf("silk",false).forGetter(Effect::silk)).apply(i,Effect::new)).validate(e->e.valid()?DataResult.success(e):DataResult.error(()->"Invalid focus effect settings"));
        public Effect(String kind,int power,int duration){this(kind,power,duration,8);}
        public Effect(String kind,int power,int duration,int depth){this(kind,power,duration,depth,0,false);}
        public boolean harvest(){return kind.equals("break")||kind.equals("exchange");}
        public boolean valid(){return fortune>=0&&fortune<=4&&(harvest()||fortune==0&&!silk)&&EFFECTS.contains(kind)&&power>=1&&power<=5&&List.of(8,16,24,32).contains(depth)&&duration>=(kind.equals("rift")?2:1)&&duration<=(kind.equals("rift")?10:5);}
        public int complexity(){return (harvest()? (silk?4:0)+(fortune==0?0:(fortune+1)*3):0)+switch(kind){case "fire","frost"->power*2+duration;case "air"->power*2;case "heal"->power*4;case "curse"->power*3+duration;case "exchange"->5+power;case "rift"->3+duration/2+depth/4;default->power*3;};}
        public String aspect(){return switch(kind){case "fire"->"ignis";case "frost"->"gelum";case "air"->"aer";case "earth"->"terra";case "flux"->"vitium";case "heal"->"victus";case "break"->"perditio";case "exchange"->"permutatio";case "rift"->"alienis";default->"mortuus";};}
        public String research(){return switch(kind){case "fire"->"BASEAUROMANCY";case "air","earth","frost"->"FOCUSELEMENTAL";default->"FOCUS"+kind.toUpperCase(Locale.ROOT);};}
    }
    public record Branch(String medium,List<Effect> effects,int radius,int seconds,boolean friendly,ProjectileSettings projectile){
        public static final Codec<Branch> CODEC=RecordCodecBuilder.create(i->i.group(named(MEDIA,"medium").fieldOf("medium").forGetter(Branch::medium),Effect.CODEC.listOf(1,8).fieldOf("effects").forGetter(Branch::effects),Codec.intRange(1,3).optionalFieldOf("radius",1).forGetter(Branch::radius),Codec.intRange(5,30).optionalFieldOf("seconds",5).forGetter(Branch::seconds),Codec.BOOL.optionalFieldOf("friendly",false).forGetter(Branch::friendly),ProjectileSettings.CODEC.optionalFieldOf("projectile",ProjectileSettings.DEFAULT).forGetter(Branch::projectile)).apply(i,Branch::new));
        public Branch {effects=List.copyOf(effects);}
        public Branch(String m,List<Effect> e){this(m,e,1,5,false);}
        public Branch(String m,List<Effect> e,int r,int s,boolean f){this(m,e,r,s,f,ProjectileSettings.DEFAULT);}
    }
    private static final Codec<FocusProgram> FORMAT=RecordCodecBuilder.create(i->i.group(
        named(MEDIA,"medium").fieldOf("medium").forGetter(FocusProgram::medium),Effect.CODEC.listOf(1,8).fieldOf("effects").forGetter(FocusProgram::effects),
        named(MODIFIERS,"modifier").optionalFieldOf("modifier","none").forGetter(FocusProgram::modifier),Codec.intRange(2,10).optionalFieldOf("forks",2).forGetter(FocusProgram::forks),Codec.INT.validate(n->CONES.contains(n)?DataResult.success(n):DataResult.error(()->"Invalid scatter cone")).optionalFieldOf("cone",10).forGetter(FocusProgram::cone),
        Codec.intRange(1,3).optionalFieldOf("radius",1).forGetter(FocusProgram::radius),Codec.intRange(5,30).optionalFieldOf("seconds",5).forGetter(FocusProgram::seconds),Codec.BOOL.optionalFieldOf("friendly",false).forGetter(FocusProgram::friendly),Branch.CODEC.listOf(0,1).optionalFieldOf("branches",List.of()).forGetter(FocusProgram::branches),PlanSettings.CODEC.optionalFieldOf("plan",PlanSettings.DEFAULT).forGetter(FocusProgram::plan),ProjectileSettings.CODEC.optionalFieldOf("projectile",ProjectileSettings.DEFAULT).forGetter(FocusProgram::projectile)).apply(i,FocusProgram::new));
    public static final Codec<FocusProgram> CODEC=FORMAT.validate(p->p.valid()?DataResult.success(p):DataResult.error(()->"Invalid or excessive focus graph"));
    public FocusProgram(String m,List<Effect> e,String mod,int forks,int cone,int radius,int seconds,boolean friendly,List<Branch> branches,PlanSettings plan){this(m,e,mod,forks,cone,radius,seconds,friendly,branches,plan,ProjectileSettings.DEFAULT);}
    public FocusProgram(String m,List<Effect> e,String mod,int forks,int cone,int radius,int seconds,boolean friendly,List<Branch> branches){this(m,e,mod,forks,cone,radius,seconds,friendly,branches,PlanSettings.DEFAULT);}
    public FocusProgram(String m,List<Effect> e){this(m,e,"none",2,10,1,5,false,List.of());}
    public FocusProgram {effects=List.copyOf(effects);branches=List.copyOf(branches);}
    public boolean split(){return modifier.equals("split_target")||modifier.equals("split_trajectory");}
    public boolean valid(){return projectile!=null&&projectile.valid()&&plan!=null&&plan.valid()&&allEffects().stream().allMatch(Effect::valid)&&(!medium.equals("plan")||(!modifier.equals("split_trajectory")&&!modifier.equals("scatter")))&&MEDIA.contains(medium)&&MODIFIERS.contains(modifier)&&forks>=2&&forks<=10&&CONES.contains(cone)&&radius>=1&&radius<=3&&seconds>=5&&seconds<=30&&!effects.isEmpty()&&totalEffects()<=8&&branches.size()==(split()?1:0)&&branches.stream().allMatch(b->b.projectile!=null&&b.projectile.valid()&&!b.medium.equals("plan")&&MEDIA.contains(b.medium)&&!b.effects.isEmpty()&&b.radius>=1&&b.radius<=3&&b.seconds>=5&&b.seconds<=30)&&(!modifier.equals("split_target")||branches.getFirst().medium.equals("touch"));}
    public int totalEffects(){return effects.size()+branches.stream().mapToInt(b->b.effects.size()).sum();}
    public Branch root(){return new Branch(medium,effects,radius,seconds,friendly,projectile);}
    public FocusProgram path(int index){var b=index==0?root():branches.get(index-1);return new FocusProgram(b.medium,b.effects,"none",2,10,b.radius,b.seconds,b.friendly,List.of(),index==0?plan:PlanSettings.DEFAULT,b.projectile);}
    public List<Effect> impactEffects(){if(!modifier.equals("split_target"))return effects;var all=new ArrayList<>(effects);for(var b:branches)all.addAll(b.effects);return List.copyOf(all);}
    public FocusProgram replace(int index,Branch b){if(index==0)return new FocusProgram(b.medium,b.effects,modifier,forks,cone,b.radius,b.seconds,b.friendly,branches,plan,b.projectile);return new FocusProgram(medium,effects,modifier,forks,cone,radius,seconds,friendly,List.of(b),plan,projectile);}
    public FocusProgram modifier(String m){var b=m.startsWith("split_")?(branches.isEmpty()?new Branch("touch",List.of(new Effect("fire",1,1))):branches.getFirst()):null;if(m.equals("split_target")&&b!=null)b=new Branch("touch",b.effects,b.radius,b.seconds,b.friendly,b.projectile);return new FocusProgram(medium,effects,m,forks,cone,radius,seconds,friendly,b==null?List.of():List.of(b),plan,projectile);}
    public static int mediumCost(Branch b){return switch(b.medium){case "touch"->2;case "bolt"->5;case "spellbat"->8;case "projectile"->4+b.projectile.complexity();case "cloud"->4+b.radius*2+b.seconds/5;default->4;};}
    public static String mediumAspect(String m){return switch(m){case "touch"->"aversio";case "bolt"->"potentia";case "cloud"->"alkimia";case "mine"->"vinculum";case "plan"->"fabrico";case "spellbat"->"bestia";default->"motus";};}
    public static String mediumResearch(String m){return switch(m){case "touch"->"BASEAUROMANCY";case "bolt"->"FOCUSBOLT";case "cloud"->"FOCUSCLOUD";case "mine"->"FOCUSMINE";case "plan"->"FOCUSPLAN";case "spellbat"->"FOCUSSPELLBAT";default->"FOCUSPROJECTILE@2";};}
    public int complexity(){int c=mediumCost(root());if(modifier.equals("split_trajectory"))for(var b:branches)c+=Math.round(mediumCost(b)*(b.medium.equals(medium)?1.5F:1));c+=switch(modifier){case "scatter"->(int)Math.max(2,2F*(forks-cone/45F));case "split_target"->4;case "split_trajectory"->5;default->0;};var repeats=new HashMap<String,Integer>();for(var e:allEffects()){int n=repeats.merge(e.kind,1,Integer::sum);c+=Math.round(e.complexity()*(n+1)*.5F);}return c;}
    private List<Effect> allEffects(){var result=new ArrayList<>(effects);for(var b:branches)result.addAll(b.effects);return result;}
    public float vis(){return complexity()/5F;}
    public int cooldown(){int c=complexity();return Math.max(5,c/5*(c/4));}
    public int craftingVis(int capacity){return complexity()*10+capacity/5;}
    public int levels(){return Math.max(1,Math.round((float)Math.sqrt(complexity())));}
    public String mediumResearch(){return mediumResearch(medium);}
    public boolean knows(Player p){return valid()&&ResearchService.knows(p,mediumResearch())&&projectileKnown(p,root())&&(modifier.equals("none")||ResearchService.knows(p,modifier.equals("scatter")?"FOCUSSCATTER":"FOCUSSPLIT"))&&allEffects().stream().allMatch(e->ResearchService.knows(p,e.research()))&&(!modifier.equals("split_trajectory")||branches.stream().allMatch(b->ResearchService.knows(p,mediumResearch(b.medium))&&projectileKnown(p,b)));}
    private static boolean projectileKnown(Player p,Branch b){return !b.medium.equals("projectile")||b.projectile.option==0||ResearchService.knows(p,"FOCUSPROJECTILE");}
    public Map<String,Integer> crystals(){var result=new TreeMap<String,Integer>();result.merge(mediumAspect(medium),1,Integer::sum);if(modifier.equals("split_trajectory"))for(var b:branches)result.merge(mediumAspect(b.medium),1,Integer::sum);for(var e:allEffects())result.merge(e.aspect(),1,Integer::sum);return Map.copyOf(result);}
}
