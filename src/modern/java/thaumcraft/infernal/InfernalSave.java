package thaumcraft.infernal;
import java.util.*;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
public record InfernalSave(InfernalWork work,List<ItemStack> input,List<ItemStack> output){
 private static final Codec<List<ItemStack>> INPUT=ItemStack.OPTIONAL_CODEC.listOf().validate(v->v.size()==32&&v.stream().allMatch(s->s.getCount()<=64)?DataResult.success(v):DataResult.error(()->"Invalid infernal inventory"));
 private static final Codec<List<ItemStack>> OUTPUT=ItemStack.CODEC.listOf().validate(v->v.size()<=64&&v.stream().allMatch(s->s.getCount()<=64)?DataResult.success(v):DataResult.error(()->"Invalid infernal output"));
 public static final Codec<InfernalSave> CODEC=RecordCodecBuilder.<InfernalSave>create(i->i.group(InfernalWork.CODEC.fieldOf("work").forGetter(InfernalSave::work),INPUT.fieldOf("input").forGetter(InfernalSave::input),OUTPUT.fieldOf("output").forGetter(InfernalSave::output)).apply(i,InfernalSave::new)).validate(s->(s.work.slot()<0||!s.input.get(s.work.slot()).isEmpty())&&(s.output.isEmpty()||s.work.slot()<0)?DataResult.success(s):DataResult.error(()->"Missing paid input or overlapping infernal work"));
}
