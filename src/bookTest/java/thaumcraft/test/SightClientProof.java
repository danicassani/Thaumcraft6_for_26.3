package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.research.*;
import thaumcraft.client.ThaumometerSight;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class SightClientProof {
 static boolean unseen,known,block,mob;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){
  if(!System.getProperty("thaumcraft.bookProbe","").equals("sight-client")||!SightClientScene.ready)return;
  var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;
  var snapshot=ThaumometerSight.visible();if(snapshot==null)return;
  if(snapshot.identity().equals("item:minecraft:diamond")){
   if(!snapshot.known()){ScanProbe.check(snapshot.scannable()&&snapshot.aspects().isEmpty(),"Unseen item exposed aspect values");if(!unseen){unseen=true;thaumcraft.Thaumcraft.LOGGER.info("SIGHT61_PREVIEW_UNKNOWN_OK: authenticated target and no undiscovered aspects");}}
   else {ScanProbe.check(!snapshot.scannable()&&!snapshot.aspects().isEmpty(),"Known item lacked its aspects");if(!known){known=true;thaumcraft.Thaumcraft.LOGGER.info("SIGHT61_PREVIEW_KNOWN_OK: received per-item aspect icons and quantities");}}
  }
  if(snapshot.known()&&snapshot.identity().equals("item:minecraft:glass")&&!block){block=true;thaumcraft.Thaumcraft.LOGGER.info("SIGHT61_PREVIEW_BLOCK_OK");}
  if(snapshot.known()&&snapshot.identity().equals("entity:minecraft:cow")&&!mob){mob=true;thaumcraft.Thaumcraft.LOGGER.info("SIGHT61_PREVIEW_MOB_OK");}
 }
}
