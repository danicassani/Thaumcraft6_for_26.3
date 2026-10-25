package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.research.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class CheatSheetClientProof {
 static int ticks;static boolean book;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.bookProbe","").equals("cheat-client"))return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.getConnection()==null||CheatSheetClientScene.stage==0)return;ticks++;var tab=thaumcraft.content.BasicContent.TAB.get();
 if(CheatSheetClientScene.stage==1&&!CheatSheetClientScene.disabled&&ticks>30&&tab.getDisplayItems().stream().anyMatch(s->s.is(ResearchContent.BOOK.get()))){CheatSheetProbe.check(tab.getDisplayItems().stream().noneMatch(CheatSheet::special),"Client creative tab leaks disabled book");CheatSheetProbe.check(mc.getConnection().searchTrees().creativeNameSearch().search("thaumonomicon").stream().noneMatch(CheatSheet::special),"Client search leaks disabled book");CheatSheetClientScene.disabled=true;thaumcraft.Thaumcraft.LOGGER.info("CHEAT73_CLIENT_DISABLED_OK: native creative tab/search exclude special book, ordinary book retained");}
 if(CheatSheetClientScene.stage>=2&&!CheatSheetClientScene.enabled&&tab.getDisplayItems().stream().filter(CheatSheet::special).count()==1&&mc.getConnection().searchTrees().creativeNameSearch().search("thaumonomicon").stream().filter(CheatSheet::special).count()==1&&CheatSheet.special(mc.player.getMainHandItem())){CheatSheetClientScene.enabled=true;thaumcraft.Thaumcraft.LOGGER.info("CHEAT73_CLIENT_ENABLED_OK: synchronized marker, live creative tab and native name-search cache include exactly one special book");}
 if(!book&&CheatSheetClientScene.stage==3&&mc.gui.screen() instanceof thaumcraft.client.ResearchScreen screen&&screen.getMenu().validData()){var menu=screen.getMenu();for(int i=0;i<148;i++){if(menu.stage(i)<=ResearchCatalog.ENTRIES.get(i).stages().size())return;CheatSheetProbe.check(menu.visible(i),"Completed entry hidden on client");for(int j=0;j<ResearchCatalog.ENTRIES.get(i).addenda().size();j++)CheatSheetProbe.check(menu.addendumVisible(i,j),"Cheat left addendum locked");}for(int c=0;c<7;c++)CheatSheetProbe.check(menu.categoryUnlocked(c),"Cheat left category locked");book=true;CheatSheetClientScene.done=true;thaumcraft.Thaumcraft.LOGGER.info("CHEAT73_CLIENT_BOOK_OK: real screen synchronizes 148 completed topics, seven categories and every addendum");}
 }
}
