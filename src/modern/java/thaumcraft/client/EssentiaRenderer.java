package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.essentia.*;

@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class EssentiaRenderer implements BlockEntityRenderer<TubeEntity,EssentiaRenderer.State>{
    public static final class State extends BlockEntityRenderState{String aspect="";}
    public EssentiaRenderer(BlockEntityRendererProvider.Context c){}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(EssentiaContent.TYPE.get(),EssentiaRenderer::new);}
    @Override public State createRenderState(){return new State();}
    @Override public void extractRenderState(TubeEntity m,State s,float p,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay breaking){BlockEntityRenderer.super.extractRenderState(m,s,p,camera,breaking);s.aspect=m.filter();}
    @Override public void submit(State s,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera){if(!s.aspect.isEmpty())for(var face:Direction.Plane.HORIZONTAL)label(pose,collector,s.aspect,face,.37F,.63F,.37F,.63F,.371F,s.lightCoords);}
    /** Original aspect glyph on paper; geometry and amount are derived only from synchronized state. */
    public static void label(PoseStack pose,SubmitNodeCollector collector,String aspect,Direction face,float left,float right,float bottom,float top,float depth,int light){
        label(pose,collector,aspect,face,left,right,bottom,top,depth,light,0);
    }
    public static void label(PoseStack pose,SubmitNodeCollector collector,String aspect,Direction face,float left,float right,float bottom,float top,float depth,int light,float angle){
        float center=(left+right)/2,height=(bottom+top)/2;
        var pivot=switch(face){case NORTH->new Vec3(center,height,depth);case SOUTH->new Vec3(center,height,1-depth);case WEST->new Vec3(depth,height,center);case EAST->new Vec3(1-depth,height,center);default->throw new IllegalArgumentException("Horizontal label required");};
        pose.pushPose();pose.translate(pivot.x,pivot.y,pivot.z);
        pose.rotate(new org.joml.Quaternionf().rotationAxis((float)Math.toRadians(angle),face.getStepX(),face.getStepY(),face.getStepZ()));
        pose.translate(-pivot.x,-pivot.y,-pivot.z);
        quad(pose,collector,Identifier.fromNamespaceAndPath("thaumcraft","textures/models/label.png"),face,left,right,bottom,top,depth,light,0xFFFFFFFF);
        int color=0xFF000000|thaumcraft.crystals.CrystalAspects.COLORS.getOrDefault(aspect,0xFFFFFF);
        float inset=(right-left)*.15F;
        quad(pose,collector,Identifier.fromNamespaceAndPath("thaumcraft","textures/aspects/"+aspect+".png"),face,left+inset,right-inset,bottom+inset,top-inset,depth-.001F,light,color);
        pose.popPose();
    }
    private static void quad(PoseStack pose,SubmitNodeCollector collector,Identifier texture,Direction face,float l,float r,float b,float t,float depth,int light,int color){
        var points=thaumcraft.visual.JarLabelGeometry.quad(face,l,r,b,t,depth);
        collector.submitCustomGeometry(pose,RenderTypes.entityCutout(texture),(transform,out)->{
            float[][] uv={{1,1},{1,0},{0,0},{0,1}};
            for(int i=0;i<4;i++){var p=points.get(i);out.addVertex(transform.pose(),(float)p.x,(float)p.y,(float)p.z).setColor(color).setUv(uv[i][0],uv[i][1]).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(transform,face.getStepX(),face.getStepY(),face.getStepZ());}
        });
    }
}
