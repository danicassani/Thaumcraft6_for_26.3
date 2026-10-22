package thaumcraft.test;

import java.util.Set;
import net.minecraft.core.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.storage.HungryChest;
import thaumcraft.taint.*;

/** Isolated visual scene, enabled explicitly and excluded from release builds. */
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class ResourceVisualProbe {
    public static final BlockPos P = new BlockPos(3800,205,8);
    public static volatile boolean ready;
    static int ticks;
    static boolean active() { return "resource-visual".equals(System.getProperty("thaumcraft.alchemyProbe")); }

    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post event) {
        if (!active()) return;
        var server = event.server();
        var level = server.overworld();
        if (++ticks < 80 || server.getPlayerList().getPlayers().isEmpty()) return;
        level.getServer().clockManager().setTotalTicks(level.dimensionType().defaultClock().orElseThrow(),6000);
        if (!ready) {
            for (var pos : BlockPos.betweenClosed(P.offset(-7,-1,-3),P.offset(7,5,11)))
                level.setBlockAndUpdate(pos,pos.getY()==P.getY()-1 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(P.offset(-3,0,3),HungryChest.BLOCK.get().defaultBlockState().setValue(ChestBlock.FACING,Direction.SOUTH));
            var viewer = server.getPlayerList().getPlayers().getFirst();
            viewer.setGameMode(GameType.CREATIVE);
            viewer.getInventory().clearContent();
            viewer.getInventory().setItem(0,new ItemStack(HungryChest.BLOCK.get()));
            viewer.teleportTo(level,P.getX()+.5,P.getY()+1,P.getZ()+9,Set.of(),180,22,true);
            viewer.getAbilities().flying=true;
            viewer.onUpdateAbilities();
            ready=true;
            Thaumcraft.LOGGER.info("RESOURCE_SCENE_READY: hungry chest block and held item, four taint growth variants");
        }
        // Keep the isolated visual samples present without changing global gamerules.
        if (ticks % 20 == 0) for (int growth=1;growth<=4;growth++)
            level.setBlockAndUpdate(P.offset(growth*2-5,0,0),TaintContent.BLOCKS.get("taint_fibre").get().defaultBlockState()
                .setValue(TaintBlock.SIDES[0],true).setValue(TaintBlock.GROWTH,growth));
    }
}
