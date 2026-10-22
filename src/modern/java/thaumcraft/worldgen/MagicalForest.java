package thaumcraft.worldgen;

import java.util.ArrayList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.*;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.content.SimpleContent;

/** Adds climate space to the vanilla preset before any levels or terrain are created. */
public final class MagicalForest {
    public static final ResourceKey<Biome> KEY=ResourceKey.create(Registries.BIOME,SimpleContent.id("magical_forest"));
    private MagicalForest() {}
    public static void register() { ServerAboutToStartEvent.BUS.addListener(MagicalForest::prepare); }
    private static void prepare(ServerAboutToStartEvent event) {
        if (!ThaumcraftConfig.generateMagicForest.get() || ThaumcraftConfig.biomeMagicalForestWeight.get()==0) return;
        var registries=event.getServer().registryAccess();
        var preset=registries.lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
            .getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD).value();
        var forest=registries.lookupOrThrow(Registries.BIOME).getOrThrow(KEY);
        var values=new ArrayList<Pair<Climate.ParameterPoint,Holder<Biome>>>();
        int slices=0;
        double fraction=ThaumcraftConfig.biomeMagicalForestWeight.get()/(50D+ThaumcraftConfig.biomeMagicalForestWeight.get());
        for (var entry:preset.parameters().values()) {
            var p=entry.getFirst();
            // Replace whole climate cells: narrow strips can be shadowed by neighboring climates.
            // A stable hash makes increasing the weight monotonic and independent of startup order.
            long hash=mix(p.temperature().min() ^ Long.rotateLeft(p.humidity().min(),9)
                ^ Long.rotateLeft(p.continentalness().min(),21) ^ Long.rotateLeft(p.erosion().min(),33)
                ^ Long.rotateLeft(p.weirdness().min(),45) ^ p.weirdness().max());
            if (entry.getSecond().is(Biomes.FOREST) && p.depth().min()==0 && (hash>>>11)*0x1.0p-53<fraction) {
                values.add(Pair.of(p,forest));slices++;
            } else values.add(entry);
        }
        preset.parameters=new Climate.ParameterList<>(values);
        var possible=com.google.common.collect.ImmutableSet.copyOf(values.stream().map(Pair::getSecond).toList());
        for (var stem:registries.lookupOrThrow(Registries.LEVEL_STEM)) {
            var generator=stem.generator();
            if (generator.getBiomeSource() instanceof MultiNoiseBiomeSource source && source.stable(MultiNoiseBiomeSourceParameterLists.OVERWORLD)) {
                source.possibleBiomes=()->possible;
                generator.refreshFeaturesPerStep();
            }
        }
        Thaumcraft.LOGGER.info("Magical forest: {} climate cells, weight {} (new terrain only)",slices,ThaumcraftConfig.biomeMagicalForestWeight.get());
    }
    private static long mix(long value) {
        value=(value^(value>>>30))*0xbf58476d1ce4e5b9L;
        value=(value^(value>>>27))*0x94d049bb133111ebL;
        return value^(value>>>31);
    }
}
