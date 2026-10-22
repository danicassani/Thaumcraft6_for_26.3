package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.mirrors.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class MirrorClientSync {
 static boolean menu,transferred,shown,title,waiting,returned;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!MirrorProbe.mode().equals("mirrors-client")||!MirrorProbe.ready)return;var mc=Minecraft.getInstance();if(mc.player!=null&&mc.player.containerMenu instanceof HandMirrorMenu m){if(!menu&&m.slots.size()==37&&m.getSlot(1).getItem().is(net.minecraft.world.item.Items.DIAMOND)&&m.getSlot(1).getItem().getCount()==8){menu=true;Thaumcraft.LOGGER.info("MIRRORS_MENU_OK: native hand mirror opened with bound component and eight real inventory diamonds");}if(menu&&!transferred&&m.getSlot(1).getItem().isEmpty()&&m.getSlot(0).getItem().isEmpty()&&m.getCarried().isEmpty()){transferred=true;Thaumcraft.LOGGER.info("MIRRORS_CLIENT_TRANSFER_OK: native shift-click transferred all eight diamonds out of player inventory and hand input");}}
 if(mc.player!=null&&mc.player.getInventory().getSelectedSlot()==1&&mc.player.containerMenu instanceof HandMirrorMenu pending&&!waiting&&pending.getSlot(0).getItem().is(net.minecraft.world.item.Items.IRON_INGOT)&&pending.getSlot(0).getItem().getCount()==3&&pending.getSlot(2).getItem().isEmpty()){waiting=true;Thaumcraft.LOGGER.info("MIRRORS_CLIENT_WAITING_OK: real native hand menu retains three iron ingots for unloaded destination");}
 if(waiting&&!returned&&mc.player!=null&&!(mc.player.containerMenu instanceof HandMirrorMenu)&&mc.gui.screen()==null){int count=0;for(int i=0;i<mc.player.getInventory().getContainerSize();i++)if(mc.player.getInventory().getItem(i).is(net.minecraft.world.item.Items.IRON_INGOT))count+=mc.player.getInventory().getItem(i).getCount();if(count==3){returned=true;Thaumcraft.LOGGER.info("MIRRORS_CLIENT_RETURN_OK: closing failed native delivery returned all three ingots to real player inventory");}}
 if(transferred&&!shown&&mc.level!=null&&mc.gui.screen()==null){int count=0;for(var entity:mc.level.entitiesForRendering())if(entity instanceof net.minecraft.world.entity.item.ItemEntity item&&item.blockPosition().distSqr(MirrorProbe.P.east(2))<16&&item.getItem().is(net.minecraft.world.item.Items.DIAMOND))count+=item.getItem().getCount();if(count==8){shown=true;Thaumcraft.LOGGER.info("MIRRORS_SCREENSHOT_READY: exactly eight diamonds visible at destination mirror after real native hand transfer; source inventory empty");}}
 if(shown&&returned&&!title&&mc.gui.screen() instanceof net.minecraft.client.gui.screens.TitleScreen){title=true;Thaumcraft.LOGGER.info("MIRRORS_TITLE_READY: native world saved and integrated server closed");}}
}
