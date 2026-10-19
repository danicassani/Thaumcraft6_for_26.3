package thaumcraft.client;
import java.util.*;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.infusion.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class StabilizationColors {
    @SubscribeEvent public static void colors(RegisterColorHandlersEvent.Block e){e.register(List.of(new BlockTintSource(){public int color(BlockState s){int charge=s.getValue(InfusionBlock.CHARGE);int v=charge==0?76:127+128*charge/15;return 0xff000000|v<<16|v<<8|v;}public Set<Property<?>> relevantProperties(){return Set.of(InfusionBlock.CHARGE);}}),InfusionContent.STABILIZER.get(),InfusionContent.INLAY.get());}
}
