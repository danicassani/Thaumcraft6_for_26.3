package thaumcraft.client;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.world.InteractionHand;
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

/** Two-handed viewing pose while equipped; native walking bob and a subtle scan tremor. */
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class ThaumometerHands {
    @SubscribeEvent public static boolean hand(RenderHandEvent event){
        var mc=Minecraft.getInstance();var player=mc.player;
        if(player==null||!thaumcraft.research.ScanTarget.holdingMeter(player))return false;
        var selection=FirstPersonHandsAndItems.evaluateWhichHandsToRender(player);
        var renderedHand=selection.renderMainHand?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND;
        if(event.getHand()!=renderedHand)return true;
        var meter=player.getMainHandItem().is(ResearchContent.METER.get())?player.getMainHandItem():player.getOffhandItem();
        boolean scanning=player.isUsingItem()&&player.getUseItem().is(ResearchContent.METER.get());
        float elapsed=scanning?ThaumometerItem.SCAN_TICKS-player.getUseItemRemainingTicks()+event.getPartialTick():0;
        float progress=Math.clamp(elapsed/ThaumometerItem.SCAN_TICKS,0F,1F);
        float lift=progress*progress*(3-2*progress);
        var pose=event.getPoseStack();pose.pushPose();
        // Keep the native walking/view bob from the parent pose, without adding a second walk cycle.
        pose.translate(Math.sin(elapsed*1.7)*.002*lift,.03,-.62+.015*lift);
        pose.rotateDegrees(Axis.ZP,(float)Math.sin(elapsed*1.3)*.65F*lift);
        if(!player.isInvisible()&&mc.getEntityRenderDispatcher().getRenderer(player) instanceof AvatarRenderer<?> renderer){
            for(int sign:new int[]{1,-1}){
                pose.pushPose();
                // Anchor the native fingertips on the frame, then let the arm extend back toward the player.
                pose.translate(sign*.2875,-.629,.02);
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
        var item=new ItemStackRenderState();mc.getItemModelResolver().updateForLiving(item,meter,ItemDisplayContext.NONE,player);
        pose.scale(1.035F,1.035F,1.035F);
        item.submit(pose,event.getNodeCollector(),event.getPackedLight(),OverlayTexture.NO_OVERLAY,0);
        pose.popPose();return true;
    }
}
