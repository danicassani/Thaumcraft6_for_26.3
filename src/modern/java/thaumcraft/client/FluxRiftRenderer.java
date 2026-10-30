package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.rifts.*;

@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class FluxRiftRenderer extends EntityRenderer<FluxRiftEntity,FluxRiftRenderer.State> {
    public static final class State extends EntityRenderState { List<RiftShape.Point> points=List.of();boolean collapse,advanced;float stability; }
    public FluxRiftRenderer(EntityRendererProvider.Context context){super(context);}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers event){event.registerEntityRenderer(RiftContent.RIFT.get(),FluxRiftRenderer::new);}
    @Override protected net.minecraft.world.phys.AABB getBoundingBoxForCulling(FluxRiftEntity r,float partial){return super.getBoundingBoxForCulling(r,partial).inflate(.6);}
    @Override public State createRenderState(){return new State();}
    @Override public void extractRenderState(FluxRiftEntity entity,State state,float partial){super.extractRenderState(entity,state,partial);state.points=entity.shape();state.collapse=entity.collapsing();state.stability=entity.stability();state.advanced=ClientGraphics.advancedPortals();}
    @Override public void submit(State state,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera){
        super.submit(state,pose,collector,camera);
        var core=thaumcraft.visual.PortalGeometry.rift(state.points,state.ageInTicks,state.stability,1);
        PortalSurface.submit(pose,collector,core,state.advanced);
        // The original had three enlarged, additive star layers. Native emissive translucency
        // retains their silhouette and movement while respecting the modern depth buffer.
        for(int layer=0;layer<3;layer++){
            var shell=thaumcraft.visual.PortalGeometry.rift(state.points,state.ageInTicks,state.stability,1.25F+.5F*layer);
            int color=((24-layer*6)<<24)|(state.collapse?0xff6688:0x9874df);
            collector.submitCustomGeometry(pose,RenderTypes.entityTranslucentEmissive(PortalSurface.TEXTURE),(transform,buffer)->{for(var face:shell){var normal=face[1].subtract(face[0]).cross(face[2].subtract(face[0])).normalize();for(int i=0;i<4;i++){var p=face[i];buffer.addVertex(transform.pose(),(float)p.x,(float)p.y,(float)p.z).setColor(color).setUv(i<2?0:1,i==0||i==3?0:1).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(0x00f000f0).setNormal(transform,(float)normal.x,(float)normal.y,(float)normal.z);}}});
        }
    }
}
