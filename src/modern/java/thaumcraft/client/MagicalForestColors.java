package thaumcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.worldgen.MagicalForest;

/** Client-only palette, shared by native grass/foliage tint sources and their biome blending. */
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class MagicalForestColors {
    private static volatile Biome forest;
    private static volatile boolean blue;
    private MagicalForestColors() {}

    @SubscribeEvent public static void resolvers(RegisterColorHandlersEvent.ColorResolvers event) {
        var grass=BiomeColors.GRASS_COLOR_RESOLVER;
        var foliage=BiomeColors.FOLIAGE_COLOR_RESOLVER;
        BiomeColors.GRASS_COLOR_RESOLVER=(biome,x,z)->blue&&biome==forest?(0xff000000|6728396):grass.getColor(biome,x,z);
        BiomeColors.FOLIAGE_COLOR_RESOLVER=(biome,x,z)->blue&&biome==forest?(0xff000000|7851246):foliage.getColor(biome,x,z);
        event.register(BiomeColors.GRASS_COLOR_RESOLVER);
        event.register(BiomeColors.FOLIAGE_COLOR_RESOLVER);
    }

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();
        var next=mc.level==null?null:mc.level.registryAccess().lookupOrThrow(Registries.BIOME).get(MagicalForest.KEY).map(h->h.value()).orElse(null);
        boolean enabled=ThaumcraftConfig.blueBiome.get();
        if(forest==next&&blue==enabled)return;
        forest=next;blue=enabled;
        if(mc.level!=null){
            mc.levelExtractor.allChanged();
        }
    }
}
