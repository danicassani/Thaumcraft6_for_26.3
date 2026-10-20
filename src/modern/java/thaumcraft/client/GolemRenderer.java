package thaumcraft.client;
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.resources.Identifier;
import thaumcraft.golems.*;
/** Native articulated approximation; legacy OBJ attachment meshes remain a later parity step. */
public final class GolemRenderer extends MobRenderer<WorkerGolem,IronGolemRenderState,IronGolemModel> {
    public static final class State extends IronGolemRenderState {int material,legs;}
    public GolemRenderer(EntityRendererProvider.Context c){super(c,new Model(layer().bakeRoot()),.3F);}
    private static LayerDefinition layer(){var mesh=new MeshDefinition();var root=mesh.getRoot();root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-3,-6,-3,6,6,6),PartPose.offset(0,-6,0));root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(0,32).addBox(-4,0,-2,8,12,4),PartPose.offset(0,-6,0));root.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(32,0).addBox(-4,0,-2,4,16,4),PartPose.offset(-4,-5,0));root.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(32,0).addBox(0,0,-2,4,16,4),PartPose.offset(4,-5,0));root.addOrReplaceChild("right_leg",CubeListBuilder.create().texOffs(32,32).addBox(-2,0,-2,4,18,4),PartPose.offset(-2,6,0));root.addOrReplaceChild("left_leg",CubeListBuilder.create().texOffs(32,32).addBox(-2,0,-2,4,18,4),PartPose.offset(2,6,0));root.addOrReplaceChild("wheel",CubeListBuilder.create().texOffs(32,32).addBox(-5,-3,-3,10,6,6),PartPose.offset(0,21,0));root.addOrReplaceChild("levitator",CubeListBuilder.create().texOffs(0,32).addBox(-5,-2,-4,10,4,8),PartPose.offset(0,17,0));return LayerDefinition.create(mesh,64,64);}
    private static final class Model extends IronGolemModel {
        private final ModelPart left,right,wheel,hover;
        Model(ModelPart root){super(root);left=root.getChild("left_leg");right=root.getChild("right_leg");wheel=root.getChild("wheel");hover=root.getChild("levitator");}
        @Override public void setupAnim(IronGolemRenderState state){super.setupAnim(state);int legs=((State)state).legs;left.visible=right.visible=legs==0||legs==2;wheel.visible=legs==1;hover.visible=legs==3;if(legs==1)wheel.xRot=state.walkAnimationPos;if(legs==2){left.zRot=-.3F;right.zRot=.3F;}}
    }
    public IronGolemRenderState createRenderState(){return new State();}
    public void extractRenderState(WorkerGolem g,IronGolemRenderState s,float t){super.extractRenderState(g,s,t);((State)s).material=g.material();((State)s).legs=g.legs();s.attackTicksRemaining=Math.max(0,g.getAttackAnimationTick()-t);}
    public Identifier getTextureLocation(IronGolemRenderState s){return Identifier.fromNamespaceAndPath("thaumcraft","textures/entity/golems/mat_"+GolemDesign.MATERIALS.get(Math.clamp(((State)s).material,0,5))+".png");}
}
