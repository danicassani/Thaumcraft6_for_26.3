package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.crystals.CrystalAspects;
import thaumcraft.crystals.CrystalContent;
import thaumcraft.devices.*;

@Mod.EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class AuraDeviceRenderers {
    private AuraDeviceRenderers() {}
    @SubscribeEvent public static void colors(RegisterColorHandlersEvent.Block event) {
        CrystalContent.BLOCKS.forEach((aspect, block) -> event.register(List.of(BlockTintSources.constant(0xFF000000 | CrystalAspects.COLORS.get(aspect))), block.get()));
        event.register(List.of(BlockTintSources.grassBlock()), DeviceContent.GRASS.get());
    }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(DeviceContent.DIOPTRA_TYPE.get(), DioptraRenderer::new);
        event.registerBlockEntityRenderer(DeviceContent.PEDESTAL_TYPE.get(), PedestalRenderer::new);
    }
    public static final class GridState extends BlockEntityRenderState { int[] grid = new int[169]; boolean flux; }
    public static final class DioptraRenderer implements BlockEntityRenderer<DioptraEntity, GridState> {
        public DioptraRenderer(BlockEntityRendererProvider.Context context) {}
        @Override public GridState createRenderState() { return new GridState(); }
        @Override public void extractRenderState(DioptraEntity tile, GridState state, float partial, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breaking) {
            BlockEntityRenderer.super.extractRenderState(tile, state, partial, camera, breaking);
            state.grid = tile.grid(); state.flux = tile.getBlockState().getValue(AuraDeviceBlock.POWERED);
        }
        @Override public void submit(GridState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            var grid = state.grid; int color = state.flux ? 0xCCE066F2 : 0xCCB5EBFF;
            collector.submitCustomGeometry(pose, RenderTypes.debugQuads(), (transform, buffer) -> {
                for (int x = 0; x < 12; x++) for (int z = 0; z < 12; z++) {
                    float x0 = .005F + x * .99F / 12, x1 = .005F + (x+1) * .99F / 12;
                    float z0 = .005F + z * .99F / 12, z1 = .005F + (z+1) * .99F / 12;
                    float a = 1.002F + grid[x+z*13]/96F, b = 1.002F + grid[x+1+z*13]/96F;
                    float c = 1.002F + grid[x+1+(z+1)*13]/96F, d = 1.002F + grid[x+(z+1)*13]/96F;
                    vertex(buffer, transform, x0, a, z0, color); vertex(buffer, transform, x0, d, z1, color);
                    vertex(buffer, transform, x1, c, z1, color); vertex(buffer, transform, x1, b, z0, color);
                    // Narrow dark seams preserve the readable 13x13 sampling grid.
                    int seam = 0xFF244459; float width = .002F;
                    vertex(buffer, transform, x0, a+.001F, z0, seam); vertex(buffer, transform, x0, d+.001F, z1, seam);
                    vertex(buffer, transform, x0+width, d+.001F, z1, seam); vertex(buffer, transform, x0+width, a+.001F, z0, seam);
                }
            });
        }
        private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, int color) { buffer.addVertex(pose.pose(), x, y, z).setColor(color); }
    }
    public static final class PedestalState extends BlockEntityRenderState { final ItemStackRenderState item = new ItemStackRenderState(); float time; }
    public static final class PedestalRenderer implements BlockEntityRenderer<RechargePedestalEntity, PedestalState> {
        private final ItemModelResolver resolver;
        public PedestalRenderer(BlockEntityRendererProvider.Context context) { resolver = context.itemModelResolver(); }
        @Override public PedestalState createRenderState() { return new PedestalState(); }
        @Override public void extractRenderState(RechargePedestalEntity tile, PedestalState state, float partial, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breaking) {
            BlockEntityRenderer.super.extractRenderState(tile, state, partial, camera, breaking);
            state.time = tile.getLevel() == null ? 0 : tile.getLevel().getGameTime() + partial;
            resolver.updateForTopItem(state.item, tile.item(), ItemDisplayContext.GROUND, tile.getLevel(), null, tile.getBlockPos().hashCode());
        }
        @Override public void submit(PedestalState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            pose.pushPose(); pose.translate(.5F, 1.15F + (float)Math.sin(state.time / 10) * .025F, .5F);
            pose.rotateDegrees(Axis.YP, state.time * 2); pose.scale(.6F, .6F, .6F);
            state.item.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0); pose.popPose();
        }
    }
}
