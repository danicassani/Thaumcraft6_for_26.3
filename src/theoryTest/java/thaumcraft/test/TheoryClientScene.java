package thaumcraft.test;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.research.*;
import thaumcraft.theory.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID)
public final class TheoryClientScene {
    private static final Set<UUID> ready=new HashSet<>();
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent.Post e){
        if(!System.getProperty("thaumcraft.theoryProbe","").equals("client")||!(e.player() instanceof ServerPlayer p)||p.tickCount<60||!ready.add(p.getUUID()))return;
        var level=p.level();var pos=new BlockPos(8,205,11);
        for(var b:BlockPos.betweenClosed(pos.offset(-4,-1,-4),pos.offset(4,4,4)))level.setBlockAndUpdate(b,b.getY()==204?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pos,TheoryContent.TABLE.get().defaultBlockState());level.setBlockAndUpdate(pos.east(2),Blocks.ENCHANTING_TABLE.defaultBlockState());
        KnowledgeStore.write(p,KnowledgeState.EMPTY.stage("THEORYRESEARCH",3).stage("FIRSTSTEPS",4).stage("CELESTIALSCANNING",2));p.getInventory().clearContent();p.getInventory().setItem(0,new ItemStack(ResearchContent.METER.get()));p.getInventory().setItem(1,new ItemStack(TheoryContent.SCRIBE.get()));p.getInventory().setItem(2,new ItemStack(Items.PAPER,16));for(int i=0;i<13;i++)p.getInventory().setItem(9+i,TheoryContent.note(i));
        var table=(ResearchTableEntity)level.getBlockEntity(pos);table.inventory.setItem(0,new ItemStack(TheoryContent.SCRIBE.get()));table.inventory.setItem(1,new ItemStack(Items.PAPER,20));
        p.teleportTo(level,8.5,205,8.5,Set.of(),0,15,true);p.giveExperienceLevels(10);p.inventoryMenu.broadcastChanges();p.openMenu(table);
        Thaumcraft.LOGGER.info("THEORY_CLIENT_SCENE_READY: native table menu, ink/paper and 13 rendered celestial variants; click start, draw, choose");
    }
}
