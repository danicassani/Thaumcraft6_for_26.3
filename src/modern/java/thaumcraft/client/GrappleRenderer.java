package thaumcraft.client;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.thaumaturgy.*;

@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class GrappleRenderer extends EntityRenderer<GrappleProjectile,GrappleRenderer.State> {
 private static final Identifier HEAD=Identifier.parse("thaumcraft:textures/entity/grappler.png"),ROPE=Identifier.parse("thaumcraft:textures/misc/rope.png");
 private static final ModelLayerLocation LAYER=new ModelLayerLocation(Identifier.parse("thaumcraft:grapple"),"main");
 private final Model<Void> model;
 public GrappleRenderer(EntityRendererProvider.Context c){super(c);model=new Model<>(c.bakeLayer(LAYER),RenderTypes::entityCutout){public void setupAnim(Void ignored){}};}
 @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(LAYER,GrappleRenderer::geometry);}
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(ThaumaturgyContent.GRAPPLE.get(),GrappleRenderer::new);}
 public static final class State extends EntityRenderState {public Vec3 handOffset;public boolean anchored;public float anchorAge,yaw,pitch;public int flightAge;}
 public State createRenderState(){return new State();}
 protected AABB getBoundingBoxForCulling(GrappleProjectile g,float partial){var owner=g.getOwner();return owner==null?super.getBoundingBoxForCulling(g,partial):new AABB(g.position(),owner.getEyePosition()).inflate(.6);}
 public void extractRenderState(GrappleProjectile g,State s,float partial){super.extractRenderState(g,s,partial);s.yaw=Mth.rotLerp(partial,g.yRotO,g.getYRot());s.pitch=Mth.lerp(partial,g.xRotO,g.getXRot());s.anchored=g.anchored();s.anchorAge=g.anchorAge(partial);s.flightAge=g.flightAge();s.handOffset=null;
  if(g.getOwner() instanceof Player p){var gun=p.getItemInHand(g.hand());if(gun.getItem() instanceof GrappleGun&&g.getUUID().equals(gun.get(ThaumaturgyContent.GRAPPLE_LINK.get())))s.handOffset=handPosition(p,g.hand(),partial).subtract(g.getPosition(partial));}
 }
 /** Same camera/arm conventions as the native fishing line, adjusted to the grapple muzzle. */
 public Vec3 handPosition(Player p,InteractionHand hand,float partial){int sign=(p.getMainArm()==HumanoidArm.RIGHT?1:-1)*(hand==InteractionHand.MAIN_HAND?1:-1);var mc=Minecraft.getInstance();
  if(p==mc.player&&mc.options.getCameraType().isFirstPerson()){float fov=mc.options.fov().get();float swing=(float)Math.sin(Math.sqrt(p.getSwingAnimation(partial))*Math.PI);var offset=entityRenderDispatcher.camera.getNearPlane(fov).getPointOnPlane(sign*.4F*(16F/9)*mc.getWindow().getHeight()/mc.getWindow().getWidth(),-.66F).scale(1568D/fov).yRot(swing*.5F).xRot(-swing*.7F);return p.getEyePosition(partial).add(offset);}
  float yaw=Mth.lerp(partial,p.yBodyRotO,p.yBodyRot)*(float)Math.PI/180,scale=p.getScale();double side=sign*.375*scale,forward=.483*scale;return p.getEyePosition(partial).add(-Math.cos(yaw)*side-Math.sin(yaw)*forward,(p.isCrouching()?-.1875:0)-1.024*scale,-Math.sin(yaw)*side+Math.cos(yaw)*forward);
 }
 public void submit(State s,PoseStack pose,SubmitNodeCollector out,CameraRenderState camera){super.submit(s,pose,out,camera);pose.pushPose();pose.rotateDegrees(Axis.YP,s.yaw-90);pose.rotateDegrees(Axis.ZP,s.pitch);out.submitModel(model,null,pose,RenderTypes.entityCutout(HEAD),s.lightCoords,OverlayTexture.NO_OVERLAY,s.outlineColor);pose.popPose();if(s.handOffset==null)return;
  var points=GrappleRope.points(s.handOffset,s.ageInTicks,s.anchored,s.anchorAge,s.flightAge);if(points.size()<2)return;var direction=s.handOffset.normalize();var side=direction.cross(new Vec3(0,1,0));if(side.lengthSqr()<.00001)side=new Vec3(1,0,0);side=side.normalize().scale(GrappleRope.RADIUS);var vertical=direction.cross(side).normalize().scale(GrappleRope.RADIUS);var axes=List.of(side,vertical,side.scale(-1),vertical.scale(-1));int light=s.lightCoords;
  out.submitCustomGeometry(pose,RenderTypes.entityCutout(ROPE),(transform,buffer)->{for(int n=1;n<points.size();n++){var a=points.get(n-1);var b=points.get(n);for(int face=0;face<4;face++){var v1=axes.get(face);var v2=axes.get((face+1)%4);var normal=v1.add(v2).normalize();var vertices=List.of(a.add(v1),a.add(v2),b.add(v2),b.add(v1));for(int i=0;i<4;i++){var v=vertices.get(i);buffer.addVertex(transform.pose(),(float)v.x,(float)v.y,(float)v.z).setColor(-1).setUv(i==0||i==3?face/4F:(face+1)/4F,i<2?0:1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(transform,(float)normal.x,(float)normal.y,(float)normal.z);}}}});
 }
 /** Original ModelGrappler boxes and UVs. */
 private static LayerDefinition geometry(){var mesh=new MeshDefinition();var root=mesh.getRoot();root.addOrReplaceChild("core",CubeListBuilder.create().texOffs(0,0).addBox(-1.5F,-1.5F,-1.5F,3,3,3),PartPose.ZERO);for(int i=0;i<3;i++)root.addOrReplaceChild("prong"+i,CubeListBuilder.create().texOffs(0,10).addBox(-.5F,-.5F,-2.5F,1,1,5),PartPose.rotation(i==2?(float)Math.PI/2:0,i>0?(float)Math.PI/2:0,0));return LayerDefinition.create(mesh,64,32);}
}
