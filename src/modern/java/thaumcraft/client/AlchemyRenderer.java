package thaumcraft.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class AlchemyRenderer implements BlockEntityRenderer<AlchemyMachine,AlchemyRenderer.State>{
    public static final class State extends BlockEntityRenderState{float x0,x1,y0,y1;int color;boolean show;String filter="";net.minecraft.core.Direction facing;}
    public AlchemyRenderer(BlockEntityRendererProvider.Context c){}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(AlchemyContent.TYPE.get(),AlchemyRenderer::new);e.registerEntityRenderer(AlchemyContent.PROJECTILE.get(),ThrownItemRenderer::new);}
    @Override public State createRenderState(){return new State();}
    @Override public void extractRenderState(AlchemyMachine m,State s,float partial,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay breaking){BlockEntityRenderer.super.extractRenderState(m,s,partial,camera,breaking);s.filter=m.filter();s.facing=m.getBlockState().getValue(AlchemyBlock.FACING);s.show=m.kind()!=AlchemyBlock.Kind.SMELTER&&(m.water.getFluidAmount()>0||m.total()>0);s.color=m.color();s.x0=m.kind()==AlchemyBlock.Kind.CRUCIBLE?.13F:.255F;s.x1=1-s.x0;s.y0=m.kind()==AlchemyBlock.Kind.CRUCIBLE?.32F:.07F;s.y1=m.kind()==AlchemyBlock.Kind.CRUCIBLE?Math.min(.98F,.32F+.5F*m.water.getFluidAmount()/1000F+.16F*m.total()/500F):.07F+.58F*m.total()/m.capacity();}
    @Override public void submit(State s,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera){if(!s.filter.isEmpty())EssentiaRenderer.label(pose,collector,s.filter,s.facing,.36F,.64F,.22F,.50F,.181F,s.lightCoords);if(!s.show)return;collector.submitCustomGeometry(pose,RenderTypes.debugQuads(),(transform,b)->{float l=s.x0,r=s.x1,y=s.y0,t=s.y1;int c=s.color;
        float[][][] faces={{{l,t,l},{l,t,r},{r,t,r},{r,t,l}},{{l,y,r},{l,y,l},{r,y,l},{r,y,r}},{{l,y,l},{l,t,l},{r,t,l},{r,y,l}},{{r,y,r},{r,t,r},{l,t,r},{l,y,r}},{{l,y,r},{l,t,r},{l,t,l},{l,y,l}},{{r,y,l},{r,t,l},{r,t,r},{r,y,r}}};for(var face:faces)for(var v:face)b.addVertex(transform.pose(),v[0],v[1],v[2]).setColor(c);
    });}
}
