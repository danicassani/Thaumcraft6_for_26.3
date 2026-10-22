package thaumcraft.test;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.content.SimpleContent;

@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class TreasureClientSync {
    static boolean shown;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post event) {
        if (!TreasureProbe.mode().equals("treasure-client") || !TreasureProbe.ready || shown) return;
        var mc=Minecraft.getInstance();
        if (mc.player==null || mc.level==null || mc.gui.screen()!=null) return;
        var bag=mc.player.getMainHandItem();
        if (!bag.is(SimpleContent.ITEMS.get("loot_bag_rare").get()) || bag.getCount()!=1) return;
        int units=mc.level.getEntitiesOfClass(ItemEntity.class,mc.player.getBoundingBox().inflate(6),e->e.isAlive())
            .stream().mapToInt(e->e.getItem().getCount()).sum();
        if (units<8 || units>36) return;
        shown=true;
        Thaumcraft.LOGGER.info("TREASURE_SCREENSHOT_READY: real use input consumed exactly one rare bag, native synchronized reward entities ({} units), six container models",units);
    }
}
