package thaumcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.aura.AuraNetwork;
import thaumcraft.content.SimpleContent;
import thaumcraft.research.ResearchContent;

@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class ThaumometerHud {
    private ThaumometerHud(){}
    @SubscribeEvent public static void layers(AddGuiOverlayLayersEvent event){event.getLayeredDraw().add(SimpleContent.id("thaumometer"),(graphics,time)->{
        var client=Minecraft.getInstance();if(client.player==null||client.level==null||client.gui.hud.isHidden())return;
        if(!client.player.getMainHandItem().is(ResearchContent.METER.get())&&!client.player.getOffhandItem().is(ResearchContent.METER.get())&&!thaumcraft.equipment.FortressMasks.hasGoggles(client.player)&&!(client.player.getMainHandItem().getItem() instanceof thaumcraft.thaumaturgy.CasterItem))return;
        if(!CrucibleSight.draw(graphics))ThaumometerSight.draw(graphics);
        var snapshot=AuraNetwork.clientSnapshot();if(snapshot==null||!snapshot.dimension().equals(client.level.dimension().identifier())||snapshot.chunk()!=client.player.chunkPosition().pack())return;
        var cell=snapshot.cell();
        var aura=Component.translatable("research.thaumcraft.aura",cell.base(),String.format(java.util.Locale.ROOT,"%.1f",cell.vis()),String.format(java.util.Locale.ROOT,"%.1f",cell.flux()));
        int width=client.font.width(aura);
        graphics.fill(5,5,Math.min(width+13,client.getWindow().getGuiScaledWidth()-5),23,0xAA20152D);
        graphics.text(client.font,aura,9,9,0xB3E9D2FA,false);
    });}
}
