package thaumcraft.client;
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.resources.Identifier;
import thaumcraft.golems.*;
/** Native articulated approximation; legacy OBJ attachment meshes remain a later parity step. */
public final class GolemRenderer extends MobRenderer<WorkerGolem,IronGolemRenderState,IronGolemModel> {
    public static final class State extends IronGolemRenderState {int material;}
    public GolemRenderer(EntityRendererProvider.Context c){super(c,new IronGolemModel(layer().bakeRoot()),.3F);}
    private static LayerDefinition layer(){var mesh=new MeshDefinition();var root=mesh.getRoot();root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-3,-6,-3,6,6,6),PartPose.offset(0,-6,0));root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(0,32).addBox(-4,0,-2,8,12,4),PartPose.offset(0,-6,0));root.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(32,0).addBox(-4,0,-2,4,16,4),PartPose.offset(-4,-5,0));root.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(32,0).addBox(0,0,-2,4,16,4),PartPose.offset(4,-5,0));root.addOrReplaceChild("right_leg",CubeListBuilder.create().texOffs(32,32).addBox(-2,0,-2,4,18,4),PartPose.offset(-2,6,0));root.addOrReplaceChild("left_leg",CubeListBuilder.create().texOffs(32,32).addBox(-2,0,-2,4,18,4),PartPose.offset(2,6,0));return LayerDefinition.create(mesh,64,64);}
    public IronGolemRenderState createRenderState(){return new State();}
    public void extractRenderState(WorkerGolem g,IronGolemRenderState s,float t){super.extractRenderState(g,s,t);((State)s).material=g.material();s.attackTicksRemaining=Math.max(0,g.getAttackAnimationTick()-t);}
    public Identifier getTextureLocation(IronGolemRenderState s){return Identifier.fromNamespaceAndPath("thaumcraft","textures/entity/golems/mat_"+GolemDesign.MATERIALS.get(Math.clamp(((State)s).material,0,5))+".png");}
}
