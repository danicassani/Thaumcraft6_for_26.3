package thaumcraft.client;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.research.ResearchContent;
import thaumcraft.research.ThaumometerItem;

/** Both hands lift the meter to the eye; the other held item is only visually hidden. */
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class ThaumometerHands {
    @SubscribeEvent public static boolean hand(RenderHandEvent event){
        var mc=Minecraft.getInstance();var player=mc.player;
        if(player==null||!player.isUsingItem()||!player.getUseItem().is(ResearchContent.METER.get()))return false;
        if(event.getHand()!=player.getUsedItemHand())return true;
        float elapsed=ThaumometerItem.SCAN_TICKS-player.getUseItemRemainingTicks()+event.getPartialTick();
        float progress=Math.clamp(elapsed/ThaumometerItem.SCAN_TICKS,0F,1F);
        float lift=progress*progress*(3-2*progress);
        var pose=event.getPoseStack();pose.pushPose();
        pose.translate(Math.sin(elapsed*1.7)*.003*lift,-.48+.28*lift,-.85+.22*lift);
        pose.rotateDegrees(Axis.ZP,(float)Math.sin(elapsed*1.3)*.65F*lift);
        if(!player.isInvisible()&&mc.getEntityRenderDispatcher().getRenderer(player) instanceof AvatarRenderer<?> renderer){
            for(int sign:new int[]{1,-1}){
                pose.pushPose();
                // Anchor the native fingertips on the frame, then let the arm extend back toward the player.
                pose.translate(sign*.34,-.18,.02);
                pose.rotateDegrees(Axis.YP,sign*12);
                pose.rotateDegrees(Axis.XP,-55);
                pose.rotateDegrees(Axis.ZP,sign*-8);
                double palmCenter=player.getSkin().model()==net.minecraft.world.entity.player.PlayerModelType.SLIM?.03125:.0625;
                double tipX=.3125+palmCenter*Math.cos(.1)+.625*Math.sin(.1);
                double tipY=.125-palmCenter*Math.sin(.1)+.625*Math.cos(.1);
                pose.translate(sign*tipX,-tipY,0);
                if(sign==1)renderer.renderRightHand(pose,event.getNodeCollector(),event.getPackedLight(),player.getSkin().body().texturePath(),true);
                else renderer.renderLeftHand(pose,event.getNodeCollector(),event.getPackedLight(),player.getSkin().body().texturePath(),true);
                pose.popPose();
            }
        }
        var item=new ItemStackRenderState();mc.getItemModelResolver().updateForLiving(item,player.getUseItem(),ItemDisplayContext.NONE,player);
        pose.scale(.9F,.9F,.9F);
        item.submit(pose,event.getNodeCollector(),event.getPackedLight(),OverlayTexture.NO_OVERLAY,0);
        pose.popPose();return true;
    }
}
