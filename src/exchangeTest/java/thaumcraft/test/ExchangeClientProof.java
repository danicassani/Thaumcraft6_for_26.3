package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.thaumaturgy.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class ExchangeClientProof {
 static boolean selected,done;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.exchangeProbe","").equals("client"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;var sample=ExchangeSelection.selected(mc.player.getMainHandItem());
  if(!selected&&sample.is(Items.STONE_BRICKS)){selected=true;ExchangeProbe.check(sample.getCount()==1&&ExchangeSelection.supports(mc.player.getMainHandItem()),"Selected sample component invalid");thaumcraft.Thaumcraft.LOGGER.info("EXCHANGE75_CLIENT_SAMPLE_SYNC_OK: selected component and exchange focus reach native client HUD");}
  if(!done&&selected&&ExchangeClient.stage==3&&mc.level.getBlockState(ExchangeClient.Q).is(Blocks.STONE_BRICKS)&&mc.player.getInventory().getItem(10).getCount()==2){done=true;thaumcraft.Thaumcraft.LOGGER.info("EXCHANGE75_CLIENT_SYNC_OK: replacement, material count and saved sample synchronized");}
 }
}
