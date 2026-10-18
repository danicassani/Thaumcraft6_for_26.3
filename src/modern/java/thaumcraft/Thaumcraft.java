package thaumcraft;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.persistence.PlayerWarpStore;

@Mod(Thaumcraft.MODID)
public final class Thaumcraft {
    public static final String MODID = "thaumcraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Thaumcraft(FMLJavaModLoadingContext context) {
        var modBus = context.getModBusGroup();
        Registrar.register(modBus);
        context.registerConfig(ModConfig.Type.CLIENT, ThaumcraftConfig.CLIENT, "thaumcraft-client.toml");
        context.registerConfig(ModConfig.Type.SERVER, ThaumcraftConfig.SERVER, "thaumcraft-server.toml");
        FMLCommonSetupEvent.getBus(modBus).addListener(event ->
            LOGGER.info("Thaumcraft initialized: basic ores, materials and storage blocks loaded"));
        PlayerWarpStore.registerEvents();
    }
}
