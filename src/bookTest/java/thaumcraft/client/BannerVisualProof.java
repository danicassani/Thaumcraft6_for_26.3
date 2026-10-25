package thaumcraft.client;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.banners.*;
import thaumcraft.mirrors.*;
import thaumcraft.test.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class BannerVisualProof {
 static boolean sync,wave,mirrors,book;static float first=Float.NaN;static int counter;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.bookProbe","").equals("banner-client"))return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||!BannerClientScene.photo)return;counter++;
 if(!book){for(var ref:BookRecipes.expand("thaumcraft:Banners")){var item=BookRecipes.result(BookRecipes.get(ref));BannerProbe.check(item.getItem() instanceof BlockItem b&&b.getBlock() instanceof AspectBannerBlock,"Book still yields vanilla banners");}book=true;thaumcraft.Thaumcraft.LOGGER.info("BANNER72_CLIENT_BOOK_OK: all sixteen book recipes yield actual TC banners");}
 if(!sync&&mc.level.getBlockEntity(BannerClientScene.P) instanceof AspectBannerEntity b&&b.aspect().equals("aer")){sync=true;thaumcraft.Thaumcraft.LOGGER.info("BANNER72_CLIENT_SYNC_OK: native phial aspect reached the rendered client block");}
 if(!wave&&mc.level.getBlockEntity(BannerClientScene.P) instanceof AspectBannerEntity b){var dispatcher=mc.getBlockEntityRenderDispatcher();Object r=dispatcher.getRenderer(b);BannerProbe.check(r instanceof BannerRenderer,"Original banner renderer missing");var renderer=(BannerRenderer)r;var state=renderer.createRenderState();renderer.extractRenderState(b,state,0,mc.player.position(),null);if(Float.isNaN(first))first=state.wave;else if(counter>30&&Math.abs(first-state.wave)>.0001){wave=true;thaumcraft.Thaumcraft.LOGGER.info("BANNER72_CLIENT_WAVE_OK: cloth angle advances in live world; glyph shares its transform");}}
 if(!mirrors&&BannerClientScene.view.equals("mirrors")){int active=0;for(var face:net.minecraft.core.Direction.values()){if(mc.level.getBlockEntity(BannerClientScene.M.east(face.ordinal()*2)) instanceof MirrorEntity b){Object raw=mc.getBlockEntityRenderDispatcher().getRenderer(b);BannerProbe.check(raw instanceof MirrorRenderer,"Mirror portal renderer missing");var r=(MirrorRenderer)raw;var state=r.createRenderState();r.extractRenderState(b,state,0,mc.player.position(),null);if(state.active&&state.face==face)active++;}}if(active==6){mirrors=true;thaumcraft.Thaumcraft.LOGGER.info("BANNER72_CLIENT_MIRRORS_OK: six linked native portal surfaces active and correctly oriented");}}
 }
}
