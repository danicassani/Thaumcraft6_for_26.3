package thaumcraft.test;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.aura.*;
import thaumcraft.research.*;

@Mod.EventBusSubscriber(modid=Thaumcraft.MODID)
public final class KnowledgeClientScene {
    private static final Set<UUID> prepared=new HashSet<>(),scanned=new HashSet<>();
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent.Post event){
        if(!System.getProperty("thaumcraft.knowledgeProbe","").equals("client")||!(event.player() instanceof ServerPlayer player))return;
        if(prepared.add(player.getUUID())){
            var level=player.level();var base=new BlockPos(8,205,8);
            for(int x=-3;x<=3;x++)for(int z=-2;z<=8;z++){level.setBlockAndUpdate(base.offset(x,-1,z),Blocks.STONE.defaultBlockState());for(int y=0;y<6;y++)level.setBlockAndUpdate(base.offset(x,y,z),Blocks.AIR.defaultBlockState());}
            level.setBlockAndUpdate(new BlockPos(8,207,11),Blocks.STONE.defaultBlockState());
            var state=KnowledgeState.EMPTY.flag("!gotthaumonomicon").flag("!gotdream").stage("FIRSTSTEPS",2).stage("KNOWLEDGETYPES",2).craft("thaumcraft:thaumometer").craft("thaumcraft:arcane_workbench").addPoints("OBSERVATION","BASICS",15);
            KnowledgeStore.write(player,state);player.getInventory().clearContent();player.getInventory().setItem(0,new ItemStack(ResearchContent.METER.get()));player.getInventory().setItem(1,new ItemStack(ResearchContent.BOOK.get()));player.inventoryMenu.broadcastChanges();
            AuraData.get(level).put(AuraCell.key(0,0),new AuraCell(200,150.5F,12.25F));
            player.teleportTo(level,8.5,206,8.5,Set.of(),0,0,true);player.getAbilities().flying=true;player.onUpdateAbilities();
            Thaumcraft.LOGGER.info("KNOWLEDGE_CLIENT_SCENE_READY: meter slot 1, book slot 2, aim stone; observation 15 before scan");
        }
        if(!scanned.contains(player.getUUID())){var state=ResearchService.state(player);if(state!=null&&state.scans().contains("item:minecraft:stone")){scanned.add(player.getUUID());Thaumcraft.LOGGER.info("KNOWLEDGE_CLIENT_SCAN_OK: real item use, observations={}, discovered terra={}",state.raw("OBSERVATION","BASICS"),state.flags().contains("!terra"));}}
    }
}
