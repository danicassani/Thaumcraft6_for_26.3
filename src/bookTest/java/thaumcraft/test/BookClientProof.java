package thaumcraft.test;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.client.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class BookClientProof {
 static boolean book,table;static String ui="";
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!BookClientScene.mode().equals("client"))return;var mc=Minecraft.getInstance();if(mc.gui.screen()!=null){String name=mc.gui.screen().getClass().getSimpleName();if(!ui.equals(name)){ui=name;thaumcraft.Thaumcraft.LOGGER.info("BOOK56_UI: {}",name);}}
  if(mc.gui.screen() instanceof ResearchScreen screen&&BookClientScene.ready&&screen.getMenu().validData()){var menu=screen.getMenu();for(int c=1;c<7;c++)BookFlowProbe.check(!menu.categoryUnlocked(c),"Client exposed undiscovered category");if(!book){var recipe=BookRecipes.get("thaumcraft:thaumometer");BookFlowProbe.check(BookRecipes.stack(recipe.getAsJsonArray("grid").get(4)).is(net.minecraft.world.item.Items.GLASS_PANE)&&recipe.get("vis").getAsInt()==20,"Book diagram disagrees with actual pane/cost");book=true;thaumcraft.Thaumcraft.LOGGER.info("BOOK56_CLIENT_BOOK_READY: only Basics, graphical recipe pages and synchronized First Steps requirements");}geometry("book",mc);}
  if(mc.gui.screen() instanceof ArcaneScreen screen&&screen.getMenu().ready()){BookFlowProbe.check(screen.getMenu().cost()==20,"Client thaumometer cost not synchronized");if(!table){table=true;thaumcraft.Thaumcraft.LOGGER.info("BOOK56_CLIENT_TABLE_READY: single native output, exact ingredient icons, 20 vis, no Craft button or chunk label");}geometry("table",mc);}
 }
 static void geometry(String screen,Minecraft mc){try{var w=mc.getWindow();Files.writeString(Path.of("../work/book56-ui.json"),"{\"screen\":\""+screen+"\",\"gui_width\":"+w.getGuiScaledWidth()+",\"gui_height\":"+w.getGuiScaledHeight()+",\"width\":"+w.getWidth()+",\"height\":"+w.getHeight()+"}");}catch(Exception x){throw new RuntimeException(x);}}
}
