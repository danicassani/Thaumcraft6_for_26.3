package thaumcraft.test;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.devices.*;

@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class M05ClientProbe {
    private static boolean done;
    private M05ClientProbe() {}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post event) {
        var client=Minecraft.getInstance();var pos=M05ClientScene.center;
        if(done||pos==null||client.level==null||!System.getProperty("thaumcraft.m05Probe","").equals("client"))return;
        if(client.level.getBlockEntity(pos.offset(-2,0,2)) instanceof DioptraEntity dioptra && dioptra.grid()[84]>0
            &&client.level.getBlockEntity(pos.offset(2,0,2)) instanceof RechargePedestalEntity pedestal && !pedestal.item().isEmpty()
            &&thaumcraft.api.items.RechargeHelper.getCharge(pedestal.item())>=5
            &&client.level.getBlockEntity(pos.offset(0,0,2)) instanceof VisGeneratorEntity generator && generator.getEnergyStored()>0) {
            done=true;
            Thaumcraft.LOGGER.info("M05_CLIENT_SYNC_OK: dioptra grid={}, pedestal item={}, charge={}",dioptra.grid()[84],pedestal.item(),thaumcraft.api.items.RechargeHelper.getCharge(pedestal.item()));
        }
    }
}
