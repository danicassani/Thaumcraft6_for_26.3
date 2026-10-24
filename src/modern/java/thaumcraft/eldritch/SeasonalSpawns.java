package thaumcraft.eldritch;
import java.time.LocalDate;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraftforge.common.world.*;
import net.minecraftforge.registries.*;
/** The original Halloween Overworld bonus is evaluated when the server loads its biome data. */
public final class SeasonalSpawns implements BiomeModifier {
 public static final MapCodec<SeasonalSpawns> CODEC=MapCodec.unit(SeasonalSpawns::new);
 private static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS=DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS,"thaumcraft");
 private static final RegistryObject<MapCodec<SeasonalSpawns>> TYPE=SERIALIZERS.register("halloween_firebats",()->CODEC);
 public static boolean halloween(LocalDate date){return date.getMonthValue()==10&&date.getDayOfMonth()==31;}
 public void modify(Holder<Biome> biome,Phase phase,ModifiableBiomeInfo.BiomeInfo.Builder builder){if(phase==Phase.ADD&&halloween(LocalDate.now())&&biome.is(net.minecraft.tags.BiomeTags.IS_OVERWORLD))ForgeBiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(HolderSet.direct(biome),5,new MobSpawnSettings.SpawnerData(EldritchContent.FIREBAT.get(),UniformInt.of(1,2))).modify(biome,phase,builder);}
 public MapCodec<? extends BiomeModifier> codec(){return CODEC;}
 public static void register(net.minecraftforge.eventbus.api.bus.BusGroup b){SERIALIZERS.register(b);}
}
