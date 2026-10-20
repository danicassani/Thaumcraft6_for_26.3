package thaumcraft.client;
import java.util.Map;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.golems.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class GolemMachineRenderers {
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(GolemContent.BUILDER_TYPE.get(),Press::new);e.registerBlockEntityRenderer(GolemContent.PATTERN_TYPE.get(),Pattern::new);}
 public static final class PressState extends BlockEntityRenderState{boolean assembled,working;Direction facing;float time;}
 public static final class Press implements BlockEntityRenderer<GolemBuilder,PressState>{
  final Map<String,float[][][]> mesh;
  public Press(BlockEntityRendererProvider.Context c){mesh=GolemGeometry.load(net.minecraft.client.Minecraft.getInstance().getResourceManager()).get("golembuilder");}
  public PressState createRenderState(){return new PressState();}
  public void extractRenderState(GolemBuilder tile,PressState s,float partial,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay breaking){BlockEntityRenderer.super.extractRenderState(tile,s,partial,camera,breaking);s.assembled=tile.getBlockState().getValue(GolemBuilderBlock.ASSEMBLED);s.facing=tile.getBlockState().getValue(GolemBuilderBlock.FACING);s.working=tile.working();s.time=tile.getLevel().getGameTime()+partial;s.lightCoords=net.minecraft.util.LightCoordsUtil.getLightCoords(tile.getLevel(),tile.getBlockPos().above(2));}
  public void submit(PressState s,PoseStack pose,SubmitNodeCollector out,CameraRenderState camera){if(!s.assembled)return;pose.pushPose();pose.translate(.5,0,.5);pose.rotateDegrees(Axis.YP,s.facing==Direction.EAST?270:s.facing==Direction.WEST?90:s.facing==Direction.SOUTH?180:0);for(var group:mesh.entrySet()){pose.pushPose();if(group.getKey().equals("press")&&s.working)pose.translate(0,-Math.abs(Math.sin(s.time/12))*.625,0);GolemGeometry.draw(group.getValue(),pose,out,Identifier.fromNamespaceAndPath("thaumcraft","textures/blocks/golembuilder.png"),s.lightCoords,0xFFFFFFFF);pose.popPose();}pose.popPose();}
 }
 public static final class PatternState extends BlockEntityRenderState{int pattern;}
 public static final class Pattern implements BlockEntityRenderer<PatternCrafter,PatternState>{
  public Pattern(BlockEntityRendererProvider.Context c){}
  public PatternState createRenderState(){return new PatternState();}
  public void extractRenderState(PatternCrafter tile,PatternState s,float partial,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay breaking){BlockEntityRenderer.super.extractRenderState(tile,s,partial,camera,breaking);s.pattern=tile.pattern();s.lightCoords=net.minecraft.util.LightCoordsUtil.getLightCoords(tile.getLevel(),tile.getBlockPos().north());}
  public void submit(PatternState s,PoseStack pose,SubmitNodeCollector out,CameraRenderState camera){float u=s.pattern/10F,v=(s.pattern+1)/10F;float[][][] triangles={{{.25F,.5F,-.001F,u,1,0,0,-1},{.25F,1,-.001F,u,0,0,0,-1},{.75F,1,-.001F,v,0,0,0,-1}},{{.25F,.5F,-.001F,u,1,0,0,-1},{.75F,1,-.001F,v,0,0,0,-1},{.75F,.5F,-.001F,v,1,0,0,-1}}};GolemGeometry.draw(triangles,pose,out,Identifier.fromNamespaceAndPath("thaumcraft","textures/blocks/pattern_crafter_modes.png"),s.lightCoords,0xFFFFFFFF);}
 }
}
