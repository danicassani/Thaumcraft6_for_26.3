package thaumcraft.client;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import thaumcraft.bore.*;
/** Original TC6 boxes, UV coordinates and texture. */
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class BoreRenderer extends MobRenderer<ArcaneBore,LivingEntityRenderState,BoreRenderer.Model> {
 static final ModelLayerLocation LAYER=new ModelLayerLocation(Identifier.parse("thaumcraft:arcane_bore"),"main");
 public BoreRenderer(EntityRendererProvider.Context c){super(c,new Model(c.bakeLayer(LAYER)),.45F);}
 @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(LAYER,BoreRenderer::geometry);}
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(BoreContent.TYPE.get(),BoreRenderer::new);}
 public LivingEntityRenderState createRenderState(){return new LivingEntityRenderState();}
 public Identifier getTextureLocation(LivingEntityRenderState s){return Identifier.parse("thaumcraft:textures/entity/arcanebore.png");}
 public void extractRenderState(ArcaneBore b,LivingEntityRenderState s,float p){super.extractRenderState(b,s,p);s.bodyRot=b.facing().getAxis()==net.minecraft.core.Direction.Axis.Y?0:b.facing().toYRot();s.yRot=0;s.xRot=b.facing()==net.minecraft.core.Direction.UP?-90:0;}
 static LayerDefinition geometry(){var mesh=new MeshDefinition();var root=mesh.getRoot();for(int i=0;i<4;i++)root.addOrReplaceChild("leg"+i,CubeListBuilder.create().texOffs(20,10).addBox(-1,1,-1,2,13,2),PartPose.offsetAndRotation(0,12,0,.5235988F,i*(float)Math.PI/2,0));root.addOrReplaceChild("tripod",CubeListBuilder.create().texOffs(13,0).addBox(-1.5F,0,-1.5F,3,2,3),PartPose.offset(0,12,0));var base=root.addOrReplaceChild("base",CubeListBuilder.create().texOffs(32,0).addBox(-3,-6,-3,6,6,6),PartPose.offset(0,13,0));base.addOrReplaceChild("crystal",CubeListBuilder.create().texOffs(32,25).addBox(-1,-4,5,2,2,2),PartPose.ZERO);base.addOrReplaceChild("domebase",CubeListBuilder.create().texOffs(32,19).addBox(-2,-5,3,4,4,1),PartPose.ZERO);base.addOrReplaceChild("dome",CubeListBuilder.create().texOffs(44,16).addBox(-2,-5,4,4,4,4),PartPose.ZERO);base.addOrReplaceChild("magbase",CubeListBuilder.create().texOffs(0,18).mirror().addBox(-1,-4,-6,2,2,3),PartPose.ZERO);base.addOrReplaceChild("tip",CubeListBuilder.create().texOffs(0,9).mirror().addBox(-1.5F,0,-1.5F,3,3,3),PartPose.offsetAndRotation(0,-3,-6,-(float)Math.PI/2,0,0));return LayerDefinition.create(mesh,64,32);}
 static final class Model extends EntityModel<LivingEntityRenderState>{Model(ModelPart root){super(root);}public void setupAnim(LivingEntityRenderState s){super.setupAnim(s);root().getChild("base").xRot=s.xRot*(float)Math.PI/180;}}
}
