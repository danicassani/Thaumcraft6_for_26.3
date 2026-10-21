package thaumcraft.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.rifts.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class RiftEventRenderers{
 private static final Identifier PARTICLES=Identifier.parse("thaumcraft:textures/misc/particles.png"),NODES=Identifier.parse("thaumcraft:textures/misc/auranodes.png");
 @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(RiftContent.WISP.get(),Orb::new);e.registerEntityRenderer(RiftContent.CLOUD.get(),Cloud::new);}
 static void quad(PoseStack p,SubmitNodeCollector out,Identifier texture,int cells,int frame,float half,int color){float u=(frame%cells)/(float)cells,v=(frame/cells)/(float)cells,step=1F/cells;out.submitCustomGeometry(p,RenderTypes.entityTranslucentEmissive(texture),(pose,b)->{float[][] verts={{-half,-half,u,v+step},{half,-half,u+step,v+step},{half,half,u+step,v},{-half,half,u,v}};for(var a:verts)b.addVertex(pose.pose(),a[0],a[1],0).setColor(color).setUv(a[2],a[3]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose,0,0,1);});}
 public static final class State extends EntityRenderState{int color,flash;Vec3 end;}
 public static final class Orb extends EntityRenderer<Wisp,State>{
  public Orb(EntityRendererProvider.Context c){super(c);shadowRadius=0;}
  public State createRenderState(){return new State();}
  public void extractRenderState(Wisp e,State s,float t){super.extractRenderState(e,s,t);s.color=thaumcraft.crystals.CrystalAspects.COLORS.getOrDefault(e.aspect(),0xffffff);s.flash=e.zapTicks();var target=e.zapTarget();s.end=target==null?null:target.position().add(0,target.getBbHeight()*.5,0).subtract(e.position());}
  public void submit(State s,PoseStack p,SubmitNodeCollector out,CameraRenderState c){super.submit(s,p,out,c);if(s.isInvisible)return;int frame=(int)s.ageInTicks%16;p.pushPose();p.translate(0,.45,0);p.rotate(c.orientation);quad(p,out,NODES,32,800+frame,.75F,0x80000000|s.color);quad(p,out,PARTICLES,64,320+frame,.75F,0x40ffffff);quad(p,out,PARTICLES,64,512+frame,.4F,0xffffffff);p.popPose();if(s.flash>0&&s.end!=null){var from=new Vec3(0,.45,0);var direction=s.end.subtract(from);var side=direction.cross(new Vec3(0,1,0)).normalize().scale(.025);if(side.lengthSqr()==0)side=new Vec3(.025,0,0);final var width=side;out.submitCustomGeometry(p,RenderTypes.lightning(),(pose,b)->{Vec3 previous=from;for(int i=1;i<=8;i++){var next=from.add(direction.scale(i/8D));if(i<8)next=next.add(Math.sin(i*13+s.ageInTicks)*.08,Math.cos(i*7+s.ageInTicks)*.08,0);for(var point:new Vec3[]{previous.subtract(width),previous.add(width),next.add(width),next.subtract(width)})b.addVertex(pose.pose(),(float)point.x,(float)point.y,(float)point.z).setColor(0xe0ffffff);previous=next;}});}}
 }
 public static final class CloudState extends EntityRenderState{int radius;}
 public static final class Cloud extends EntityRenderer<RiftCloud,CloudState>{
  public Cloud(EntityRendererProvider.Context c){super(c);shadowRadius=0;}
  protected net.minecraft.world.phys.AABB getBoundingBoxForCulling(RiftCloud e){return e.getBoundingBox().inflate(e.radius());}
  public CloudState createRenderState(){return new CloudState();}
  public void extractRenderState(RiftCloud e,CloudState s,float t){super.extractRenderState(e,s,t);s.radius=e.radius();}
  public void submit(CloudState s,PoseStack p,SubmitNodeCollector out,CameraRenderState c){super.submit(s,p,out,c);if(s.isInvisible)return;for(int i=0;i<12*s.radius;i++){double a=i*2.399963+s.ageInTicks*.013,r=s.radius*Math.sqrt((i+.5)/(12*s.radius))*.85;p.pushPose();p.translate(Math.cos(a)*r,Math.sin(i*3.3+s.ageInTicks*.02)*s.radius*.45,Math.sin(a)*r);p.rotate(c.orientation);quad(p,out,NODES,32,800+((int)s.ageInTicks+i)%16,.5F,0x44800080);p.popPose();}}
 }
}
