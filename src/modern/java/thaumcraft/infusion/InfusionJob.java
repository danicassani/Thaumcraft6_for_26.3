package thaumcraft.infusion;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import thaumcraft.alchemy.AspectAmounts;
public record InfusionJob(String recipe,String owner,ItemStack primary,ItemStack result,Map<String,Integer> remaining,List<Offering> offerings,int consumed,int instability,int total){
    private static final Codec<ItemStack> ONE=ItemStack.CODEC.validate(s->s.getCount()==1?DataResult.success(s):DataResult.error(()->"Infusion input must be one item"));
    public record Offering(int x,int y,int z,ItemStack item){
        public static final Codec<Offering> CODEC=RecordCodecBuilder.<Offering>create(i->i.group(Codec.intRange(-8,8).fieldOf("x").forGetter(Offering::x),Codec.intRange(-7,3).fieldOf("y").forGetter(Offering::y),Codec.intRange(-8,8).fieldOf("z").forGetter(Offering::z),ItemStack.CODEC.validate(s->s.getCount()==1?DataResult.success(s):DataResult.error(()->"Offering must be one item")).fieldOf("item").forGetter(Offering::item)).apply(i,Offering::new)).validate(o->o.x()==0&&o.z()==0?DataResult.error(()->"Central pedestal cannot be an offering"):DataResult.success(o));
        public BlockPos position(BlockPos matrix){return matrix.offset(x,y,z);}
    }
    private static final Codec<String> OWNER=Codec.STRING.validate(s->{try{UUID.fromString(s);return DataResult.success(s);}catch(Exception e){return DataResult.error(()->"Invalid infusion owner");}});
    public static final Codec<InfusionJob> CODEC=RecordCodecBuilder.<InfusionJob>create(i->i.group(Codec.string(1,256).fieldOf("recipe").forGetter(InfusionJob::recipe),OWNER.fieldOf("owner").forGetter(InfusionJob::owner),ONE.fieldOf("primary").forGetter(InfusionJob::primary),ONE.fieldOf("result").forGetter(InfusionJob::result),AspectAmounts.CODEC.fieldOf("remaining").forGetter(InfusionJob::remaining),Offering.CODEC.listOf(1,32).fieldOf("offerings").forGetter(InfusionJob::offerings),Codec.intRange(0,32).fieldOf("consumed").forGetter(InfusionJob::consumed),Codec.intRange(0,100).fieldOf("instability").forGetter(InfusionJob::instability),Codec.intRange(0,20000).fieldOf("total").forGetter(InfusionJob::total)).apply(i,InfusionJob::new)).validate(j->j.consumed()>j.offerings().size()||AspectAmounts.total(j.remaining())>j.total()||j.offerings().stream().map(o->List.of(o.x(),o.y(),o.z())).distinct().count()!=j.offerings().size()?DataResult.error(()->"Invalid infusion progress"):DataResult.success(j));
    public InfusionJob{primary=primary.copy();result=result.copy();remaining=Map.copyOf(remaining);offerings=List.copyOf(offerings);}
    public InfusionJob drain(String aspect){return new InfusionJob(recipe,owner,primary,result,AspectAmounts.subtract(remaining,Map.of(aspect,1)),offerings,consumed,instability,total);}
    public InfusionJob consume(){return new InfusionJob(recipe,owner,primary,result,remaining,offerings,consumed+1,instability,total);}
}
