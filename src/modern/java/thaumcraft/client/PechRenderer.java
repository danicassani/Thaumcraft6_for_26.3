package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.pech.*;

@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class PechRenderer extends MobRenderer<Pech,PechRenderer.State,PechRenderer.Model> {
    private static final ModelLayerLocation LAYER=new ModelLayerLocation(Identifier.parse("thaumcraft:pech"),"main");
    public static final class State extends ArmedEntityRenderState {public int variant;public float mumble;}
    public PechRenderer(EntityRendererProvider.Context c){super(c,new Model(c.bakeLayer(LAYER)),.4F);addLayer(new ItemInHandLayer<>(this));}
    public State createRenderState(){return new State();}
    public void extractRenderState(Pech pech,State state,float partial){super.extractRenderState(pech,state,partial);ArmedEntityRenderState.extractArmedEntityRenderState(pech,state,itemModelResolver,partial);state.variant=pech.variant();state.mumble=pech.mumble;}
    public Identifier getTextureLocation(State state){return Identifier.parse("thaumcraft:textures/entity/pech_"+(state.variant==1?"thaum":state.variant==2?"stalker":"forage")+".png");}
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(LAYER,Model::layer);}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(PechContent.PECH.get(),PechRenderer::new);e.registerEntityRenderer(PechContent.BOLT.get(),Bolt::new);}
    public static final class Bolt extends EntityRenderer<PechBolt,net.minecraft.client.renderer.entity.state.EntityRenderState>{
        public Bolt(EntityRendererProvider.Context c){super(c);shadowRadius=0;}
        public net.minecraft.client.renderer.entity.state.EntityRenderState createRenderState(){return new net.minecraft.client.renderer.entity.state.EntityRenderState();}
        public void submit(net.minecraft.client.renderer.entity.state.EntityRenderState state,PoseStack pose,net.minecraft.client.renderer.SubmitNodeCollector out,net.minecraft.client.renderer.state.level.CameraRenderState camera){super.submit(state,pose,out,camera);pose.pushPose();pose.rotate(camera.orientation);RiftEventRenderers.quad(pose,out,Identifier.parse("thaumcraft:textures/misc/particles.png"),64,512+(int)state.ageInTicks%16,.2F,0xffa060d0);pose.popPose();}
    }
    public static final class Model extends EntityModel<State> implements ArmedModel<State> {
        private final ModelPart head,jowls,rightArm,leftArm,rightLeg,leftLeg,lowerPack;
        Model(ModelPart root){super(root);head=root.getChild("head");jowls=root.getChild("jowls");rightArm=root.getChild("right_arm");leftArm=root.getChild("left_arm");rightLeg=root.getChild("right_leg");leftLeg=root.getChild("left_leg");lowerPack=root.getChild("lower_pack");}
        public static LayerDefinition layer(){var mesh=new MeshDefinition();var r=mesh.getRoot();
            r.addOrReplaceChild("body",CubeListBuilder.create().texOffs(34,12).mirror().addBox(-3,0,0,6,10,6),PartPose.offsetAndRotation(0,9,-3,.3129957F,0,0));
            r.addOrReplaceChild("right_leg",CubeListBuilder.create().texOffs(35,1).addBox(-2.9F,0,0,3,6,3),PartPose.offset(0,18,0));
            r.addOrReplaceChild("left_leg",CubeListBuilder.create().texOffs(35,1).mirror().addBox(-.1F,0,0,3,6,3),PartPose.offset(0,18,0));
            r.addOrReplaceChild("head",CubeListBuilder.create().texOffs(2,11).mirror().addBox(-3.5F,-5,-5,7,5,5),PartPose.offset(0,8,0));
            r.addOrReplaceChild("jowls",CubeListBuilder.create().texOffs(1,21).mirror().addBox(-4,-1,-6,8,3,5),PartPose.offset(0,8,0));
            r.addOrReplaceChild("lower_pack",CubeListBuilder.create().texOffs(0,0).mirror().addBox(-5,0,0,10,5,5),PartPose.offsetAndRotation(0,10,3.5F,.3013602F,0,0));
            r.addOrReplaceChild("upper_pack",CubeListBuilder.create().texOffs(64,1).mirror().addBox(-7.5F,-14,0,15,14,11),PartPose.offsetAndRotation(0,10,3,.4537856F,0,0));
            r.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(52,2).addBox(-2,0,-1,2,6,2),PartPose.offset(-3,10,-1));
            r.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(52,2).mirror().addBox(0,0,-1,2,6,2),PartPose.offset(3,10,-1));return LayerDefinition.create(mesh,128,64);
        }
        public void setupAnim(State s){super.setupAnim(s);float phase=s.walkAnimationPos*.6662F,speed=s.walkAnimationSpeed;
            head.yRot=s.yRot*(float)Math.PI/180;head.xRot=s.xRot*(float)Math.PI/180;jowls.yRot=head.yRot;jowls.xRot=head.xRot+.2617994F+(float)Math.cos(phase)*speed*.25F+.34906587F*Math.abs((float)Math.sin(s.mumble/8));
            rightArm.xRot=(float)Math.cos(phase+Math.PI)*speed;leftArm.xRot=(float)Math.cos(phase)*speed;rightLeg.xRot=(float)Math.cos(phase)*speed*1.4F;leftLeg.xRot=(float)Math.cos(phase+Math.PI)*speed*1.4F;lowerPack.yRot=(float)Math.cos(phase)*speed*.25F;lowerPack.zRot=lowerPack.yRot;
            rightArm.zRot+=(float)Math.cos(s.ageInTicks*.09F)*.05F+.05F;leftArm.zRot-=(float)Math.cos(s.ageInTicks*.09F)*.05F+.05F;rightArm.xRot+=(float)Math.sin(s.ageInTicks*.067F)*.05F;leftArm.xRot-=(float)Math.sin(s.ageInTicks*.067F)*.05F;
            if(s.swingAnimation>0)rightArm.xRot-=(float)Math.sin(s.swingAnimation*Math.PI)*1.2F;
        }
        public void translateToHand(State s,HumanoidArm arm,PoseStack pose){root().translateAndRotate(pose);(arm==HumanoidArm.LEFT?leftArm:rightArm).translateAndRotate(pose);}
    }
}
