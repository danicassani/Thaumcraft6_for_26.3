package thaumcraft.client;
import java.util.*;
import java.lang.reflect.Proxy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.test.*;
import thaumcraft.alchemy.AlchemyMachine;
import thaumcraft.mirrors.MirrorEntity;
import thaumcraft.rifts.FluxRiftEntity;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class GraphicsClientProof {
 static String applied="",confirmed="";static int wait;static boolean saved,oldShaders,oldCrooked;static final Set<String> checked=new HashSet<>();
 static final class Draws {final List<RenderType> types=new ArrayList<>();final List<org.joml.Matrix4f> poses=new ArrayList<>();SubmitNodeCollector collector(){return (SubmitNodeCollector)Proxy.newProxyInstance(SubmitNodeCollector.class.getClassLoader(),new Class<?>[]{SubmitNodeCollector.class},(p,m,a)->{if(m.getName().equals("submitCustomGeometry")){types.add((RenderType)a[1]);poses.add(new org.joml.Matrix4f(((PoseStack)a[0]).last().pose()));}if(m.getReturnType().isInstance(p))return p;return null;});}}
 static void verify(Minecraft mc,String view){GraphicsProbe.check(mc.getResourceManager().getResource(PortalSurface.TEXTURE).isPresent(),"Portal fallback texture absent from native resources");GraphicsProbe.check(mc.getResourceManager().getResource(net.minecraft.resources.Identifier.parse("thaumcraft:textures/models/label.png")).isPresent(),"Jar paper texture absent");boolean advanced=!view.equals("simple"),crooked=!view.equals("simple")&&!view.equals("straight");var mirror=(MirrorEntity)mc.level.getBlockEntity(GraphicsClientScene.M);Object renderer=mc.getBlockEntityRenderDispatcher().getRenderer(mirror);GraphicsProbe.check(renderer instanceof MirrorRenderer,"Mirror renderer missing");var mr=(MirrorRenderer)renderer;var ms=mr.createRenderState();mr.extractRenderState(mirror,ms,0,mc.player.position(),null);GraphicsProbe.check(ms.active&&ms.advanced==advanced,"Mirror option not extracted");var draws=new Draws();mr.submit(ms,new PoseStack(),draws.collector(),null);GraphicsProbe.check(draws.types.size()==1&&draws.types.getFirst()==(advanced?RenderTypes.endPortal():RenderTypes.entitySolid(PortalSurface.TEXTURE)),"Mirror submitted wrong render pipeline");
  var rift=(FluxRiftEntity)mc.level.getEntity(GraphicsClientScene.riftId);var rr=(FluxRiftRenderer)(Object)mc.getEntityRenderDispatcher().getRenderer(rift);var rs=rr.createRenderState();rr.extractRenderState(rift,rs,0);GraphicsProbe.check(rs.advanced==advanced,"Rift option not extracted");draws=new Draws();rr.submit(rs,new PoseStack(),draws.collector(),null);GraphicsProbe.check(draws.types.size()==4&&draws.types.getFirst()==(advanced?RenderTypes.endPortal():RenderTypes.entitySolid(PortalSurface.TEXTURE)),"Rift core or halo pipeline missing");
  var jar=(AlchemyMachine)mc.level.getBlockEntity(GraphicsClientScene.J.east(2));var ar=(AlchemyRenderer)(Object)mc.getBlockEntityRenderDispatcher().getRenderer(jar);var js=ar.createRenderState();ar.extractRenderState(jar,js,0,mc.player.position(),null);GraphicsProbe.check(crooked?js.labelAngle!=0:js.labelAngle==0,"Jar label option not extracted");draws=new Draws();ar.submit(js,new PoseStack(),draws.collector(),null);GraphicsProbe.check(draws.poses.size()>=2&&draws.poses.get(0).equals(draws.poses.get(1)),"Paper and aspect no longer share a transform");GraphicsProbe.check(crooked?!draws.poses.getFirst().equals(new org.joml.Matrix4f(),.000001F):draws.poses.getFirst().equals(new org.joml.Matrix4f(),.000001F),"Label rotation not applied to submitted geometry");
  // The deferred node must retain its extracted format even if config changes meanwhile.
  ThaumcraftConfig.disableShaders.set(advanced);draws=new Draws();mr.submit(ms,new PoseStack(),draws.collector(),null);GraphicsProbe.check(draws.types.getFirst()==(advanced?RenderTypes.endPortal():RenderTypes.entitySolid(PortalSurface.TEXTURE)),"Deferred mirror format changed under config reload");ThaumcraftConfig.disableShaders.set(!advanced);
  for(var face:Direction.Plane.HORIZONTAL){var paper=new Draws();EssentiaRenderer.label(new PoseStack(),paper.collector(),"vitium",face,.36F,.64F,.22F,.5F,.181F,0xf000f0,js.labelAngle);GraphicsProbe.check(paper.poses.size()==2&&paper.poses.get(0).equals(paper.poses.get(1)),"Label split on "+face);}
 }
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!System.getProperty("thaumcraft.graphicsProbe","").equals("client"))return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null){if(saved){ThaumcraftConfig.disableShaders.set(oldShaders);ThaumcraftConfig.crooked.set(oldCrooked);saved=false;thaumcraft.Thaumcraft.LOGGER.info("GRAPHICS77_CONFIG_RESTORED_OK: original client values restored after disconnect");}return;}if(!GraphicsClientScene.ready||!(mc.level.getBlockEntity(GraphicsClientScene.J.east(2)) instanceof AlchemyMachine jar)||!jar.filter().equals("vitium")||!(mc.level.getBlockEntity(GraphicsClientScene.M) instanceof MirrorEntity mirror)||!mirror.getBlockState().getValue(thaumcraft.mirrors.MirrorBlock.ACTIVE)||!(mc.level.getEntity(GraphicsClientScene.riftId) instanceof FluxRiftEntity))return;
  if(!saved){oldShaders=ThaumcraftConfig.disableShaders.get();oldCrooked=ThaumcraftConfig.crooked.get();saved=true;}
  String view=GraphicsClientScene.view;if(!view.equals(applied)){applied=view;wait=0;ThaumcraftConfig.disableShaders.set(view.equals("simple"));ThaumcraftConfig.crooked.set(!view.equals("simple")&&!view.equals("straight"));}
  if(view.equals(confirmed)||mc.player.position().distanceToSqr(GraphicsClientScene.camera(view))>.01||++wait<25)return;
  verify(mc,view);confirmed=view;checked.add(view);thaumcraft.Thaumcraft.LOGGER.info("GRAPHICS77_CLIENT_VIEW_OK: {} real render submissions, paired label transform and deferred config snapshot verified",view);
  if(checked.size()==5){GraphicsClientScene.verified=true;thaumcraft.Thaumcraft.LOGGER.info("GRAPHICS77_CLIENT_ALL_OK: live normal/simple/normal and crooked/straight/crooked transitions verified");}
 }
}
