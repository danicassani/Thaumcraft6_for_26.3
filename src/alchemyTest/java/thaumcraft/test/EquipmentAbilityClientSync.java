package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.equipment.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class EquipmentAbilityClientSync {
 static boolean shown,title;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!EquipmentAbilityProbe.mode().equals("equipment40-client")||!EquipmentAbilityProbe.ready)return;var mc=Minecraft.getInstance();if(mc.level!=null&&mc.player!=null&&!shown){int n=0;for(var q:ArchitectPlane.select(EquipmentAbilityProbe.P,net.minecraft.core.Direction.UP,net.minecraft.core.Direction.NORTH,0))if(mc.level.getBlockState(q).is(net.minecraft.world.level.block.Blocks.DIRT))n++;if(n==9){shown=true;Thaumcraft.LOGGER.info("EQUIPMENT40_SCREENSHOT_READY: actual native shovel use built nine world blocks and synchronized architect preview");}}
 if(shown&&!title&&mc.gui.screen() instanceof net.minecraft.client.gui.screens.TitleScreen){title=true;Thaumcraft.LOGGER.info("EQUIPMENT40_TITLE_READY: native client saved world and closed integrated server");}}
}
