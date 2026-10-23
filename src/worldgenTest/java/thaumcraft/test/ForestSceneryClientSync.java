package thaumcraft.test;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.worldgen.MagicalForest;

@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class ForestSceneryClientSync {
    static int stage,ticks,defaultGrass,defaultFoliage;static boolean done,originalBlue,restored;
    static String lastScreen="";
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();if(!ForestSceneryProbe.mode().equals("decoration-client"))return;
        if(mc.level==null&&mc.gui.screen()!=null){var name=mc.gui.screen().getClass().getSimpleName();if(!name.equals(lastScreen)){lastScreen=name;Thaumcraft.LOGGER.info("DECORATION_UI_SCREEN: {}",name);}}
        if(done){if(!restored&&java.nio.file.Files.exists(java.nio.file.Path.of("../work/decoration53-capture-proof.json"))){ThaumcraftConfig.blueBiome.set(originalBlue);restored=true;Thaumcraft.LOGGER.info("DECORATION_OPTIONS_RESTORED: original client palette option restored after capture");}return;}
        if(!ForestSceneryProbe.ready||mc.level==null||mc.player==null||mc.gui.screen()!=null||++ticks<60)return;
        var registry=mc.level.registryAccess().lookupOrThrow(Registries.BIOME);var forest=registry.getOrThrow(MagicalForest.KEY).value();var plains=registry.getOrThrow(Biomes.PLAINS).value();
        var p=ForestSceneryProbe.mushroom;
        ForestSceneryProbe.check(mc.level.getBlockState(p).is(net.minecraft.world.level.block.Blocks.RED_MUSHROOM_BLOCK)||mc.level.getBlockState(p).is(net.minecraft.world.level.block.Blocks.BROWN_MUSHROOM_BLOCK),"Natural mushroom not synchronized");
        ForestSceneryProbe.check(mc.level.getBlockState(ForestSceneryProbe.rock).is(net.minecraft.world.level.block.Blocks.MOSSY_COBBLESTONE),"Natural rock not synchronized");
        if(stage==0){originalBlue=ThaumcraftConfig.blueBiome.get();ThaumcraftConfig.blueBiome.set(false);stage=1;ticks=0;return;}
        if(stage==1){ForestSceneryProbe.check(BiomeColors.GRASS_COLOR_RESOLVER.getColor(forest,0,0)==forest.getGrassColor(0,0)&&BiomeColors.FOLIAGE_COLOR_RESOLVER.getColor(forest,0,0)==forest.getFoliageColor(),"Default palette changed");defaultGrass=BiomeColors.getAverageGrassColor(mc.level,p);defaultFoliage=BiomeColors.getAverageFoliageColor(mc.level,p);ThaumcraftConfig.blueBiome.set(true);stage=2;ticks=0;return;}
        if(stage==2){
            ForestSceneryProbe.check(BiomeColors.GRASS_COLOR_RESOLVER.getColor(forest,0,0)==(0xff000000|6728396)&&BiomeColors.FOLIAGE_COLOR_RESOLVER.getColor(forest,0,0)==(0xff000000|7851246),"Blue opaque palette ignored");
            ForestSceneryProbe.check(BiomeColors.GRASS_COLOR_RESOLVER.getColor(plains,0,0)==plains.getGrassColor(0,0)&&BiomeColors.FOLIAGE_COLOR_RESOLVER.getColor(plains,0,0)==plains.getFoliageColor(),"Other biome recolored");
            ForestSceneryProbe.check((forest.getGrassColor(0,0)&0xffffff)==5635969&&(forest.getFoliageColor()&0xffffff)==6750149,"Registry biome was mutated");
            // Exercise the actual cached/blended terrain color path, not only the resolver lambda.
            ForestSceneryProbe.check(BiomeColors.getAverageGrassColor(mc.level,p)!=defaultGrass&&BiomeColors.getAverageFoliageColor(mc.level,p)!=defaultFoliage,"Native cache did not update to blue");
            ThaumcraftConfig.blueBiome.set(false);
            stage=3;ticks=0;return;
        }
        if(stage==3){
            ForestSceneryProbe.check(BiomeColors.getAverageGrassColor(mc.level,p)==defaultGrass&&BiomeColors.getAverageFoliageColor(mc.level,p)==defaultFoliage,"Native cache did not restore default colors");
            Thaumcraft.LOGGER.info("DECORATION_COLORS_OK: live default/blue/default roundtrip, other biome unchanged, registry unchanged, native tint caches update");
            ThaumcraftConfig.blueBiome.set(true);stage=4;ticks=0;return;
        }
        if(!mc.levelRenderer.isSectionCompiledAndVisible(p,0)||!mc.levelRenderer.isSectionCompiledAndVisible(ForestSceneryProbe.rock,0)){
            ForestSceneryProbe.check(ticks<600,"Sample terrain did not compile after palette switch");return;
        }
        done=true;Thaumcraft.LOGGER.info("DECORATION_SCREENSHOT_READY: natural mushrooms and mossy rock synchronized and compiled, blue client palette enabled");
    }
}
