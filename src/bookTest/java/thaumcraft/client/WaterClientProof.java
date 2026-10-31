package thaumcraft.client;
import java.util.*;
import java.lang.reflect.Proxy;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.test.*;
import thaumcraft.alchemy.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class WaterClientProof {
 static boolean rendered,scanned,menu;static int wait;
 static void check(boolean v,String reason){if(!v)throw new AssertionError(reason);}
 static AlchemyRenderer.State state(Minecraft mc,net.minecraft.core.BlockPos pos){var m=(AlchemyMachine)mc.level.getBlockEntity(pos);var renderer=(AlchemyRenderer)(Object)mc.getBlockEntityRenderDispatcher().getRenderer(m);var s=renderer.createRenderState();renderer.extractRenderState(m,s,0,mc.player.position(),null);return s;}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.bookProbe","").equals("water-client"))return;var mc=Minecraft.getInstance();
  if(rendered&&scanned&&mc.level==null&&mc.gui.screen() instanceof net.minecraft.client.gui.screens.TitleScreen&&!menu){menu=true;thaumcraft.Thaumcraft.LOGGER.info("WATER80_CLIENT_MENU_READY");}
  if(!WaterClientScene.ready||mc.player==null||mc.level==null)return;
  if(WaterClientScene.phase.equals("crucibles")&&!rendered&&++wait>40&&mc.level.getBlockEntity(WaterClientScene.C.east(4)) instanceof AlchemyMachine){
   var m=(AlchemyMachine)mc.level.getBlockEntity(WaterClientScene.C);var dirty=(AlchemyMachine)mc.level.getBlockEntity(WaterClientScene.C.east(2));if(m.water.getFluidAmount()!=1000||dirty.total()!=200)return;
   var clean=state(mc,WaterClientScene.C);var stained=state(mc,WaterClientScene.C.east(2));var empty=state(mc,WaterClientScene.C.east(4));
   check(clean.show&&clean.crucible&&!empty.show&&stained.color!=clean.color&&stained.y1>clean.y1,"Crucible water states incorrect");
   int expected=BiomeColors.getAverageWaterColor(mc.level,WaterClientScene.C);check(clean.color==(0xff000000|expected),"Clean water does not use biome tint");
   var sprite=mc.getAtlasManager().get(new net.minecraft.client.resources.model.sprite.SpriteId(clean.waterAtlas,Identifier.parse("minecraft:block/water_still")));check(sprite.contents().name().equals(Identifier.parse("minecraft:block/water_still"))&&clean.u1>clean.u0&&clean.v1>clean.v0,"Native water sprite missing");
   int[] counts=new int[5];VertexConsumer vertices=(VertexConsumer)Proxy.newProxyInstance(VertexConsumer.class.getClassLoader(),new Class<?>[]{VertexConsumer.class},(p,method,a)->{switch(method.getName()){case "addVertex"->counts[0]++;case "setUv"->{counts[1]++;check((float)a[0]>=clean.u0&&(float)a[0]<=clean.u1&&(float)a[1]>=clean.v0&&(float)a[1]<=clean.v1,"UV outside native sprite");}case "setLight"->counts[2]++;case "setOverlay"->counts[3]++;case "setNormal"->counts[4]++;}return method.getReturnType().isInstance(p)?p:null;});
   SubmitNodeCollector collector=(SubmitNodeCollector)Proxy.newProxyInstance(SubmitNodeCollector.class.getClassLoader(),new Class<?>[]{SubmitNodeCollector.class},(p,method,a)->{if(method.getName().equals("submitCustomGeometry")){check(a[1]==RenderTypes.entitySolid(clean.waterAtlas),"Crucible uses untextured or transparent debug pipeline");((SubmitNodeCollector.CustomGeometryRenderer)a[2]).render(((PoseStack)a[0]).last(),vertices);}return method.getReturnType().isInstance(p)?p:null;});
   var renderer=(AlchemyRenderer)(Object)mc.getBlockEntityRenderDispatcher().getRenderer(m);var before=m.snapshot();renderer.submit(clean,new PoseStack(),collector,null);for(int n:counts)check(n==4,"Incomplete water vertex format");check(before.equals(m.snapshot()),"Rendering mutated machine contents");rendered=true;thaumcraft.Thaumcraft.LOGGER.info("WATER80_RENDER_OK: real native atlas, water tint, textured UV/light/overlay/normals, clean/stained/empty levels, unchanged contents");
  }
  var view=ThaumometerSight.visible();if(!scanned&&WaterClientScene.phase.equals("underwater")&&mc.player.isInWater()&&view!=null&&view.known()&&view.identity().equals("item:minecraft:diamond_block")){scanned=true;thaumcraft.Thaumcraft.LOGGER.info("WATER80_PREVIEW_OK: underwater target agrees with server and shows diamond aspects");}
 }
}
