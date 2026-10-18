package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.AlchemyMachine;
import thaumcraft.essentia.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class EssentiaClientProbe{
    static boolean sync;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(sync||!System.getProperty("thaumcraft.essentiaProbe","").equals("client"))return;var c=Minecraft.getInstance();if(c.level!=null&&c.level.getBlockEntity(EssentiaClientScene.J) instanceof AlchemyMachine jar&&jar.total()>=20&&jar.filter().equals("aer")&&c.level.getBlockEntity(EssentiaClientScene.C) instanceof TubeEntity valve&&valve.getBlockState().getValue(TubeBlock.OPEN)){sync=true;Thaumcraft.LOGGER.info("ESSENTIA_CLIENT_SYNC_OK: server contents, label and valve state reached client");}}
}
