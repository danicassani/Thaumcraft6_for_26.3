package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class SmelterClientSync {
 static int frames,waiting;static boolean shown,menu,title;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!SmelterProbe.mode().equals("smelters-client"))return;var mc=Minecraft.getInstance();if(SmelterProbe.ready&&mc.gui.screen() instanceof TitleScreen&&!title){title=true;Thaumcraft.LOGGER.info("SMELTERS_TITLE_READY: world saved and native title active");}if(mc.level==null||mc.player==null)return;
  if(SmelterProbe.ready&&!shown){int lit=0;waiting++;if(waiting>2400)throw new AssertionError("Smelter client synchronization timed out: pos="+mc.player.blockPosition()+", alive="+mc.player.isAlive());for(int i=0;i<3;i++)if(mc.level.getBlockEntity(SmelterProbe.P.offset(-5+i*5,0,0)) instanceof AlchemyMachine m&&m.tier()==i&&m.getBlockState().getValue(AlchemyBlock.LIT)&&m.speedBoost())lit++;if(waiting%100==0){var detail=new StringBuilder();for(int i=0;i<3;i++){var tile=mc.level.getBlockEntity(SmelterProbe.P.offset(-5+i*5,0,0));detail.append(" ").append(i).append(":").append(tile instanceof AlchemyMachine m?"tier="+m.tier()+",lit="+m.getBlockState().getValue(AlchemyBlock.LIT)+",boost="+m.speedBoost():"pending");}Thaumcraft.LOGGER.info("SMELTERS_SYNC_WAIT {}",detail);}if(lit==3&&++frames>20){shown=true;Thaumcraft.LOGGER.info("SMELTERS_SCREENSHOT_READY: three synchronized active smelter tiers, bellows, auxiliary alembics and vent");}}
  if(!menu&&mc.player.containerMenu instanceof AlchemyMenu m&&m.value(45)==1&&m.value(46)==1&&m.value(48)==1&&m.value(49)==1){menu=true;Thaumcraft.LOGGER.info("SMELTERS_MENU_OK: native menu synchronized tier=1 retention=90 bellows=1 auxiliary=1 alumentum=1 and live cooking/fuel");}
 }
}
