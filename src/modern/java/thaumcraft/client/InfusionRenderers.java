package thaumcraft.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.infusion.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class InfusionRenderers{
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(InfusionContent.PEDESTAL_TYPE.get(),PedestalRenderer::new);e.registerBlockEntityRenderer(InfusionContent.MATRIX_TYPE.get(),MatrixRenderer::new);}
    public static final class State extends BlockEntityRenderState{final ItemStackRenderState item=new ItemStackRenderState();float time;boolean active;}
    public static final class PedestalRenderer implements BlockEntityRenderer<InfusionPedestal,State>{
        private final ItemModelResolver resolver;public PedestalRenderer(BlockEntityRendererProvider.Context c){resolver=c.itemModelResolver();}
        @Override public State createRenderState(){return new State();}
        @Override public void extractRenderState(InfusionPedestal t,State s,float p,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay breaking){BlockEntityRenderer.super.extractRenderState(t,s,p,camera,breaking);s.time=t.getLevel()==null?0:t.getLevel().getGameTime()+p;resolver.updateForTopItem(s.item,t.item(),ItemDisplayContext.GROUND,t.getLevel(),null,t.getBlockPos().hashCode());}
        @Override public void submit(State s,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera){pose.pushPose();pose.translate(.5F,1.15F+(float)Math.sin(s.time/10)*.025F,.5F);pose.rotateDegrees(Axis.YP,s.time*2);pose.scale(.65F,.65F,.65F);s.item.submit(pose,collector,s.lightCoords,OverlayTexture.NO_OVERLAY,0);pose.popPose();}
    }
    public static final class MatrixRenderer implements BlockEntityRenderer<InfusionMatrix,State>{
        private final ItemModelResolver resolver;public MatrixRenderer(BlockEntityRendererProvider.Context c){resolver=c.itemModelResolver();}
        @Override public State createRenderState(){return new State();}
        @Override public void extractRenderState(InfusionMatrix t,State s,float p,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay breaking){BlockEntityRenderer.super.extractRenderState(t,s,p,camera,breaking);s.time=t.getLevel()==null?0:t.getLevel().getGameTime()+p;s.active=t.getBlockState().getValue(InfusionBlock.ACTIVE);resolver.updateForTopItem(s.item,new ItemStack(InfusionContent.MATRIX.get()),ItemDisplayContext.NONE,t.getLevel(),null,0);}
        @Override public void submit(State s,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera){pose.pushPose();pose.translate(.5F,.5F,.5F);if(s.active){pose.rotateDegrees(Axis.YP,s.time*.7F);pose.rotateDegrees(Axis.XP,(float)Math.sin(s.time/50)*10);}s.item.submit(pose,collector,s.lightCoords,OverlayTexture.NO_OVERLAY,0);pose.popPose();}
    }
}
