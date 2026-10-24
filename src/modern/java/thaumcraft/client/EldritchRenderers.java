package thaumcraft.client;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import thaumcraft.eldritch.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class EldritchRenderers {
 static final ModelLayerLocation CRAB=layer("eldritch_crab"),GOLEM=layer("eldritch_golem"),ZOMBIE=layer("brainy_zombie");
 static ModelLayerLocation layer(String n){return new ModelLayerLocation(Identifier.parse("thaumcraft:"+n),"main");}
 @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(CRAB,EldritchGeometry::crab);e.registerLayerDefinition(GOLEM,EldritchGeometry::golem);e.registerLayerDefinition(ZOMBIE,()->LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE,0),64,64));}
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){EldritchContent.TYPES.forEach((k,t)->{if(k==EldritchMob.Kind.LEADER)e.registerEntityRenderer(t.get(),Leader::new);else if(k==EldritchMob.Kind.PORTAL)e.registerEntityRenderer(t.get(),Portal::new);else e.registerEntityRenderer(t.get(),c->new Creature(c,k));});EldritchContent.ZOMBIES.forEach((k,t)->e.registerEntityRenderer(t.get(),c->new Brain(c,k)));e.registerEntityRenderer(EldritchContent.FIREBAT.get(),Fire::new);}
 public static final class State extends LivingEntityRenderState{EldritchMob.Kind kind;int phase;}
 static final class Model extends EntityModel<State>{final EldritchMob.Kind kind;Model(ModelPart root,EldritchMob.Kind k){super(root);kind=k;}
  public void setupAnim(State s){super.setupAnim(s);var root=root();float stride=(float)Math.sin(s.walkAnimationPos)*s.walkAnimationSpeed;
   if(kind==EldritchMob.Kind.CRAB){root.getChild("TailHelm").visible=s.phase==1;root.getChild("TailBare").visible=s.phase==0;for(String n:new String[]{"RFLeg0","RFLeg1","RRLeg0","RRLeg1","LFLeg0","LFLeg1","LRLeg0","LRLeg1"})root.getChild(n).zRot+=stride*.3F;}
   else if(kind==EldritchMob.Kind.GOLEM){root.getChild("Head").visible=s.phase==0;root.getChild("Head2").visible=s.phase==1;root.getChild("ArmR").xRot=stride;root.getChild("ArmL").xRot=-stride;root.getChild("LegR").xRot=-stride;root.getChild("LegL").xRot=stride;}
   else if(kind==EldritchMob.Kind.WARDEN){root.getChild("Hood1").yRot=s.yRot*(float)Math.PI/180;root.getChild("ArmL1").xRot=-1;root.getChild("ArmR1").xRot=-1;}
   else {root.getChild("head").yRot=s.yRot*(float)Math.PI/180;root.getChild("head").xRot=s.xRot*(float)Math.PI/180;root.getChild("right_leg").xRot=stride;root.getChild("left_leg").xRot=-stride;root.getChild("right_arm").xRot=-1.4F;root.getChild("left_arm").xRot=-1.4F;if(kind==EldritchMob.Kind.BRAINY||kind==EldritchMob.Kind.GIANT){root.getChild("head").xScale=1.5F;root.getChild("head").yScale=1.5F;root.getChild("head").zScale=1.5F;}}
  }
 }
 static final class Creature extends MobRenderer<EldritchMob,State,Model>{final EldritchMob.Kind kind;
  Creature(EntityRendererProvider.Context c,EldritchMob.Kind k){super(c,new Model(c.bakeLayer(k==EldritchMob.Kind.CRAB?CRAB:k==EldritchMob.Kind.GOLEM?GOLEM:k==EldritchMob.Kind.WARDEN?WarpRenderers.GUARDIAN:ZOMBIE),k),.5F);kind=k;}
  public State createRenderState(){return new State();}public void extractRenderState(EldritchMob e,State s,float p){super.extractRenderState(e,s,p);s.kind=e.kind;s.phase=e.phase();if(kind==EldritchMob.Kind.GOLEM)s.scale*=1.7F;if(kind==EldritchMob.Kind.LEADER)s.scale*=1.2F;}
  public Identifier getTextureLocation(State s){return Identifier.parse("thaumcraft:textures/entity/"+switch(kind){case BRAINY,GIANT->"bzombie";case INHABITED->"czombie";case CRAB->"crab";case GOLEM->"eldritch_golem";case WARDEN->"eldritch_warden";default->"cultist";}+".png");}
 }
 static final class Brain extends ZombieRenderer {
  final EldritchMob.Kind kind;Brain(EntityRendererProvider.Context c,EldritchMob.Kind k){super(c);kind=k;}
  public Identifier getTextureLocation(ZombieRenderState s){return Identifier.parse("thaumcraft:textures/entity/"+(kind==EldritchMob.Kind.INHABITED?"czombie":"bzombie")+".png");}
  public void extractRenderState(net.minecraft.world.entity.monster.zombie.Zombie e,ZombieRenderState s,float t){super.extractRenderState(e,s,t);}
 }
 static final class Leader extends HumanoidMobRenderer<EldritchMob,HumanoidRenderState,HumanoidModel<HumanoidRenderState>> {
  Leader(EntityRendererProvider.Context c){super(c,new HumanoidModel<>(c.bakeLayer(WarpRenderers.CULTIST)),.5F);addLayer(new net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer<>(this,ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR,c.getModelSet(),HumanoidModel<HumanoidRenderState>::new),c.getEquipmentRenderer()));}
  public HumanoidRenderState createRenderState(){return new HumanoidRenderState();}public Identifier getTextureLocation(HumanoidRenderState s){return Identifier.parse("thaumcraft:textures/entity/cultist.png");}
  public void extractRenderState(EldritchMob e,HumanoidRenderState s,float t){super.extractRenderState(e,s,t);s.scale*=1.2F;}
 }
 static final class Fire extends BatRenderer {Fire(EntityRendererProvider.Context c){super(c);}public Identifier getTextureLocation(BatRenderState s){return Identifier.parse("thaumcraft:textures/entity/firebat.png");}}
 static final class Portal extends EntityRenderer<EldritchMob,WarpRenderers.PortalState>{Portal(EntityRendererProvider.Context c){super(c);}public WarpRenderers.PortalState createRenderState(){return new WarpRenderers.PortalState();}public void extractRenderState(EldritchMob e,WarpRenderers.PortalState s,float p){super.extractRenderState(e,s,p);s.active=true;s.opening=Math.min(1,s.ageInTicks/50);s.health=e.getHealth()/e.getMaxHealth();}public void submit(WarpRenderers.PortalState s,PoseStack p,SubmitNodeCollector out,CameraRenderState c){super.submit(s,p,out,c);p.pushPose();p.translate(0,1.5,0);p.rotate(c.orientation);p.scale(2*s.opening,2*s.opening,2*s.opening);float u=(15-(int)s.ageInTicks%16)/16F;out.submitCustomGeometry(p,net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucentEmissive(Identifier.parse("thaumcraft:textures/misc/cultist_portal.png")),(pose,buffer)->{float[][] v={{-1,-1,u,1},{1,-1,u+1F/16,1},{1,1,u+1F/16,0},{-1,1,u,0}};for(var vertex:v)buffer.addVertex(pose.pose(),vertex[0],vertex[1],0).setColor(-1).setUv(vertex[2],vertex[3]).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(0xf000f0).setNormal(pose,0,0,1);});p.popPose();}}
}
