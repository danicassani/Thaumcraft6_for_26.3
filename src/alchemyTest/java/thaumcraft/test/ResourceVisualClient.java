package thaumcraft.test;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.storage.HungryChest;
import thaumcraft.taint.TaintBlock;

@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class ResourceVisualClient {
    static boolean checked;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post event) {
        if (!ResourceVisualProbe.active() || !ResourceVisualProbe.ready || checked) return;
        var mc=Minecraft.getInstance();
        if (mc.level==null || mc.player==null || mc.gui.screen()!=null) return;
        for (int growth=1;growth<=4;growth++) {
            var state=mc.level.getBlockState(ResourceVisualProbe.P.offset(growth*2-5,0,0));
            if (!state.hasProperty(TaintBlock.GROWTH) || state.getValue(TaintBlock.GROWTH)!=growth) return;
        }
        var texture=Identifier.parse("thaumcraft:entity/chest/hungry");
        var sprite=mc.getAtlasManager().get(new SpriteId(Identifier.parse("minecraft:textures/atlas/blocks.png"),texture));
        if (!sprite.contents().name().equals(texture)) throw new AssertionError("Hungry chest absent from block atlas");
        var state=mc.level.getBlockState(ResourceVisualProbe.P.offset(-3,0,3));
        var particle=mc.getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();
        if (!particle.contents().name().equals(texture)) throw new AssertionError("Hungry chest particle uses missing texture");
        if (!mc.player.getMainHandItem().is(HungryChest.BLOCK.get().asItem())) return;
        checked=true;
        Thaumcraft.LOGGER.info("RESOURCE_VISUAL_OK: real stitched chest sprite, baked block particle, held chest and four synchronized growth samples");
    }
}
