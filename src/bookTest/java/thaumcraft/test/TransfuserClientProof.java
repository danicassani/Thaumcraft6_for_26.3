package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.essentia.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class TransfuserClientProof {
 static boolean models,flow,photo;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.bookProbe","").equals("transfuser-client"))return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||!TransfuserClientScene.ready)return;if(!models){for(var id:new String[]{"essentia_input","essentia_output"}){var view=thaumcraft.client.BookRecipes.get(id.equals("essentia_input")?"thaumcraft:EssentiaTransportIn":"thaumcraft:EssentiaTransportOut");TransfuserProbe.check(thaumcraft.client.BookRecipes.result(view).is((id.equals("essentia_input")?EssentiaContent.INPUT:EssentiaContent.OUTPUT).get().asItem()),"Book still uses mirror placeholder");TransfuserProbe.check(mc.getResourceManager().getResource(net.minecraft.resources.Identifier.parse("thaumcraft:models/block/"+id+".json")).isPresent(),"Original transfuser model missing");}models=true;thaumcraft.Thaumcraft.LOGGER.info("TRANSFUSER71_CLIENT_MODELS_OK: original models and real book recipe items");}if(!flow&&mc.level.getBlockEntity(TransfuserClientScene.P) instanceof TransfuserEntity a&&a.snapshot().transferred()>=5&&mc.level.getBlockEntity(TransfuserClientScene.Q) instanceof TransfuserEntity b&&b.snapshot().transferred()>=5){flow=true;thaumcraft.Thaumcraft.LOGGER.info("TRANSFUSER71_CLIENT_SYNC_OK: native transfer counters received on client");}if(TransfuserClientScene.photo&&!photo){photo=true;thaumcraft.Thaumcraft.LOGGER.info("TRANSFUSER71_CLIENT_PHOTO_OK: two native machines, jars, tubes and measured totals");}}
}
