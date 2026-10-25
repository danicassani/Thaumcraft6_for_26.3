package thaumcraft.test;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.client.GrappleRenderer;
import thaumcraft.thaumaturgy.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class GrappleClientProof {
 static boolean idle,main,off,third,variant;static int camera=-1;static net.minecraft.world.phys.AABB loadedBox;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.bookProbe","").equals("grapple-client")||!GrappleClientScene.ready)return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
  int desired=Files.exists(Path.of("../work/grapple66-third"))?1:0;if(camera!=desired){camera=desired;mc.options.setCameraType(camera==0?CameraType.FIRST_PERSON:CameraType.THIRD_PERSON_BACK);}
  var gun=mc.player.getMainHandItem().getItem() instanceof GrappleGun?mc.player.getMainHandItem():mc.player.getOffhandItem();if(gun.getItem() instanceof GrappleGun&&!idle){var item=new ItemStackRenderState();mc.getItemModelResolver().updateForLiving(item,gun,ItemDisplayContext.GUI,mc.player);var box=item.getModelBoundingBox();GrappleProbe.check(!item.isEmpty()&&box.getZsize()>.1,"Original three-dimensional launcher model missing");loadedBox=box;idle=true;thaumcraft.Thaumcraft.LOGGER.info("GRAPPLE66_IDLE_MODEL_OK: native item definition resolved original 3D loaded model");}
  for(var entity:mc.level.entitiesForRendering())if(entity instanceof GrappleProjectile g&&g.anchored()&&g.getOwner()==mc.player){var renderer=mc.getEntityRenderDispatcher().getRenderer(g);GrappleProbe.check(renderer instanceof GrappleRenderer,"Missing original hook/cable renderer");var r=(GrappleRenderer)renderer;var state=r.createRenderState(g,1);if(state.handOffset==null)continue;if(!variant&&loadedBox!=null){var item=new ItemStackRenderState();mc.getItemModelResolver().updateForLiving(item,gun,ItemDisplayContext.GUI,mc.player);GrappleProbe.check(!loadedBox.equals(item.getModelBoundingBox()),"Native component did not select original unloaded model");variant=true;thaumcraft.Thaumcraft.LOGGER.info("GRAPPLE66_ACTIVE_MODEL_OK: native has-component condition selected original mesh without projectile");}var points=GrappleRope.points(state.handOffset,state.ageInTicks,true,state.anchorAge,state.flightAge);GrappleProbe.check(points.size()>2&&points.getLast().equals(state.handOffset),"Cable did not end at actual holding hand");if(!main&&g.hand()==net.minecraft.world.InteractionHand.MAIN_HAND){main=true;thaumcraft.Thaumcraft.LOGGER.info("GRAPPLE66_MAIN_RENDER_OK: original head, native item variant, finite textured cable to main hand");}if(!off&&g.hand()==net.minecraft.world.InteractionHand.OFF_HAND){off=true;thaumcraft.Thaumcraft.LOGGER.info("GRAPPLE66_OFF_RENDER_OK: other-hand launcher and left-side cable synchronized");}if(!third&&mc.options.getCameraType()==CameraType.THIRD_PERSON_BACK){third=true;thaumcraft.Thaumcraft.LOGGER.info("GRAPPLE66_THIRD_RENDER_OK: cable uses native third-person arm placement");}}
 }
}
