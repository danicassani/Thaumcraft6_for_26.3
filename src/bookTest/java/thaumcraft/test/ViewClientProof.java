package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.research.*;
import thaumcraft.client.ArcaneScreen;
import thaumcraft.crafting.ArcaneMenu;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class ViewClientProof {
 static int arms;static boolean idle,walk,table,offhand,reversing;
 static boolean active(){return System.getProperty("thaumcraft.bookProbe","").equals("view-client");}
 @SubscribeEvent public static void arm(RenderArmEvent e){var p=Minecraft.getInstance().player;if(active()&&p!=null&&!p.isUsingItem()&&ScanTarget.holdingMeter(p))arms++;}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!active()||!ViewClientScene.ready)return;var mc=Minecraft.getInstance();var p=mc.player;if(p==null)return;
  if(!idle&&arms>=2&&!p.isUsingItem()){idle=true;thaumcraft.Thaumcraft.LOGGER.info("VIEW62_IDLE_POSE_OK: native arm rendering while not using the item");}
  if(!walk&&p.avatarState().getInterpolatedBob(1)>.005){walk=true;ScanProbe.check(mc.options.bobView().get(),"Native walking bob disabled in test settings");thaumcraft.Thaumcraft.LOGGER.info("VIEW62_WALK_BOB_OK: native walking bob is active");}
  if(!offhand&&p.getOffhandItem().is(ResearchContent.METER.get())){if(!reversing){reversing=true;arms=0;}else if(arms>=2){offhand=true;thaumcraft.Thaumcraft.LOGGER.info("VIEW62_OFFHAND_OK");}}
  if(!table&&mc.gui.screen() instanceof ArcaneScreen screen){var m=screen.getMenu();ScanProbe.check(m.slots.get(0).x==40&&m.slots.get(0).y==40&&m.slots.get(15).x==160&&m.slots.get(15).y==64,"Incorrect workbench layout");for(int i=0;i<6;i++)ScanProbe.check(m.slots.get(9+i).x==ArcaneMenu.crystalX(i)&&m.slots.get(9+i).y==ArcaneMenu.crystalY(i),"Crystal layout lost sync");table=true;thaumcraft.Thaumcraft.LOGGER.info("VIEW62_MENU_SYNC_OK: original crystal ring and grid synchronized on client");}
 }
}
