package thaumcraft.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.*;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import thaumcraft.storage.HungryChest;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class HungryChestRenderer extends ChestRenderer<HungryChest> {
 private final ChestModel model;private final SpriteGetter sprites;
 private static final SpriteId TEXTURE=new SpriteId(Identifier.parse("minecraft:textures/atlas/chest.png"),Identifier.parse("thaumcraft:entity/chest/hungry"));
 public HungryChestRenderer(BlockEntityRendererProvider.Context context){super(context);sprites=context.sprites();model=new ChestModel(context.bakeLayer(ModelLayers.CHEST));}
 @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(HungryChest.TYPE.get(),HungryChestRenderer::new);}
 public void submit(ChestRenderState s,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera){pose.pushPose();pose.mulPose(modelTransformation(s.facing));float open=1-s.open;open=1-open*open*open;collector.submitModel(model,open,pose,s.lightCoords,OverlayTexture.NO_OVERLAY,-1,TEXTURE,sprites,0);if(s.breakProgress!=null)collector.order(1).submitCrumblingOverlay(model,open,pose,TEXTURE.renderType(model.renderType()),s.lightCoords,OverlayTexture.NO_OVERLAY,-1,s.breakProgress);pose.popPose();}
}
