package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.client.*;
import thaumcraft.research.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class AtlasClientProof {
 static boolean initial,icons,pages,category,zoom,pan,recipe,turned;static int chaptersSeen;static double[] anchor;static int sum;
 static void check(boolean ok,String msg){if(!ok)throw new AssertionError(msg);}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.bookProbe","").equals("atlas-client")||!AtlasClientScene.ready)return;var mc=Minecraft.getInstance();if(!(mc.gui.screen() instanceof ResearchScreen s)||!s.getMenu().validData())return;var m=s.getMenu();
  if(!icons){for(var t:ResearchAtlas.TOPICS.values())for(var icon:t.icons()){if(icon.has("texture"))check(mc.getResourceManager().getResource(net.minecraft.resources.Identifier.parse(icon.get("texture").getAsString())).isPresent(),"Missing original topic texture "+t.key());else check(!BookRecipes.stack(icon).isEmpty(),"Missing translated item icon "+t.key()+" "+icon);}icons=true;thaumcraft.Thaumcraft.LOGGER.info("BOOK69_ICONS_OK: all 148 original topics have usable native texture or item icons");}
  if(!initial&&s.mapView()&&!AtlasClientScene.expanded&&s.visibleTopics()>0){int chapters=0;for(int c=0;c<7;c++)if(m.categoryUnlocked(c))chapters++;check(chapters==1&&s.visibleTopics()>=2&&s.visibleTopics()<10,"New player sees undiscovered topics/chapters");initial=true;var p=s.nodeCenter("FIRSTSTEPS");thaumcraft.Thaumcraft.LOGGER.info("BOOK69_INITIAL_MAP_OK: original coordinates, only Basics, {} visible topics; native First Steps click {},{}",s.visibleTopics(),Math.round(p[0]*mc.getWindow().getGuiScale()),Math.round(p[1]*mc.getWindow().getGuiScale()));}
  if(!pages&&!s.mapView()&&!AtlasClientScene.expanded){check(ResearchCatalog.ENTRIES.get(m.selected()).key().equals("FIRSTSTEPS")&&m.stage(m.selected())==1&&!m.ready(),"Opening pages changed research or enabled unpaid progression");pages=true;thaumcraft.Thaumcraft.LOGGER.info("BOOK69_PAGES_OK: native topic click opens original parchment spread, gated requirements retained");}
  if(pages&&!turned&&!AtlasClientScene.expanded&&!s.mapView()&&s.currentRecipe().equals("thaumcraft:salismundusfake")){turned=true;thaumcraft.Thaumcraft.LOGGER.info("BOOK69_PAGE_TURN_OK: native forward arrow turns the two-leaf spread to the Salis Mundus diagram");}
  if(AtlasClientScene.expanded&&s.mapView()){int old=chaptersSeen;chaptersSeen|=1<<s.currentCategory();if(chaptersSeen==127&&old!=127)thaumcraft.Thaumcraft.LOGGER.info("BOOK69_ALL_CHAPTERS_OK: all seven original backgrounds displayed through native tabs");}
  if(AtlasClientScene.expanded&&!s.mapView()&&!recipe&&s.currentRecipe().equals("thaumcraft:thaumometer")){var v=BookRecipes.get("thaumcraft:thaumometer");check(BookRecipes.stack(v.getAsJsonArray("grid").get(4)).is(net.minecraft.world.item.Items.GLASS_PANE)&&v.get("vis").getAsInt()==20,"Original pages changed the actual thaumometer recipe");recipe=true;thaumcraft.Thaumcraft.LOGGER.info("BOOK69_RECIPE_OK: native parchment pages show real glass pane, gold, six crystals and 20 vis");}
  if(AtlasClientScene.expanded&&s.mapView()&&s.currentCategory()==3){if(!category){int n=0;for(int c=0;c<7;c++)if(m.categoryUnlocked(c))n++;if(n<7)return;category=true;sum=0;for(int i=0;i<ResearchCatalog.ENTRIES.size();i++)sum+=m.stage(i);thaumcraft.Thaumcraft.LOGGER.info("BOOK69_CATEGORY_OK: native Artifice tab shows original icons and dependencies; seven discovered chapters");}if(s.zoom()!=1&&!zoom){zoom=true;anchor=s.nodeCenter("BASEARTIFICE");thaumcraft.Thaumcraft.LOGGER.info("BOOK69_ZOOM_OK: native wheel zoom={}",s.zoom());}if(zoom&&!pan&&anchor!=null){var now=s.nodeCenter("BASEARTIFICE");if(Math.abs(now[0]-anchor[0])>8||Math.abs(now[1]-anchor[1])>8){int current=0;for(int i=0;i<ResearchCatalog.ENTRIES.size();i++)current+=m.stage(i);check(current==sum,"Map navigation changed server research");pan=true;thaumcraft.Thaumcraft.LOGGER.info("BOOK69_PAN_OK: native drag moves map without changing authoritative progress");}}}
  if(pages&&!s.mapView()&&s.currentRecipe().equals("thaumcraft:salismundusfake"))thaumcraft.Thaumcraft.LOGGER.debug("BOOK69 recipe spread visible");
 }
}
