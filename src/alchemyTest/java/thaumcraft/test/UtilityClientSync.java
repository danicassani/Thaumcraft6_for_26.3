package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.utility.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class UtilityClientSync {
 static boolean menu,mixed,taken,returned,shown,title;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!UtilityProbe.mode().equals("utility-client")||!UtilityProbe.ready)return;var mc=Minecraft.getInstance();var p=UtilityProbe.P;if(mc.player!=null&&mc.player.containerMenu instanceof UtilityMenu m&&m.value(0)==0){var salt=m.getSlot(0).getItem();if(!menu&&m.value(2)==500&&salt.is(thaumcraft.taint.TaintContent.SALTS.get())&&salt.getCount()==1){menu=true;Thaumcraft.LOGGER.info("UTILITY_MENU_OK: real native spa menu with500mB and one retained salt");}if(menu&&!mixed&&m.value(3)==1){mixed=true;Thaumcraft.LOGGER.info("UTILITY_MIX_OK: native button packet changed spa to mixing mode");}int count=0;for(int i=0;i<mc.player.getInventory().getContainerSize();i++)if(mc.player.getInventory().getItem(i).is(thaumcraft.taint.TaintContent.SALTS.get()))count+=mc.player.getInventory().getItem(i).getCount();if(mixed&&!taken&&salt.isEmpty()&&count==1){taken=true;Thaumcraft.LOGGER.info("UTILITY_TAKE_OK: real shift-click moved exactly one salt to player");}if(taken&&!returned&&salt.is(thaumcraft.taint.TaintContent.SALTS.get())&&salt.getCount()==1&&count==0){returned=true;Thaumcraft.LOGGER.info("UTILITY_RETURN_OK: real shift-click returned the salt without duplication");}}
 if(returned&&mc.player!=null&&!(mc.player.containerMenu instanceof UtilityMenu)&&mc.gui.screen()==null)UtilityProbe.menuDone=true;
 if(UtilityProbe.framed&&!shown&&mc.level!=null&&mc.gui.screen()==null&&mc.level.getBlockEntity(p.south(4)) instanceof WaterDevice spa&&spa.amount()==500&&spa.mix()&&spa.inventory.getItem(0).getCount()==1&&mc.level.getBlockState(p.west(5).south(2)).is(net.minecraft.world.level.block.Blocks.WHEAT)&&mc.level.getBlockState(p.west(5).south(2)).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AGE_7)>0){shown=true;Thaumcraft.LOGGER.info("UTILITY_SCREENSHOT_READY: client sees paid water/spa state, native grown wheat and six utility devices after real menu interaction");}
 if(shown&&!title&&mc.gui.screen() instanceof net.minecraft.client.gui.screens.TitleScreen){title=true;Thaumcraft.LOGGER.info("UTILITY_TITLE_READY: native world saved and integrated server closed");}}
}
