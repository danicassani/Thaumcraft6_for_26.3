package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class ContainerFocusClientProof {
 static boolean exchange,cracks,done;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.containerFocusProbe","").equals("client"))return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
  if(!exchange&&ContainerFocusClient.stage==2&&mc.level.getBlockState(ContainerFocusClient.Q).is(Blocks.DIRT)&&mc.player.getInventory().getItem(10).getCount()==3){exchange=true;ExchangeProbe.check(mc.gui.screen()==null,"Chest GUI opened instead of casting");thaumcraft.Thaumcraft.LOGGER.info("CONTAINER76_CLIENT_EXCHANGE_SYNC_OK: native client sees replacement, sample and paid material");}
  var progress=mc.level.destructionProgress().get(ContainerFocusClient.Q.asLong());
  if(!cracks&&ContainerFocusClient.stage==3&&progress!=null&&progress.stream().anyMatch(p->p.getProgress()>=4&&p.getProgress()<=8)){cracks=true;ContainerFocusClient.cracks=true;thaumcraft.Thaumcraft.LOGGER.info("CONTAINER76_CLIENT_CRACKS_OK: real chest crack packets received");}
  if(!done&&ContainerFocusClient.stage==6&&mc.level.getBlockState(ContainerFocusClient.Q).isAir()&&(progress==null||progress.isEmpty())){done=true;thaumcraft.Thaumcraft.LOGGER.info("CONTAINER76_CLIENT_CLEAR_OK: mined chest and crack cleanup synchronized");}
 }
}
