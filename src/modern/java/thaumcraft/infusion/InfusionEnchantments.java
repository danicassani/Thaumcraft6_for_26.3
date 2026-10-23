package thaumcraft.infusion;

import com.mojang.serialization.*;
import java.util.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.*;
import net.minecraft.core.registries.Registries;
import thaumcraft.Thaumcraft;

/** Stable string identities; separate from vanilla enchantments, as in TC6. */
public final class InfusionEnchantments {
    public enum Kind {
        COLLECTOR(1),DESTRUCTIVE(1),BURROWING(1),SOUNDING(4),REFINING(4),ARCING(4),ESSENCE(5),LAMPLIGHT(1);
        public final int max;
        Kind(int max){this.max=max;}
        public String id(){return name().toLowerCase(Locale.ROOT);}
        public boolean accepts(ItemStack s){return switch(this){case ARCING,ESSENCE->s.is(ItemTags.SWORDS)||s.is(ItemTags.AXES);case BURROWING->s.is(ItemTags.AXES)||s.is(ItemTags.PICKAXES);case SOUNDING,REFINING->s.is(ItemTags.PICKAXES);default->s.is(ItemTags.AXES)||s.is(ItemTags.PICKAXES)||s.is(ItemTags.SHOVELS)||(this==COLLECTOR&&s.is(ItemTags.SWORDS));};}
        public static Kind of(String id){for(var k:values())if(k.id().equals(id))return k;return null;}
    }
    private static final Codec<Map<String,Integer>> CODEC=Codec.unboundedMap(Codec.STRING,Codec.intRange(1,5)).validate(m->m.size()<=Kind.values().length&&m.entrySet().stream().allMatch(e->Kind.of(e.getKey())!=null&&e.getValue()<=Kind.of(e.getKey()).max)?DataResult.success(Map.copyOf(m)):DataResult.error(()->"Invalid infusion enchantments"));
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,Thaumcraft.MODID);
    public static final RegistryObject<DataComponentType<Map<String,Integer>>> ENCHANTMENTS=COMPONENTS.register("infusion_enchantments",()->DataComponentType.<Map<String,Integer>>builder().persistent(CODEC).networkSynchronized(ByteBufCodecs.fromCodec(CODEC)).build());
    public static final RegistryObject<DataComponentType<Integer>> WARP=COMPONENTS.register("infusion_warp",()->DataComponentType.<Integer>builder().persistent(Codec.intRange(0,127)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static int warp(ItemStack stack){return stack.getOrDefault(WARP.get(),0);}
    /** Roll only when committing a job, never when rendering or matching a recipe. */
    public static ItemStack commitWarp(ItemStack original,ItemStack result,int roll){if(roll<0||roll>=10)throw new IllegalArgumentException("Invalid infusion warp draw");var out=result.copy();if(roll<original.getOrDefault(ENCHANTMENTS.get(),Map.<String,Integer>of()).size())out.set(WARP.get(),Math.min(127,warp(original)+1));return out;}
    public static int level(ItemStack s,Kind k){return s.getOrDefault(ENCHANTMENTS.get(),Map.<String,Integer>of()).getOrDefault(k.id(),0);}
    public static boolean canApply(ItemStack s,Kind k){return s.getCount()==1&&k.accepts(s)&&level(s,k)<k.max;}
    public static ItemStack apply(ItemStack s,Kind k){if(!canApply(s,k))return ItemStack.EMPTY;var out=s.copy();var values=new TreeMap<>(s.getOrDefault(ENCHANTMENTS.get(),Map.<String,Integer>of()));values.put(k.id(),level(s,k)+1);out.set(ENCHANTMENTS.get(),Map.copyOf(values));return out;}
    public static float cost(ItemStack s,Kind k){var all=s.getOrDefault(ENCHANTMENTS.get(),Map.<String,Integer>of());return level(s,k)+1+(all.size()-(all.containsKey(k.id())?1:0))*.33F;}
    public static void register(BusGroup bus){COMPONENTS.register(bus);InfusionToolEvents.register();}
}
