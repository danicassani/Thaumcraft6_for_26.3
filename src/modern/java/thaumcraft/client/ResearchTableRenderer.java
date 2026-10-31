package thaumcraft.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.theory.*;
/** Original inkwell texture and quill, visible only while scribing tools occupy the table. */
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class ResearchTableRenderer implements BlockEntityRenderer<ResearchTableEntity,ResearchTableRenderer.State> {
 static final Identifier MODEL=Identifier.parse("thaumcraft:textures/blocks/research_table_model.png"),QUILL=Identifier.parse("thaumcraft:textures/research/quill.png");
 public static final class State extends BlockEntityRenderState {public boolean ink,active;}
 public ResearchTableRenderer(BlockEntityRendererProvider.Context context){}
 @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(TheoryContent.TYPE.get(),ResearchTableRenderer::new);}
 public State createRenderState(){return new State();}
 public void extractRenderState(ResearchTableEntity table,State state,float partial,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay overlay){BlockEntityRenderer.super.extractRenderState(table,state,partial,camera,overlay);if(table.getLevel()!=null)state.lightCoords=net.minecraft.util.LightCoordsUtil.getLightCoords(table.getLevel(),table.getBlockPos().above());state.ink=table.inventory.getItem(0).is(TheoryContent.SCRIBE.get());state.active=table.session()!=null;}
 public void submit(State state,PoseStack pose,SubmitNodeCollector out,CameraRenderState camera){
  if(state.active)box(pose,out,.38F,1.005F,.5F,.88F,1.13F,.625F,0,0,8,2,2,state.lightCoords);
  if(!state.ink)return;
  box(pose,out,.125F,1,.18F,.3125F,1.125F,.3675F,0,16,3,2,3,state.lightCoords);
  // Two outward faces retain the transparent silhouette from either side of the desk.
  out.submitCustomGeometry(pose,RenderTypes.entityCutout(QUILL),(transform,b)->{
   float[][] v={{.22F,1.075F,.27F,0,1},{.08F,1.635F,.37F,0,0},{.48F,1.635F,.63F,1,0},{.62F,1.075F,.53F,1,1}};
   var normal=new Vec3(-.14,.56,.10).cross(new Vec3(.26,.56,.36)).normalize();
   for(int side=0;side<2;side++)for(int i=0;i<4;i++){var a=v[side==0?i:3-i];b.addVertex(transform.pose(),a[0],a[1],a[2]).setColor(-1).setUv(a[3],a[4]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(state.lightCoords).setNormal(transform,(float)normal.x*(side==0?1:-1),(float)normal.y*(side==0?1:-1),(float)normal.z*(side==0?1:-1));}
  });
 }
 private static void box(PoseStack pose,SubmitNodeCollector out,float l,float b,float n,float r,float t,float f,int u,int v,int w,int h,int d,int light){
  float[][][] faces={{{l,t,n},{l,t,f},{r,t,f},{r,t,n}},{{l,b,n},{r,b,n},{r,b,f},{l,b,f}},{{l,b,n},{l,t,n},{r,t,n},{r,b,n}},{{r,b,f},{r,t,f},{l,t,f},{l,b,f}},{{l,b,f},{l,t,f},{l,t,n},{l,b,n}},{{r,b,n},{r,t,n},{r,t,f},{r,b,f}}};
  int[][] uv={{u+d,v,u+d+w,v+d},{u+d+w,v,u+d+w+w,v+d},{u+d,v+d,u+d+w,v+d+h},{u+d+w+d,v+d,u+d+w+d+w,v+d+h},{u,v+d,u+d,v+d+h},{u+d+w,v+d,u+d+w+d,v+d+h}};
  out.submitCustomGeometry(pose,RenderTypes.entityCutout(MODEL),(transform,buffer)->{for(int face=0;face<6;face++){var points=faces[face];var normal=new Vec3(points[1][0]-points[0][0],points[1][1]-points[0][1],points[1][2]-points[0][2]).cross(new Vec3(points[2][0]-points[0][0],points[2][1]-points[0][1],points[2][2]-points[0][2])).normalize();for(int i=0;i<4;i++){var p=points[i];float uu=(i<2?uv[face][0]:uv[face][2])/64F,vv=(i==0||i==3?uv[face][3]:uv[face][1])/32F;buffer.addVertex(transform.pose(),p[0],p[1],p[2]).setColor(-1).setUv(uu,vv).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(transform,(float)normal.x,(float)normal.y,(float)normal.z);}}});
 }
}
