package thaumcraft.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.warp.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class WarpRenderers {
 static final ModelLayerLocation GUARDIAN=new ModelLayerLocation(Identifier.parse("thaumcraft:eldritch_guardian"),"main"),CULTIST=new ModelLayerLocation(Identifier.parse("thaumcraft:cultist"),"main");
 @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(GUARDIAN,WarpGuardianGeometry::layer);e.registerLayerDefinition(CULTIST,()->LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE,0),64,32));}
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(WarpCreatures.SPIDER.get(),Mind::new);e.registerEntityRenderer(WarpCreatures.GUARDIAN.get(),Guardian::new);e.registerEntityRenderer(WarpCreatures.KNIGHT.get(),Cultist::new);e.registerEntityRenderer(WarpCreatures.CLERIC.get(),Cultist::new);e.registerEntityRenderer(WarpCreatures.PORTAL.get(),Portal::new);e.registerEntityRenderer(WarpCreatures.ORB.get(),Orb::new);}
 public static final class Mind extends SpiderRenderer<MindSpider>{
  public Mind(EntityRendererProvider.Context c){super(c);}
  public boolean shouldRender(MindSpider e,Frustum f,double x,double y,double z,float partial){return (!e.harmless()||Minecraft.getInstance().player!=null&&e.viewer().equals(Minecraft.getInstance().player.getUUID().toString()))&&super.shouldRender(e,f,x,y,z,partial);}
  public void extractRenderState(MindSpider e,LivingEntityRenderState s,float t){super.extractRenderState(e,s,t);s.scale=.6F;}
 }
 public static final class GuardianModel extends EntityModel<LivingEntityRenderState>{public GuardianModel(ModelPart p){super(p);}
  private ModelPart part(String path){ModelPart p=root();for(String n:path.split("/"))p=p.getChild(n);return p;}
  private void x(String n,float v){part(n).xRot=v;}private void z(String n,float v){part(n).zRot=v;}
  private float wave(float time,float phase,float period,float amplitude){return (float)Math.sin((time+phase)/period)*amplitude;}
  public void setupAnim(LivingEntityRenderState s){super.setupAnim(s);float t=s.ageInTicks;x("Hood1",s.xRot*(float)Math.PI/180);part("Hood1").yRot=s.yRot*(float)Math.PI/180;x("HoodEye",part("Hood1").xRot);part("HoodEye").yRot=part("Hood1").yRot;
   x("ArmL1",-1+wave(t,20,10,.08F));x("ArmR1",-1+wave(t,0,10,.08F));
   x("LegpanelC1",-.15F+wave(t,0,8,.12F));x("LegpanelC1/LegpanelC2",wave(t,-5,8,.13F));x("LegpanelC1/LegpanelC2/LegpanelC3",wave(t,-10,8,.14F));
   x("Cloak1",.2F+wave(t,0,7,.08F));x("Cloak1/Cloak2",wave(t,-5,7,.1F));x("Cloak1/Cloak2/Cloak3",wave(t,-10,7,.12F));
   z("SidepanelL2",-.2F+wave(t,10,8,.12F));z("SidepanelL2/SidepanelL3",wave(t,5,8,.13F));z("SidepanelL2/SidepanelL3/SidepanelL4",wave(t,0,8,.14F));z("SidepanelR2",.2F+wave(t,-5,8,.12F));z("SidepanelR2/SidepanelR3",wave(t,-10,8,.13F));z("SidepanelR2/SidepanelR3/SidepanelR4",wave(t,-15,8,.14F));
  }
 }
 public static final class Guardian extends MobRenderer<WarpMonster,LivingEntityRenderState,GuardianModel>{
  public Guardian(EntityRendererProvider.Context c){super(c,new GuardianModel(c.bakeLayer(GUARDIAN)),.5F);}
  public LivingEntityRenderState createRenderState(){return new LivingEntityRenderState();}
  public Identifier getTextureLocation(LivingEntityRenderState s){return Identifier.parse("thaumcraft:textures/entity/eldritch_guardian.png");}
 }
 public static final class Cultist extends HumanoidMobRenderer<WarpMonster,HumanoidRenderState,HumanoidModel<HumanoidRenderState>>{
  public Cultist(EntityRendererProvider.Context c){super(c,new HumanoidModel<>(c.bakeLayer(CULTIST)),.5F);}
  public HumanoidRenderState createRenderState(){return new HumanoidRenderState();}
  public Identifier getTextureLocation(HumanoidRenderState s){return Identifier.parse("thaumcraft:textures/entity/cultist.png");}
 }
 public static final class Orb extends EntityRenderer<WarpOrb,EntityRenderState>{
  public Orb(EntityRendererProvider.Context c){super(c);shadowRadius=0;}
  public EntityRenderState createRenderState(){return new EntityRenderState();}
  public void submit(EntityRenderState s,PoseStack p,SubmitNodeCollector out,CameraRenderState c){super.submit(s,p,out,c);if(s.isInvisible)return;p.pushPose();p.rotate(c.orientation);RiftEventRenderers.quad(p,out,Identifier.parse("thaumcraft:textures/misc/particles.png"),64,512+(int)s.ageInTicks%16,.3F,0xff442266);p.popPose();}
 }
 public static final class Portal extends EntityRenderer<WarpMonster,EntityRenderState>{
  public Portal(EntityRendererProvider.Context c){super(c);shadowRadius=0;}
  public EntityRenderState createRenderState(){return new EntityRenderState();}
  public void submit(EntityRenderState s,PoseStack p,SubmitNodeCollector out,CameraRenderState camera){super.submit(s,p,out,camera);if(s.isInvisible)return;p.pushPose();p.translate(0,1.5,0);p.rotate(camera.orientation);float u=((int)s.ageInTicks/2%16)/16F;out.submitCustomGeometry(p,RenderTypes.entityTranslucentEmissive(Identifier.parse("thaumcraft:textures/misc/cultist_portal.png")),(pose,b)->{float[][] vertices={{-1.5F,-1.5F,u,1},{1.5F,-1.5F,u+1F/16,1},{1.5F,1.5F,u+1F/16,0},{-1.5F,1.5F,u,0}};for(var v:vertices)b.addVertex(pose.pose(),v[0],v[1],0).setColor(0xffffffff).setUv(v[2],v[3]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xf000f0).setNormal(pose,0,0,1);});p.popPose();}
 }
}
