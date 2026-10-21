package thaumcraft.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
/** Both native slime layers use the original Thaumcraft skin. */
public final class ThaumicSlimeRenderer extends SlimeRenderer{
 private static final Identifier TEXTURE=Identifier.parse("thaumcraft:textures/entity/tslime.png");
 public ThaumicSlimeRenderer(EntityRendererProvider.Context c){super(c);layers.clear();addLayer(new RenderLayer<SlimeRenderState,SlimeModel>(this){private final SlimeModel outer=new SlimeModel(c.bakeLayer(ModelLayers.SLIME_OUTER));public void submit(PoseStack p,SubmitNodeCollector out,int light,SlimeRenderState s,float y,float x){boolean outline=s.isInvisible&&s.appearsGlowing();if(!s.isInvisible||outline)out.order(1).submitModel(outer,s,p,outline?RenderTypes.outline(TEXTURE):RenderTypes.entityTranslucent(TEXTURE),light,LivingEntityRenderer.getOverlayCoords(s,0),s.outlineColor);}});}
 public Identifier getTextureLocation(SlimeRenderState s){return TEXTURE;}
}
