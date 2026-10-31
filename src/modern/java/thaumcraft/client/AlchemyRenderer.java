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
    public static final class State extends BlockEntityRenderState{float x0,x1,y0,y1,labelAngle,u0,u1,v0,v1;int color;boolean show,braced,crucible;net.minecraft.resources.Identifier waterAtlas;String filter="";net.minecraft.core.Direction facing;}
    public AlchemyRenderer(BlockEntityRendererProvider.Context c){}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(AlchemyContent.TYPE.get(),AlchemyRenderer::new);e.registerEntityRenderer(AlchemyContent.PROJECTILE.get(),ThrownItemRenderer::new);}
    @Override public State createRenderState(){return new State();}
    @Override public void extractRenderState(AlchemyMachine m,State s,float partial,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay breaking){BlockEntityRenderer.super.extractRenderState(m,s,partial,camera,breaking);s.braced=m.braced();s.filter=m.filter();s.facing=m.getBlockState().getValue(AlchemyBlock.FACING);s.labelAngle=m.jar()?thaumcraft.visual.JarLabelGeometry.degrees(s.filter,m.getBlockPos().getX(),s.facing,ClientGraphics.crookedLabels()):0;s.show=m.kind()!=AlchemyBlock.Kind.SMELTER&&(m.water.getFluidAmount()>0||m.total()>0);s.color=m.color();s.crucible=m.kind()==AlchemyBlock.Kind.CRUCIBLE;
        if(s.crucible){s.show=m.water.getFluidAmount()>0;var sprite=net.minecraft.client.Minecraft.getInstance().getAtlasManager().get(new net.minecraft.client.resources.model.sprite.SpriteId(net.minecraft.resources.Identifier.parse("minecraft:textures/atlas/blocks.png"),net.minecraft.resources.Identifier.parse("minecraft:block/water_still")));s.waterAtlas=sprite.atlasLocation();s.u0=sprite.getU0();s.u1=sprite.getU1();s.v0=sprite.getV0();s.v1=sprite.getV1();int tint=m.getLevel() instanceof net.minecraft.client.multiplayer.ClientLevel level?BiomeColors.getAverageWaterColor(level,m.getBlockPos()):0x3f76e4;s.color=thaumcraft.visual.CrucibleWater.color(tint,m.total());}
        s.x0=m.kind()==AlchemyBlock.Kind.CRUCIBLE?.13F:.255F;s.x1=1-s.x0;s.y0=m.kind()==AlchemyBlock.Kind.CRUCIBLE?.32F:.07F;s.y1=m.kind()==AlchemyBlock.Kind.CRUCIBLE?Math.min(.98F,.32F+.5F*m.water.getFluidAmount()/1000F+.16F*m.total()/500F):.07F+.58F*m.total()/m.capacity();}
    @Override public void submit(State s,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera){if(!s.filter.isEmpty())EssentiaRenderer.label(pose,collector,s.filter,s.facing,.36F,.64F,.22F,.50F,.181F,s.lightCoords,s.labelAngle);if(s.braced)brace(pose,collector,s.lightCoords);if(!s.show)return;if(s.crucible){water(s,pose,collector);return;}collector.submitCustomGeometry(pose,RenderTypes.debugQuads(),(transform,b)->{float l=s.x0,r=s.x1,y=s.y0,t=s.y1;int c=s.color;
        float[][][] faces={{{l,t,l},{l,t,r},{r,t,r},{r,t,l}},{{l,y,r},{l,y,l},{r,y,l},{r,y,r}},{{l,y,l},{l,t,l},{r,t,l},{r,y,l}},{{r,y,r},{r,t,r},{l,t,r},{l,y,r}},{{l,y,r},{l,t,r},{l,t,l},{l,y,l}},{{r,y,l},{r,t,l},{r,t,r},{r,y,r}}};for(var face:faces)for(var v:face)b.addVertex(transform.pose(),v[0],v[1],v[2]).setColor(c);
    });}
    private static void water(State s,PoseStack pose,SubmitNodeCollector collector){
        collector.submitCustomGeometry(pose,RenderTypes.entitySolid(s.waterAtlas),(transform,out)->{
            float[][] vertices={{s.x0,s.y1,s.x0,s.u0,s.v0},{s.x0,s.y1,s.x1,s.u0,s.v1},{s.x1,s.y1,s.x1,s.u1,s.v1},{s.x1,s.y1,s.x0,s.u1,s.v0}};
            for(var v:vertices)out.addVertex(transform.pose(),v[0],v[1],v[2]).setColor(s.color).setUv(v[3],v[4]).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(s.lightCoords).setNormal(transform,0,1,0);
        });
    }
    private static void brace(PoseStack pose,SubmitNodeCollector collector,int light){collector.submitCustomGeometry(pose,RenderTypes.debugQuads(),(transform,out)->{for(float[] box:new float[][]{{.18F,.735F,.43F,.82F,.78F,.57F},{.43F,.735F,.18F,.57F,.78F,.82F}}){float l=box[0],b=box[1],n=box[2],r=box[3],t=box[4],f=box[5];float[][][] faces={{{l,t,n},{l,t,f},{r,t,f},{r,t,n}},{{l,b,n},{r,b,n},{r,b,f},{l,b,f}},{{l,b,n},{l,t,n},{r,t,n},{r,b,n}},{{r,b,f},{r,t,f},{l,t,f},{l,b,f}},{{l,b,f},{l,t,f},{l,t,n},{l,b,n}},{{r,b,n},{r,t,n},{r,t,f},{r,b,f}}};for(var face:faces)for(var v:face)out.addVertex(transform.pose(),v[0],v[1],v[2]).setColor(0xffbc8d3b);}});}
}
