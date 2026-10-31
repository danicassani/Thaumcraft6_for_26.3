package thaumcraft.client;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.research.*;
import thaumcraft.test.AspectDisplayScene;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class AspectDisplayProof {
 static final Set<String> seen=new HashSet<>();static boolean book,menu;
 static void check(boolean v,String reason){if(!v)throw new AssertionError(reason);}
 static void mark(String s){if(seen.add(s))thaumcraft.Thaumcraft.LOGGER.info("ASPECTS82_"+s+"_OK");}
 static void field(Object o,String name,Object value)throws Exception{var f=o.getClass().getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e)throws Exception{if(!System.getProperty("thaumcraft.bookProbe","").equals("aspect-display"))return;var mc=Minecraft.getInstance();if(seen.contains("EMPTY")&&mc.level==null&&mc.gui.screen() instanceof net.minecraft.client.gui.screens.TitleScreen&&!menu){menu=true;mark("CLIENT_MENU");}if(mc.level==null||mc.player==null)return;
 if(mc.gui.screen() instanceof ResearchScreen s&&s.getMenu().selected()==ResearchCatalog.ENTRIES.indexOf(ResearchCatalog.BY_KEY.get("UNLOCKALCHEMY"))){if(!book){field(s,"reading",true);var m=ResearchScreen.class.getDeclaredMethod("textLeaves");m.setAccessible(true);int text=(int)m.invoke(s);int index=s.illustratedRecipes().indexOf("thaumcraft:nitor");check(index>=0,"Nitor recipe absent");field(s,"spread",(text+index)/2);book=true;}if(s.shownRecipes().contains("thaumcraft:nitor")){check(s.readerLayoutFits(),"Book margins fail");mark("BOOK");}}
 if(mc.gui.screen() instanceof AbstractContainerScreen<?> s&&!(s instanceof ResearchScreen)&&mc.player.tickCount%5==0){var json=new com.google.gson.JsonObject();int[] slots={37,38,39,40};for(int index:slots){var slot=s.getMenu().getSlot(index);double scale=mc.getWindow().getGuiScale();var a=new com.google.gson.JsonArray();a.add((s.getGuiLeft()+slot.x+8)*scale);a.add((s.getGuiTop()+slot.y+8)*scale);json.add(Integer.toString(index),a);}Files.writeString(Path.of("../work/aspects82-slots.json"),json.toString());}
 var target=CrucibleSight.target();String phase=AspectDisplayScene.phase;if(phase.equals("crucible")&&target!=null&&target.aspects().equals(Map.of("aqua",10,"ignis",10,"potentia",10)))mark("CRUCIBLE");if(phase.equals("goggles")&&target!=null&&target.aspects().equals(Map.of("aqua",7,"ignis",4,"lux",3))&&!ScanTarget.holdingMeter(mc.player))mark("GOGGLES");if(phase.equals("empty")&&target!=null&&target.aspects().isEmpty())mark("EMPTY");if(phase.equals("away")&&target==null)mark("AWAY");
 }
 @SubscribeEvent public static void tooltip(RenderTooltipEvent.Pre e){if(!System.getProperty("thaumcraft.bookProbe","").equals("aspect-display"))return;var mc=Minecraft.getInstance();var update=InventoryInspection.received();if(update==null||mc.level==null||!net.minecraft.world.item.ItemStack.isSameItemSameComponents(e.getItemStack(),InventoryInspection.item(update,mc.level.registryAccess())))return;var rows=e.getComponents().stream().filter(c->c instanceof InventoryAspects.Row).map(c->(InventoryAspects.Row)c).toList();var stack=e.getItemStack();
 if(stack.is(net.minecraft.world.item.Items.COAL)&&update.known()){check(rows.size()==1&&rows.getFirst().aspects().equals(update.aspects()),"Coal tooltip row missing");check(stack.getCount()==64,"Tooltip changed count");mark("COAL_TOOLTIP");}
 if(stack.is(net.minecraft.world.item.Items.DIAMOND)&&!update.known()){check(rows.isEmpty()&&update.aspects().isEmpty(),"Unknown diamond revealed");mark("UNKNOWN_TOOLTIP");}
 if(stack.is(thaumcraft.crystals.CrystalContent.ESSENCE.get())){String aspect=thaumcraft.crystals.CrystalContent.aspect(stack);if(aspect.equals("aer")&&update.known()){check(rows.size()==1&&rows.getFirst().aspects().equals(Map.of("aer",1)),"Known crystal tooltip wrong");mark("CRYSTAL_TOOLTIP");}if(aspect.equals("ignis")&&!update.known()){check(rows.isEmpty(),"Unknown crystal variant revealed");mark("VARIANT_TOOLTIP");}}
 }
}
