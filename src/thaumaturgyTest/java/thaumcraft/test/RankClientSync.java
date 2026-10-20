package thaumcraft.test;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.*;
import thaumcraft.golems.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class RankClientSync {
 static boolean first,max,packed;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.thaumaturgyProbe","").equals("rank-client"))return;var mc=net.minecraft.client.Minecraft.getInstance();if(mc.player==null||mc.level==null)return;for(var ent:mc.level.entitiesForRendering())if(ent instanceof WorkerGolem g){if(!first&&g.progress().rank()==1&&g.progress().xp()==1){first=true;Thaumcraft.LOGGER.info("RANK_SYNC_FIRST_OK");}if(!max&&g.progress().rank()==10){max=true;Thaumcraft.LOGGER.info("RANK_SYNC_MAX_OK");}}var s=mc.player.getMainHandItem();if(!packed&&s.getItem() instanceof GolemPlacer&&s.getOrDefault(GolemContent.PROGRESS.get(),GolemProgress.EMPTY).rank()==10){var expected=s.get(GolemContent.PROGRESS.get()).description().getString();var lines=s.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.of(mc.level),mc.player,net.minecraft.world.item.TooltipFlag.NORMAL);ThaumaturgyProbe.check(lines.stream().anyMatch(line->line.getString().equals(expected)),"Native tooltip omitted golem rank");packed=true;Thaumcraft.LOGGER.info("RANK_SYNC_ITEM_OK: {}",s.get(GolemContent.PROGRESS.get()).description().getString());}}
}
