package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.client.*;
import thaumcraft.research.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class ProgressionClientProof {
 static boolean hidden,shown,page;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.bookProbe","").equals("progression-client")||!ProgressionClientScene.ready)return;var mc=Minecraft.getInstance();if(!(mc.gui.screen() instanceof ResearchScreen s)||!s.getMenu().validData())return;var m=s.getMenu();var index=ResearchCatalog.ENTRIES.indexOf(ResearchCatalog.BY_KEY.get("SEALCOLLECT"));if(m.selected()!=index)return;if(!hidden){ProgressionProbe.check(!m.addendumVisible(index,0)&&!s.illustratedRecipes().contains("thaumcraft:SealCollectAdv"),"Advanced seal page revealed before biothaumic mind");hidden=true;thaumcraft.Thaumcraft.LOGGER.info("PROGRESSION68_CLIENT_HIDDEN_OK: completed base seal excludes locked addendum");}if(m.addendumVisible(index,0)&&!shown){ProgressionProbe.check(s.illustratedRecipes().contains("thaumcraft:SealCollectAdv"),"Unlocked addendum recipe not in book");var v=BookRecipes.get("thaumcraft:SealCollectAdv");ProgressionProbe.check(v!=null&&!BookRecipes.result(v).isEmpty(),"Advanced seal illustration has no native item");shown=true;thaumcraft.Thaumcraft.LOGGER.info("PROGRESSION68_CLIENT_ADDENDUM_OK: server unlock reveals advanced text and exact native recipe");}if(shown&&!page&&s.currentRecipe().equals("thaumcraft:SealCollectAdv")){page=true;thaumcraft.Thaumcraft.LOGGER.info("PROGRESSION68_CLIENT_RECIPE_OK: native page navigation displays advanced seal recipe");}}
}
