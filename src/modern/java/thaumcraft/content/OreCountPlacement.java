package thaumcraft.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.config.ThaumcraftConfig;

/** Apply the existing server generation toggles and ore density to new chunks. */
public record OreCountPlacement(String ore, int attempts) implements RepeatingPlacement {
    public static final MapCodec<OreCountPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.stringResolver(s -> s, s -> switch (s) { case "amber", "cinnabar", "quartz" -> s; default -> null; })
            .fieldOf("ore").forGetter(OreCountPlacement::ore),
        Codec.intRange(1, 64).fieldOf("count").forGetter(OreCountPlacement::attempts)
    ).apply(instance, OreCountPlacement::new));
    private static final DeferredRegister<MapCodec<? extends PlacementModifier>> TYPES =
        DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, Thaumcraft.MODID);
    static { TYPES.register("ore_count", () -> CODEC); }

    public static void register(BusGroup bus) { TYPES.register(bus); }

    @Override
    public int count(RandomSource random, BlockPos origin) {
        boolean enabled = switch (ore) {
            case "amber" -> ThaumcraftConfig.generateAmber.get();
            case "cinnabar" -> ThaumcraftConfig.generateCinnabar.get();
            case "quartz" -> ThaumcraftConfig.generateQuartz.get();
            default -> false;
        };
        if (!enabled) return 0;
        int scaled = attempts * ThaumcraftConfig.oreDensity.get();
        return scaled / 100 + (random.nextInt(100) < scaled % 100 ? 1 : 0);
    }

    @Override
    public MapCodec<OreCountPlacement> codec() { return CODEC; }
}
