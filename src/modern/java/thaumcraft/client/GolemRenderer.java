package thaumcraft.client;

import java.util.*;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import thaumcraft.golems.*;

/** Original OBJ triangles, groups, UVs and attachment transforms; no approximation cubes. */
public final class GolemRenderer extends EntityRenderer<WorkerGolem,GolemRenderer.State> {
    public static final class State extends EntityRenderState {
        int material,head,arms,legs,addon,channel;float yaw,pitch,headYaw,walk,move,attack;boolean combat;
        final ItemStackRenderState carried=new ItemStackRenderState(),haul=new ItemStackRenderState();
    }
    private final Map<String,Map<String,float[][][]>> meshes=new HashMap<>();private final ItemModelResolver resolver;
    public GolemRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.3F;resolver=c.getItemModelResolver();
        meshes.putAll(GolemGeometry.load(c.getResourceManager()));
    }
    private static Identifier id(String path){return Identifier.fromNamespaceAndPath("thaumcraft",path);}
    public State createRenderState(){return new State();}
    public void extractRenderState(WorkerGolem g,State s,float t){super.extractRenderState(g,s,t);s.material=g.material();s.head=g.head();s.arms=g.arms();s.legs=g.legs();s.addon=g.addon();s.channel=g.renderChannel();s.yaw=Mth.rotLerp(t,g.yBodyRotO,g.yBodyRot);s.headYaw=Mth.rotLerp(t,g.yHeadRotO,g.yHeadRot)-s.yaw;s.pitch=Mth.lerp(t,g.xRotO,g.getXRot());s.walk=g.walkAnimation.position(t);s.move=g.walkAnimation.speed(t);s.attack=Math.max(0,g.getAttackAnimationTick()-t)/10F;s.combat=g.inCombat();resolver.updateForTopItem(s.carried,g.visibleCargo(0),ItemDisplayContext.GROUND,g.level(),g,g.getId());resolver.updateForTopItem(s.haul,g.visibleCargo(1),ItemDisplayContext.GROUND,g.level(),g,g.getId()+1);}
    public void submit(State s,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera){super.submit(s,pose,collector,camera);if(s.isInvisible)return;pose.pushPose();pose.rotateDegrees(Axis.YP,180-s.yaw);String material="mat_"+GolemDesign.MATERIALS.get(Math.clamp(s.material,0,5));
        pose.pushPose();pose.translate(0,.5,0);part(pose,collector,s,"golem_base","chest",material,0xFFFFFFFF);part(pose,collector,s,"golem_base","waist",material,0xFFFFFFFF);part(pose,collector,s,"golem_base","flag",material,0xFF000000|net.minecraft.world.item.DyeColor.byId(s.channel).getTextureDiffuseColor());if(s.addon==1)model(pose,collector,s,"golem_armor",material,null);if(s.addon==3)model(pose,collector,s,"golem_hauler",material,"golem_hauler");if(s.legs==1||s.legs==3)model(pose,collector,s,s.legs==1?"golem_legs_wheel":"golem_legs_floater",material,s.legs==1?"golem_legs_wheel":"golem_legs_floater");pose.popPose();
        pose.pushPose();pose.translate(0,.75,-.03125);pose.rotateDegrees(Axis.YP,-s.headYaw);pose.rotateDegrees(Axis.XP,-s.pitch);model(pose,collector,s,"golem_head_"+new String[]{"basic","smart","smart_armor","scout","scout_smart"}[s.head],material,"golem_head_other");pose.popPose();
        for(int side=0;side<2;side++){pose.pushPose();pose.translate(side==0?.20625:-.20625,.6875,0);float swing=(float)Math.cos(s.walk*.6662+(side==0?Math.PI:0))*s.move*40;float arm=!s.carried.isEmpty()?90:s.combat&&s.arms==4?90-s.pitch:swing+s.attack*75;pose.rotateDegrees(Axis.XP,arm);if(side==1)pose.rotateDegrees(Axis.YP,180);part(pose,collector,s,"golem_base","arm",material,0xFFFFFFFF);model(pose,collector,s,"golem_arms_"+new String[]{"basic","fine","claws","breakers","darter"}[s.arms],material,"golem_arms_"+new String[]{"basic","fine","claws","breakers","darter"}[s.arms]);pose.popPose();
            if(s.legs==0||s.legs==2){pose.pushPose();pose.translate(side==0?.09375:-.09375,.375,0);pose.rotateDegrees(Axis.XP,(float)Math.cos(s.walk*.6662+(side==0?0:Math.PI))*s.move*55);if(side==1)pose.rotateDegrees(Axis.YP,180);model(pose,collector,s,s.legs==0?"golem_legs_walker":"golem_legs_climber",material,null);pose.popPose();}}
        pose.pushPose();pose.translate(0,.6,-.32);pose.scale(.375F,.375F,.375F);s.carried.submit(pose,collector,s.lightCoords,OverlayTexture.NO_OVERLAY,s.outlineColor);pose.popPose();if(s.addon==3){pose.pushPose();pose.translate(0,.65,.3);pose.scale(.375F,.375F,.375F);s.haul.submit(pose,collector,s.lightCoords,OverlayTexture.NO_OVERLAY,s.outlineColor);pose.popPose();}pose.popPose();
    }
    private void model(PoseStack pose,SubmitNodeCollector out,State s,String model,String material,String other){for(String group:meshes.get(model).keySet()){pose.pushPose();if(group.equals("wheel")){pose.translate(0,-.375,0);pose.rotateDegrees(Axis.XP,-s.walk*60);}if(group.equals("grinder")){pose.translate(0,-.34,0);pose.rotateDegrees(Axis.XP,s.ageInTicks*(s.attack>0?20:1));}if(group.startsWith("claw")){pose.translate(0,-.2,0);pose.rotateDegrees(Axis.XP,s.attack*s.attack*(group.endsWith("1")?17:-17));}part(pose,out,s,model,group,group.startsWith("bm")||other==null?material:other,0xFFFFFFFF);pose.popPose();}}
    private void part(PoseStack pose,SubmitNodeCollector collector,State state,String model,String group,String texture,int color){GolemGeometry.draw(meshes.get(model).get(group),pose,collector,id("textures/entity/golems/"+texture+".png"),state.lightCoords,color);}
}
