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
        var snapshot=AuraNetwork.clientSnapshot();if(snapshot==null||!snapshot.dimension().equals(client.level.dimension().identifier())||snapshot.chunk()!=client.player.chunkPosition().pack())return;
        var cell=snapshot.cell();
        var aura=Component.translatable("research.thaumcraft.aura",cell.base(),String.format(java.util.Locale.ROOT,"%.1f",cell.vis()),String.format(java.util.Locale.ROOT,"%.1f",cell.flux()));
        boolean meter=client.player.getMainHandItem().is(ResearchContent.METER.get())||client.player.getOffhandItem().is(ResearchContent.METER.get());
        var use=client.options.keyUse.getTranslatedKeyMessage();var sneak=client.options.keyShift.getTranslatedKeyMessage();
        var first=Component.translatable("research.thaumcraft.scan_hint",use);
        var other=thaumcraft.research.ScanningService.otherHand(client.player);
        var second=other.isEmpty()||other.is(ResearchContent.METER.get())?Component.translatable("research.thaumcraft.scan_other_empty_hint",sneak,use):Component.translatable("research.thaumcraft.scan_other_hint",sneak,use,other.getHoverName());
        int width=client.font.width(aura);if(meter)width=Math.max(width,Math.max(client.font.width(first),client.font.width(second)));
        graphics.fill(5,5,Math.min(width+13,client.getWindow().getGuiScaledWidth()-5),meter?47:23,0xAA20152D);
        graphics.text(client.font,aura,9,9,0xFFE9D2FA,false);
        if(meter){graphics.text(client.font,first,9,22,0xFFB4DDF0,false);graphics.text(client.font,second,9,35,0xFFB4DDF0,false);}
    });}
}
