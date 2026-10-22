package thaumcraft.test;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.crystals.CrystalContent;
import thaumcraft.infusion.CuriosityContent;
import thaumcraft.taint.TaintContent;

@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class LootClientSync {
    static boolean shown;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post event) {
        if (!LootProbe.mode().equals("loot-client") || !LootProbe.ready || shown) return;
        var mc=Minecraft.getInstance();
        if (mc.player==null || !(mc.player.containerMenu instanceof ChestMenu menu) || menu.getRowCount()!=3) return;
        if (!menu.getSlot(0).getItem().is(TaintContent.POROUS.get().asItem())) return;
        LootProbe.check("vitium".equals(menu.getSlot(1).getItem().get(CrystalContent.ASPECT.get())),"Client lost aspect component");
        LootProbe.check(menu.getSlot(6).getItem().getCount()==3 && "vitium".equals(menu.getSlot(6).getItem().get(CrystalContent.ASPECT.get())),"Client lost count modifier");
        LootProbe.check("rites".equals(menu.getSlot(7).getItem().get(CuriosityContent.VARIANT.get())),"Client lost curiosity component");
        LootProbe.check(menu.getSlot(5).getItem().is(Items.PISTON) && menu.getSlot(3).getItem().is(Items.GRAVEL),"Client fallback drops wrong");
        shown=true;
        Thaumcraft.LOGGER.info("LOOT_SCREENSHOT_READY: real native chest menu with server-generated Silk, fallback, prime-seed count and rites/vitium components");
    }
}
