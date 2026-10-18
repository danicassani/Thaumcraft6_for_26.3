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
    public static final class State extends EntityRenderState { List<RiftShape.Point> points=List.of();boolean collapse;float stability; }
    public FluxRiftRenderer(EntityRendererProvider.Context context){super(context);}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers event){event.registerEntityRenderer(RiftContent.RIFT.get(),FluxRiftRenderer::new);}
    @Override public State createRenderState(){return new State();}
    @Override public void extractRenderState(FluxRiftEntity entity,State state,float partial){super.extractRenderState(entity,state,partial);state.points=entity.shape();state.collapse=entity.collapsing();state.stability=entity.stability();}
    @Override public void submit(State state,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera){
        super.submit(state,pose,collector,camera);
        var points=state.points;float age=state.ageInTicks;
        collector.submitCustomGeometry(pose,RenderTypes.debugQuads(),(transform,buffer)->{
            // A dark eight-sided core with thin bright seams follows the seeded TC6 path.
            for(int i=0;i<points.size()-1;i++) {
                var a=points.get(i);var b=points.get(i+1);var tangent=b.position().subtract(a.position()).normalize();
                var u=tangent.cross(Math.abs(tangent.y)<.9?new Vec3(0,1,0):new Vec3(1,0,0)).normalize();var v=tangent.cross(u).normalize();
                float pulse=1+(float)Math.sin(age*.08+i*.7)*Math.max(0,-state.stability)/1600;
                for(int side=0;side<8;side++) {
                    double angle=side*Math.PI/4,next=(side+1)*Math.PI/4;
                    var p=u.scale(Math.cos(angle)).add(v.scale(Math.sin(angle)));var q=u.scale(Math.cos(next)).add(v.scale(Math.sin(next)));
                    Vec3[] vertices={a.position().add(p.scale(a.width()*pulse)),a.position().add(q.scale(a.width()*pulse)),b.position().add(q.scale(b.width()*pulse)),b.position().add(p.scale(b.width()*pulse))};
                    for(var point:vertices)buffer.addVertex(transform.pose(),(float)point.x,(float)point.y,(float)point.z).setColor(side==1||side==5?(state.collapse?0xFFCC4466:0xFF8754BF):0xFF080411);
                }
            }
        });
    }
}
