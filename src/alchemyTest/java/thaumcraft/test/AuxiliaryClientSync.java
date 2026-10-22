package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.auxiliary.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class AuxiliaryClientSync {
 static boolean shown,title;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!AuxiliaryProbe.mode().equals("auxiliary-client")||!AuxiliaryProbe.ready)return;var mc=Minecraft.getInstance();var p=AuxiliaryProbe.P;if(!shown&&AuxiliaryProbe.framed&&mc.level!=null&&mc.gui.screen()==null&&mc.level.getBlockState(p.east(2)).is(AuxiliaryContent.TOGGLE.get())&&mc.level.getBlockState(p.east(2)).getValue(ArcaneEarBlock.ACTIVE)&&mc.level.getBlockEntity(p.south(4)) instanceof RelayEntity relay&&relay.input()==8&&relay.output()==9&&mc.level.getBlockState(p.south(6)).is(net.minecraft.world.level.block.Blocks.REDSTONE_LAMP)&&mc.level.getBlockState(p.south(6)).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)){shown=true;Thaumcraft.LOGGER.info("AUXILIARY_SCREENSHOT_READY: client sees latched ear from native note block, synced input8/output9 relay and lamp powered through solid output block");}if(shown&&!title&&mc.gui.screen() instanceof net.minecraft.client.gui.screens.TitleScreen){title=true;Thaumcraft.LOGGER.info("AUXILIARY_TITLE_READY: native world saved and integrated server closed");}}
}
