package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.client.*;
import thaumcraft.thaumaturgy.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class TurretClientProof {
 static boolean basic,advanced,basicShot,advancedShot,menu,edited;static boolean basicLoad,advancedLoad;static int oldFlags=-1;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.bookProbe","").equals("turret-client")||!TurretClientScene.ready)return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;mc.options.setCameraType(CameraType.FIRST_PERSON);
  for(var entity:mc.level.entitiesForRendering())if(entity instanceof CrossbowTurret t){var r=mc.getEntityRenderDispatcher().getRenderer(t);TurretProbe.check(t.advanced()?r instanceof TurretRenderers.Advanced:r instanceof TurretRenderers.Basic,"Missing original turret renderer");if(t.advanced()&&!advanced){advanced=true;thaumcraft.Thaumcraft.LOGGER.info("TURRET67_ADVANCED_RENDER_OK: original grouped OBJ renderer");}if(!t.advanced()&&!basic){basic=true;thaumcraft.Thaumcraft.LOGGER.info("TURRET67_BASIC_RENDER_OK: original articulated model");}if(t.loadProgress(0)>0){if(t.advanced()&&!advancedLoad){advancedLoad=true;thaumcraft.Thaumcraft.LOGGER.info("TURRET67_ADVANCED_LOAD_OK: synchronized original sliding loader");}if(!t.advanced()&&!basicLoad){basicLoad=true;thaumcraft.Thaumcraft.LOGGER.info("TURRET67_BASIC_LOAD_OK: synchronized original loading bars");}}if(t.shotProgress(0)>0){if(t.advanced()&&!advancedShot){advancedShot=true;thaumcraft.Thaumcraft.LOGGER.info("TURRET67_ADVANCED_ANIMATION_OK: native shot event animates bow groups");}if(!t.advanced()&&!basicShot){basicShot=true;thaumcraft.Thaumcraft.LOGGER.info("TURRET67_BASIC_ANIMATION_OK: native shot event animates bow arms");}}}
  if(mc.gui.screen() instanceof TurretScreen&&mc.player.containerMenu instanceof TurretMenu m){if(!menu&&m.value(0)==1&&!m.slots.getFirst().getItem().isEmpty()){TurretProbe.check(m.value(0)==1&&m.slots.getFirst().getItem().is(net.minecraft.world.item.Items.TIPPED_ARROW)&&m.slots.getFirst().getItem().has(net.minecraft.core.component.DataComponents.POTION_CONTENTS),"Client menu lost actual potion ammo");menu=true;oldFlags=m.value(1);thaumcraft.Thaumcraft.LOGGER.info("TURRET67_CLIENT_MENU_OK: native screen and real potion inventory synchronized");}else if(menu&&!edited&&m.value(1)!=oldFlags){edited=true;TurretProbe.check(m.value(1)==(oldFlags^1),"Native GUI toggled wrong target bit");thaumcraft.Thaumcraft.LOGGER.info("TURRET67_CLIENT_TOGGLE_OK: native click synchronized independent animal target flag");}}
 }
}
