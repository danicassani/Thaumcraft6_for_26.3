package thaumcraft.client;
import java.util.*;
import java.lang.reflect.Proxy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.theory.*;
import thaumcraft.test.ResearchArtScene;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class ResearchArtProof {
 static boolean world,idle,removed,restored,cards,played,cancelled,menu;static int ticks,originalScale;
 static void check(boolean condition,String reason){if(!condition)throw new AssertionError(reason);}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.bookProbe","").equals("research-art"))return;var mc=Minecraft.getInstance();
  if(cancelled&&mc.level==null&&mc.gui.screen() instanceof net.minecraft.client.gui.screens.TitleScreen&&!menu){menu=true;thaumcraft.Thaumcraft.LOGGER.info("RESEARCH80_CLIENT_MENU_READY");}
  if(!ResearchArtScene.ready||mc.level==null||mc.player==null||!(mc.level.getBlockEntity(ResearchArtScene.TABLE) instanceof ResearchTableEntity t))return;
  var renderer=(ResearchTableRenderer)(Object)mc.getBlockEntityRenderDispatcher().getRenderer(t);check(renderer!=null,"Research table renderer absent");var state=renderer.createRenderState();renderer.extractRenderState(t,state,0,mc.player.position(),null);
  if(!world&&state.ink&&++ticks>30){originalScale=mc.getWindow().getGuiScale();check(mc.getResourceManager().getResource(ResearchTableRenderer.MODEL).isPresent()&&mc.getResourceManager().getResource(ResearchTableRenderer.QUILL).isPresent(),"Original prop textures missing");check(t.getRenderBoundingBox().maxY>t.getBlockPos().getY()+1.6,"Quill outside render bounds");world=true;thaumcraft.Thaumcraft.LOGGER.info("RESEARCH80_PROPS_OK: original quill and inkwell resources and synchronized tools");}
  if(cancelled&&mc.gui.screen()==null)check(mc.getWindow().getGuiScale()==originalScale,"Research screen did not restore GUI scale: expected "+originalScale+" got "+mc.getWindow().getGuiScale());
  if(!idle&&mc.gui.screen()==null)originalScale=mc.getWindow().getGuiScale();
  if(!(mc.gui.screen() instanceof TheoryScreen)||!(mc.player.containerMenu instanceof TheoryMenu m))return;
  if(mc.player.tickCount%10==0){
   var buttons=mc.gui.screen().children().stream().filter(v->v instanceof net.minecraft.client.gui.components.Button).map(v->(net.minecraft.client.gui.components.Button)v).toList();check(buttons.size()>=7,"Research controls absent");
   int gw=mc.getWindow().getGuiScaledWidth(),gh=mc.getWindow().getGuiScaledHeight();double scale=mc.getWindow().getWidth()/(double)gw;var json=new com.google.gson.JsonObject();json.addProperty("width",mc.getWindow().getWidth());json.addProperty("cardsCount",m.card(2)!=null?3:m.card(1)!=null?2:m.card(0)!=null?1:0);
   for(int i=0;i<7;i++){var b=buttons.get(i);var point=new com.google.gson.JsonArray();point.add((b.getX()+b.getWidth()/2.0)*scale);point.add((b.getY()+b.getHeight()/2.0)*scale);json.add(new String[]{"start","finish","cancel","deck","card0","card1","card2"}[i],point);}
   for(int i:new int[]{0,37}){var slot=m.getSlot(i);var point=new com.google.gson.JsonArray();point.add(((gw-255)/2+slot.x+8)*scale);point.add(((gh-255)/2+slot.y+8)*scale);json.add(i==0?"tools":"returnedTools",point);}
   try{java.nio.file.Files.writeString(java.nio.file.Path.of("../work/research80-layout.json"),json.toString());}catch(java.io.IOException ex){throw new RuntimeException(ex);}
  }
  if(!idle&&m.value(10)==1){check(mc.getWindow().getGuiScaledHeight()>=265,"Research board is clipped by GUI scale");
   for(var kind:TheoryCards.Kind.values()){String text=net.minecraft.network.chat.Component.translatable("theory.thaumcraft.effect."+kind.name().toLowerCase(Locale.ROOT),kind==TheoryCards.Kind.INFUSE?"25":"Alchemy",25).getString();check(!text.contains("%s")&&!text.contains("%%"),"Card translation contains unresolved formatting: "+kind);}
   idle=true;check(m.slots.size()==38&&m.getSlot(0).x==16&&m.getSlot(1).x==224&&m.getSlot(2).x==77&&m.getSlot(29).x==20,"Original inventory layout not synchronized");for(var path:List.of("gui/gui_research_table","gui/gui_base","gui/paper","gui/papergilded"))check(mc.getResourceManager().getResource(net.minecraft.resources.Identifier.parse("thaumcraft:textures/"+path+".png")).isPresent(),"Original GUI resource missing");thaumcraft.Thaumcraft.LOGGER.info("RESEARCH80_IDLE_OK: original board, inventory and controls ready");}
  if(idle&&!removed&&!state.ink){int[] draws={0};SubmitNodeCollector out=(SubmitNodeCollector)Proxy.newProxyInstance(SubmitNodeCollector.class.getClassLoader(),new Class<?>[]{SubmitNodeCollector.class},(p,method,args)->{if(method.getName().equals("submitCustomGeometry"))draws[0]++;return method.getReturnType().isInstance(p)?p:null;});renderer.submit(state,new PoseStack(),out,null);check(draws[0]==0,"Removed tools still rendered");removed=true;thaumcraft.Thaumcraft.LOGGER.info("RESEARCH80_TOOLS_REMOVED_OK: actual shift transfer removed props without chunk reload");}
  if(removed&&!restored&&state.ink){restored=true;thaumcraft.Thaumcraft.LOGGER.info("RESEARCH80_TOOLS_RESTORED_OK: returned tools restored props without chunk reload");}
  if(!cards&&m.card(0)!=null&&m.card(1)!=null&&m.card(2)!=null&&m.value(21+6)==1){check(state.active,"Active theory scroll state not synchronized");cards=true;thaumcraft.Thaumcraft.LOGGER.info("RESEARCH80_CARDS_OK: three server-backed parchment choices with requirements and category progress");}
  if(cards&&!played&&m.value(7)==1){played=true;thaumcraft.Thaumcraft.LOGGER.info("RESEARCH80_CLIENT_PLAY_OK");}
  if(played&&!cancelled&&m.value(1)==1){cancelled=true;check(!state.active,"Cancelled theory still renders scroll");thaumcraft.Thaumcraft.LOGGER.info("RESEARCH80_CLIENT_CANCEL_OK");}
 }
}
