package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.banners.*;

/** Original five boxes and 128x64 UVs; the cloth and glyph share one wave transform. */
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class BannerRenderer implements BlockEntityRenderer<AspectBannerEntity,BannerRenderer.State> {
 private static final ModelLayerLocation LAYER=new ModelLayerLocation(Identifier.parse("thaumcraft:banner"),"main");
 private static final Identifier BLANK=Identifier.parse("thaumcraft:textures/models/banner_blank.png"),CULT=Identifier.parse("thaumcraft:textures/models/banner_cultist.png");
 public record Parts(boolean wall,boolean cloth,float wave){}
 public static final class State extends BlockEntityRenderState{boolean wall,cult;int rotation,color;float wave;String aspect="";}
 private final Model<Parts> model;
 public BannerRenderer(BlockEntityRendererProvider.Context c){model=new Model<Parts>(c.bakeLayer(LAYER),RenderTypes::entityCutout){public void setupAnim(Parts p){root.getChild("pole").visible=!p.cloth&&!p.wall;root.getChild("beam").visible=!p.cloth;for(var name:new String[]{"tab_left","tab_right","cloth"})root.getChild(name).visible=p.cloth;root.getChild("cloth").xRot=p.wave;}};}
 @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(LAYER,BannerRenderer::geometry);}
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(BannerContent.TYPE.get(),BannerRenderer::new);}
 public static float wave(float phase){return BannerDesign.wave(phase);}
 public static LayerDefinition geometry(){var mesh=new MeshDefinition();var r=mesh.getRoot();r.addOrReplaceChild("tab_left",CubeListBuilder.create().texOffs(0,29).mirror().addBox(-5,-7.5F,-1.5F,2,3,3),PartPose.ZERO);r.addOrReplaceChild("tab_right",CubeListBuilder.create().texOffs(0,29).mirror().addBox(3,-7.5F,-1.5F,2,3,3),PartPose.ZERO);r.addOrReplaceChild("beam",CubeListBuilder.create().texOffs(30,0).mirror().addBox(-7,-7,-1,14,2,2),PartPose.ZERO);r.addOrReplaceChild("cloth",CubeListBuilder.create().texOffs(0,0).mirror().addBox(-7,0,-.5F,14,28,1),PartPose.offset(0,-5,0));r.addOrReplaceChild("pole",CubeListBuilder.create().texOffs(62,0).mirror().addBox(0,0,-1,2,31,2),PartPose.offset(-1,-7,-2));return LayerDefinition.create(mesh,128,64);}
 @SubscribeEvent public static void colours(net.minecraftforge.client.event.RegisterColorHandlersEvent.Block e){BannerContent.COLOURS.forEach((d,b)->e.register(java.util.List.of(net.minecraft.client.color.block.BlockTintSources.constant(0xff000000|d.getTextureDiffuseColor())),b.get()));}
 public State createRenderState(){return new State();}
 public boolean shouldRenderOffScreen(){return true;}
 public void extractRenderState(AspectBannerEntity b,State s,float partial,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay breaking){BlockEntityRenderer.super.extractRenderState(b,s,partial,camera,breaking);var state=b.getBlockState();s.wall=state.getValue(AspectBannerBlock.WALL);s.rotation=state.getValue(AspectBannerBlock.ROTATION);var dye=((AspectBannerBlock)state.getBlock()).dye;s.cult=dye==null;s.color=dye==null?-1:0xff000000|dye.getTextureDiffuseColor();s.aspect=b.aspect();var p=b.getBlockPos();s.wave=wave(p.getX()*7+p.getY()*9+p.getZ()*13+(b.getLevel()==null?0:b.getLevel().getGameTime())+partial);}
 public void submit(State s,PoseStack pose,SubmitNodeCollector out,CameraRenderState camera){pose.pushPose();pose.translate(.5,1.5,.5);pose.rotateDegrees(Axis.XP,180);pose.rotateDegrees(Axis.YP,180+s.rotation*22.5F);if(s.wall)pose.translate(0,1,-.4125);var texture=s.cult?CULT:BLANK;out.submitModel(model,new Parts(s.wall,false,s.wave),pose,RenderTypes.entityCutout(texture),s.lightCoords,OverlayTexture.NO_OVERLAY,-1,null,0);out.submitModel(model,new Parts(s.wall,true,s.wave),pose,RenderTypes.entityCutout(texture),s.lightCoords,OverlayTexture.NO_OVERLAY,s.color,null,0);
  if(!s.aspect.isEmpty()){pose.pushPose();pose.translate(0,-5F/16,0);pose.rotateDegrees(Axis.XP,s.wave*180/(float)Math.PI);var glyph=Identifier.fromNamespaceAndPath("thaumcraft","textures/aspects/"+s.aspect+".png");int color=0xbf000000|thaumcraft.crystals.CrystalAspects.COLORS.getOrDefault(s.aspect,0xffffff);out.submitCustomGeometry(pose,RenderTypes.entityTranslucent(glyph),(transform,b)->{float[][] v={{-.3F,.31F,+.032F,1,0},{.3F,.31F,+.032F,0,0},{.3F,.91F,+.032F,0,1},{-.3F,.91F,+.032F,1,1}};for(var a:v)b.addVertex(transform.pose(),a[0],a[1],a[2]).setColor(color).setUv(a[3],a[4]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(s.lightCoords).setNormal(transform,0,0,1);});pose.popPose();}
  pose.popPose();}
}
