package thaumcraft.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.taint.*;

@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class TaintRenderer extends EntityRenderer<TaintedMob,TaintRenderer.State>{
 public static final class State extends EntityRenderState{TaintedMob.Kind kind;float yaw;}
 public TaintRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.4F;}
 @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){for(var type:TaintContent.MOBS.values())e.registerEntityRenderer(type.get(),TaintRenderer::new);e.registerEntityRenderer(TaintContent.BOTTLE_ENTITY.get(),ThrownItemRenderer::new);e.registerEntityRenderer(TaintContent.SLIME.get(),ThaumicSlimeRenderer::new);}
 private static Identifier id(String name){return Identifier.parse("thaumcraft:textures/entity/"+name+".png");}
 public State createRenderState(){return new State();}
 public void extractRenderState(TaintedMob e,State s,float t){super.extractRenderState(e,s,t);s.kind=e.kind;s.yaw=e.getYRot();}
 public void submit(State s,PoseStack p,SubmitNodeCollector out,CameraRenderState camera){super.submit(s,p,out,camera);if(s.isInvisible)return;p.pushPose();p.rotateDegrees(Axis.YP,180-s.yaw);
  if(s.kind==TaintedMob.Kind.SWARM){for(int i=0;i<24;i++){p.pushPose();double a=s.ageInTicks*.12+i*2.4;p.translate(Math.sin(a)*.6,1+Math.sin(a*.7+i)*.6,Math.cos(a)*.6);box(p,out,id("crawler"),s.lightCoords,-.04F,-.025F,-.06F,.08F,.05F,.12F,0,0,32,32);p.popPose();}}
  else if(s.kind==TaintedMob.Kind.CRAWLER){int[][] dims={{4,2,2},{6,4,3},{3,3,3},{3,4,4},{2,3,3},{2,2,3},{1,1,2}};float z=-.4F;for(int i=0;i<7;i++){var d=dims[i];p.pushPose();p.translate(Math.sin(s.ageInTicks*.3+i)*.015,0,z);p.rotateDegrees(Axis.YP,(float)Math.sin(s.ageInTicks*.3+i)*6);box(p,out,id("crawler"),s.lightCoords,-d[0]/32F,0,0,d[0]/16F,d[1]/16F,d[2]/16F,0,0,64,32);p.popPose();z+=d[2]/16F;}}
  else{boolean seed=s.kind.seed;int segments=seed?8:10;float scale=s.kind.height/(seed?1.6F:2.4F);p.scale(scale,scale,scale);p.translate(0,.25,0);Identifier tex=id(seed?"taintseed":"taintacle");for(int i=0;i<segments;i++){if(i>0){p.translate(0,seed?.32:.36,0);p.rotateDegrees(Axis.XP,(float)Math.sin(s.ageInTicks*.1-i*.5)*(seed?4:10));p.rotateDegrees(Axis.ZP,(float)Math.sin(s.ageInTicks*.07-i*.5)*5);}float width=(seed?(i<4?.5F:.25F):i==segments-1?.75F:.5F)*(float)Math.pow(seed?.86:.88,i);box(p,out,tex,s.lightCoords,-width/2,-width/2,-width/2,width,width,width,0,i==0?0:seed&&i>=4?48:16,64,64);}}
  p.popPose();
 }
 /** Legacy ModelBox UV unfolding, emitted on the native submit pipeline. */
 private static void box(PoseStack p,SubmitNodeCollector out,Identifier tex,int light,float x,float y,float z,float w,float h,float d,int u,int v,int tw,int th){float[][] corners={{x,y,z},{x+w,y,z},{x+w,y+h,z},{x,y+h,z},{x,y,z+d},{x+w,y,z+d},{x+w,y+h,z+d},{x,y+h,z+d}};int[][] faces={{1,0,3,2},{4,5,6,7},{0,4,7,3},{5,1,2,6},{3,7,6,2},{0,1,5,4}};float W=w*16,H=h*16,D=d*16;float[][] uv={{u+D,v+D,u+D+W,v+D+H},{u+D+W+D,v+D,u+D+W+D+W,v+D+H},{u,v+D,u+D,v+D+H},{u+D+W,v+D,u+D+W+D,v+D+H},{u+D,v,u+D+W,v+D},{u+D+W,v,u+D+W+W,v+D}};out.submitCustomGeometry(p,RenderTypes.entityCutout(tex),(pose,b)->{for(int f=0;f<6;f++)for(int i=0;i<4;i++){var a=corners[faces[f][i]];var t=uv[f];b.addVertex(pose.pose(),a[0],a[1],a[2]).setColor(-1).setUv((i==0||i==3?t[0]:t[2])/tw,(i<2?t[1]:t[3])/th).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,f==2?-1:f==3?1:0,f==4?1:f==5?-1:0,f==0?-1:f==1?1:0);}});}
}
