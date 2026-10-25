package thaumcraft.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.mirrors.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class MirrorRenderer implements BlockEntityRenderer<MirrorEntity,MirrorRenderer.State>{
 public static final class State extends BlockEntityRenderState{boolean active;Direction face;}
 public MirrorRenderer(BlockEntityRendererProvider.Context c){}
 @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(MirrorContent.TYPE.get(),MirrorRenderer::new);}
 public State createRenderState(){return new State();}
 public void extractRenderState(MirrorEntity b,State s,float partial,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay breaking){BlockEntityRenderer.super.extractRenderState(b,s,partial,camera,breaking);s.active=b.supported()&&b.getBlockState().getValue(MirrorBlock.ACTIVE);s.face=b.getBlockState().getValue(MirrorBlock.FACING);}
 public void submit(State s,PoseStack pose,SubmitNodeCollector out,CameraRenderState camera){if(!s.active)return;var points=thaumcraft.visual.PortalGeometry.mirror(s.face);out.submitCustomGeometry(pose,RenderTypes.endPortal(),(transform,b)->{for(var p:points)b.addVertex(transform.pose(),(float)p.x,(float)p.y,(float)p.z);});}
}
