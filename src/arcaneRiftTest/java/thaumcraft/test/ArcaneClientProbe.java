package thaumcraft.test;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.crafting.ArcaneMenu;
import thaumcraft.rifts.FluxRiftEntity;

@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class ArcaneClientProbe {
    private static boolean menu,craft,rift;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post event){
        if(!System.getProperty("thaumcraft.arcaneRiftProbe","").equals("client"))return;
        var client=Minecraft.getInstance();if(client.player==null||client.level==null)return;
        if(!rift)for(var entity:client.level.entitiesForRendering())if(entity instanceof FluxRiftEntity flux&&flux.size()==80&&flux.seed()==42){rift=true;Thaumcraft.LOGGER.info("ARCANE_CLIENT_RIFT_SYNC_OK: size/seed/mesh");}
        if(client.player.containerMenu instanceof ArcaneMenu arcane) {
            if(!menu&&arcane.ready()&&arcane.cost()==50&&arcane.chunks()==9&&!arcane.slots.get(16).getItem().isEmpty()) {menu=true;Thaumcraft.LOGGER.info("ARCANE_CLIENT_MENU_SYNC_OK: preview, cost={}, aura={}, range={} chunks",arcane.cost(),arcane.available(),arcane.chunks());}
            if(!craft&&!arcane.slots.get(15).getItem().isEmpty()){craft=true;Thaumcraft.LOGGER.info("ARCANE_CLIENT_CRAFT_OK: actual output={}, crystals={}/{}",arcane.slots.get(15).getItem(),arcane.slots.get(9).getItem().getCount(),arcane.slots.get(11).getItem().getCount());}
        }
    }
}
