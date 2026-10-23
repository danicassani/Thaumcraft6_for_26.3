package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.rifts.Wisp;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class WispSpawnClientSync {
 static boolean shown,particlesChecked;static int ticks;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){var mc=Minecraft.getInstance();
  if(!System.getProperty("thaumcraft.worldgenProbe","").equals("wisp-client"))return;
  if(!particlesChecked&&mc.level!=null&&mc.player!=null){
   var blocks=new java.util.ArrayList<net.minecraft.world.level.block.Block>();for(var entry:thaumcraft.crystals.CrystalContent.BLOCKS.values())blocks.add(entry.get());blocks.add(thaumcraft.taint.TaintContent.BLOCKS.get("taint_fibre").get());int count=0;
   for(var block:blocks)for(var state:block.getStateDefinition().getPossibleStates()){var material=mc.getModelManager().getBlockStateModelSet().get(state).particleMaterial(net.minecraftforge.client.model.data.ModelData.EMPTY);if(material==null)throw new AssertionError("No particle material for "+state);count++;}
   var bad=net.minecraft.world.level.block.Block.stateById(39141);mc.getModelManager().getBlockStateModelSet().get(bad).particleMaterial(net.minecraftforge.client.model.data.ModelData.EMPTY);
   for(var entry:thaumcraft.crystals.CrystalContent.BLOCKS.values())mc.level.levelEvent(2001,mc.player.blockPosition().below(2),net.minecraft.world.level.block.Block.getId(entry.get().defaultBlockState()));
   particlesChecked=true;Thaumcraft.LOGGER.info("WISP_PARTICLES_OK: {} crystal/fibre states, native destruction particles for unsupported crystals, prior disconnect state {}",count,bad);
  }
  if(shown||!WispSpawnProbe.ready||mc.level==null||mc.player==null||mc.gui.screen()!=null)return;for(var entity:mc.level.entitiesForRendering())if(entity instanceof Wisp w&&w.getUUID().equals(WispSpawnProbe.viewId)&&++ticks>=60){shown=true;Thaumcraft.LOGGER.info("WISP_SCREENSHOT_READY: natural forest wisp synchronized, aspect {}",w.aspect());return;}}
}
