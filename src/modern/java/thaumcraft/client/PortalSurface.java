package thaumcraft.client;
import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
/** Ordinary textured geometry when TC's advanced portal effect is disabled. */
public final class PortalSurface {
 public static final Identifier TEXTURE=Identifier.parse("minecraft:textures/entity/end_portal/end_portal.png");
 private PortalSurface(){}
 public static void submit(PoseStack pose,SubmitNodeCollector out,List<Vec3[]> faces,boolean advanced){
  if(advanced){out.submitCustomGeometry(pose,RenderTypes.endPortal(),(transform,b)->{for(var face:faces)for(var p:face)b.addVertex(transform.pose(),(float)p.x,(float)p.y,(float)p.z);});return;}
  out.submitCustomGeometry(pose,RenderTypes.entitySolid(TEXTURE),(transform,b)->{for(var face:faces){var n=face[1].subtract(face[0]).cross(face[2].subtract(face[0])).normalize();for(int i=0;i<4;i++){var p=face[i];b.addVertex(transform.pose(),(float)p.x,(float)p.y,(float)p.z).setColor(0xffffffff).setUv(i<2?0:1,i==0||i==3?0:1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xf000f0).setNormal(transform,(float)n.x,(float)n.y,(float)n.z);}}});
 }
}
