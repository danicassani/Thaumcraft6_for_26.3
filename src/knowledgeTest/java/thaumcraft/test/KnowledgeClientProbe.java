package thaumcraft.test;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.research.*;

@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class KnowledgeClientProbe {
    private static boolean menu,advanced;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post event){
        if(!System.getProperty("thaumcraft.knowledgeProbe","").equals("client"))return;var client=Minecraft.getInstance();if(client.player==null)return;
        if(client.player.containerMenu instanceof ResearchMenu book){
            int first=ResearchCatalog.ENTRIES.indexOf(ResearchCatalog.BY_KEY.get("FIRSTSTEPS"));
            if(!menu&&book.validData()&&book.stage(first)==2&&book.points(false,0)>=18&&book.scans()==1){menu=true;Thaumcraft.LOGGER.info("KNOWLEDGE_CLIENT_MENU_OK: native sync stage 2, observations {}, scans 1, ready={}",book.points(false,0),book.ready());}
            if(!advanced&&book.stage(first)==4&&book.points(false,0)==2){advanced=true;Thaumcraft.LOGGER.info("KNOWLEDGE_CLIENT_ADVANCE_OK: native button packet completed First Steps and spent exactly 16 points");}
        }
    }
}
