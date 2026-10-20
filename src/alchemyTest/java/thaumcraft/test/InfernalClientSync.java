package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.infernal.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class InfernalClientSync{
 static int waiting;static boolean shown,title,menu;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!InfernalProbe.mode().equals("infernal-client"))return;var mc=Minecraft.getInstance();if(InfernalProbe.ready&&mc.gui.screen() instanceof TitleScreen&&!title){title=true;Thaumcraft.LOGGER.info("INFERNAL_TITLE_READY: native world saved and title active");}if(InfernalProbe.ready&&!menu&&mc.player!=null&&mc.player.containerMenu instanceof net.minecraft.world.inventory.ChestMenu chest&&chest.slots.stream().limit(27).anyMatch(s->s.getItem().is(Items.IRON_INGOT))){menu=true;Thaumcraft.LOGGER.info("INFERNAL_CHEST_OK: native chest menu contains physically cooked ingots");}if(!InfernalProbe.ready||shown||mc.level==null)return;if(++waiting>2400)throw new AssertionError("Infernal client synchronization timeout");if(mc.level.getBlockEntity(InfernalProbe.P) instanceof InfernalFurnace f&&f.supported()&&f.work().speed()>0&&f.getBlockState().getValue(InfernalBlock.ACTIVE)&&f.bellows()==3&&InfernalStructure.frame(InfernalProbe.P,f.front()).keySet().stream().allMatch(p->mc.level.getBlockEntity(p) instanceof InfernalFrame part&&InfernalProbe.P.equals(part.controller))&&mc.level.getBlockEntity(InfernalProbe.P.south(2)) instanceof ChestBlockEntity){shown=true;Thaumcraft.LOGGER.info("INFERNAL_SCREENSHOT_READY: synchronized original multiblock, active timer, paid aura, three bellows and native chest");}}
}
