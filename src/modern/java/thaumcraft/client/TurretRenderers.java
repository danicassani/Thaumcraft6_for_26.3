package thaumcraft.client;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import thaumcraft.thaumaturgy.*;
/** The original twenty-piece model and the original eight independently animated OBJ groups. */
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class TurretRenderers {
 static final ModelLayerLocation LAYER=new ModelLayerLocation(Identifier.parse("thaumcraft:turret_basic"),"main");
 @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(LAYER,TurretRenderers::geometry);}
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(TurretContent.BASIC.get(),Basic::new);e.registerEntityRenderer(TurretContent.ADVANCED.get(),Advanced::new);e.registerEntityRenderer(TurretContent.SPECTRAL.get(),SpectralArrowRenderer::new);}
 public static final class State extends LivingEntityRenderState {public float shot,load,yaw,pitch;public boolean cart;}
 static float f(JsonObject j,String key,int i){return j.getAsJsonArray(key).get(i).getAsFloat();}
 static LayerDefinition geometry(){var mesh=new MeshDefinition();var root=mesh.getRoot();try(var reader=Minecraft.getInstance().getResourceManager().openAsReader(Identifier.parse("thaumcraft:models/turrets/basic.json"))){var parts=JsonParser.parseReader(reader).getAsJsonObject();for(var entry:parts.entrySet()){var j=entry.getValue().getAsJsonObject();var parent=j.get("parent").getAsString().equals("root")?root:root.getChild("crossbow");parent.addOrReplaceChild(entry.getKey(),CubeListBuilder.create().texOffs((int)f(j,"uv",0),(int)f(j,"uv",1)).mirror().addBox(f(j,"box",0),f(j,"box",1),f(j,"box",2),f(j,"box",3),f(j,"box",4),f(j,"box",5)),PartPose.offsetAndRotation(f(j,"pos",0),f(j,"pos",1),f(j,"pos",2),f(j,"rot",0),f(j,"rot",1),f(j,"rot",2)));}}catch(java.io.IOException e){throw new IllegalStateException("Missing original crossbow model",e);}return LayerDefinition.create(mesh,64,32);}
 public static final class Basic extends MobRenderer<CrossbowTurret,State,BasicModel>{
  public Basic(EntityRendererProvider.Context c){super(c,new BasicModel(c.bakeLayer(LAYER)),.5F);}
  public State createRenderState(){return new State();}public Identifier getTextureLocation(State s){return Identifier.parse("thaumcraft:textures/entity/crossbow.png");}
  public void extractRenderState(CrossbowTurret b,State s,float p){super.extractRenderState(b,s,p);s.bodyRot=0;s.yRot=0;s.xRot=b.getXRot();s.yaw=Mth.rotLerp(p,b.yRotO,b.getYRot());s.shot=b.shotProgress(p);s.load=b.loadProgress(p);s.cart=b.isPassenger()&&b.getVehicle() instanceof net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;}
 }
 public static final class BasicModel extends EntityModel<State>{
  public BasicModel(ModelPart root){super(root);}
  public void setupAnim(State s){super.setupAnim(s);var bow=root().getChild("crossbow");bow.yRot=s.yaw*(float)Math.PI/180;bow.xRot=s.xRot*(float)Math.PI/180;float wave=(float)Math.sin(Math.sqrt(s.shot)*Math.PI*2)*.2F;for(int i=1;i<=3;i++){bow.getChild("crossl"+i).yRot=-.2F+wave;bow.getChild("crossr"+i).yRot=.2F-wave;}float load=-.5F+(float)Math.sin(Math.sqrt(s.load)*Math.PI*2)*.5F;for(var n:new String[]{"loadbarcross","loadbarl","loadbarr"})bow.getChild(n).xRot=load;for(int i=1;i<=4;i++){var leg=root().getChild("leg"+i);leg.y=s.cart?4:12;leg.xRot=s.cart?.1F:.5F;}}
 }
 public static final class Advanced extends EntityRenderer<CrossbowTurret,State>{
  private final java.util.Map<String,float[][][]> groups;
  public Advanced(EntityRendererProvider.Context c){super(c);groups=TurretGeometry.load(c.getResourceManager()).get("advanced");}
  public State createRenderState(){return new State();}
  public void extractRenderState(CrossbowTurret b,State s,float p){super.extractRenderState(b,s,p);s.yaw=Mth.rotLerp(p,b.yRotO,b.getYRot());s.pitch=Mth.lerp(p,b.xRotO,b.getXRot());s.shot=b.shotProgress(p);s.load=b.loadProgress(p);s.cart=b.isPassenger()&&b.getVehicle() instanceof net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;}
  private void part(String n,PoseStack pose,SubmitNodeCollector out,State s){TurretGeometry.draw(groups.get(n),pose,out,Identifier.parse("thaumcraft:textures/entity/crossbow_advanced.png"),s.lightCoords,-1);}
  public void submit(State s,PoseStack pose,SubmitNodeCollector out,CameraRenderState camera){super.submit(s,pose,out,camera);pose.pushPose();pose.translate(0,.75,0);pose.pushPose();if(s.cart)pose.scale(.66F,.75F,.66F);part("legs",pose,out,s);pose.popPose();pose.rotateDegrees(Axis.YP,-s.yaw);pose.rotateDegrees(Axis.XP,s.pitch);for(var n:new String[]{"mech","box","shield","brain"})part(n,pose,out,s);pose.pushPose();pose.translate(0,0,Math.sin(Math.sqrt(s.load)*Math.PI*2)/12);part("loader",pose,out,s);pose.popPose();pose.translate(0,0,.375);float wave=(float)Math.sin(Math.sqrt(s.shot)*Math.PI*2)*20;pose.pushPose();pose.rotateDegrees(Axis.YP,wave);part("bow1",pose,out,s);pose.popPose();pose.rotateDegrees(Axis.YP,-wave);part("bow2",pose,out,s);pose.popPose();}
 }
}
