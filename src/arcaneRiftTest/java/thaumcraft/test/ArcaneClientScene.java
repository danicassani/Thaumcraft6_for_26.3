package thaumcraft.test;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.aura.*;
import thaumcraft.crafting.*;
import thaumcraft.content.SimpleContent;
import thaumcraft.crystals.CrystalContent;
import thaumcraft.devices.*;
import thaumcraft.rifts.*;

@Mod.EventBusSubscriber(modid=Thaumcraft.MODID)
public final class ArcaneClientScene {
    public static volatile BlockPos center;
    private static final Set<UUID> prepared=new HashSet<>();
    private static final Map<UUID,Integer> ticks=new HashMap<>();
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent.Post event){
        if(!System.getProperty("thaumcraft.arcaneRiftProbe","").equals("client") || !(event.player() instanceof ServerPlayer player))return;
        int elapsed=ticks.merge(player.getUUID(),1,Integer::sum);
        if(prepared.add(player.getUUID())) {
        var known=new java.util.HashMap<String,Integer>();for(var entry:thaumcraft.research.ResearchCatalog.ENTRIES)known.put(entry.key(),entry.stages().size()+1);
        thaumcraft.research.KnowledgeStore.write(player,new thaumcraft.research.KnowledgeState(1,known,java.util.Set.of(),java.util.Set.of(),java.util.Set.of(),java.util.Map.of()));
            var level=player.level();var base=new BlockPos(8,205,8);center=base;
            for(int x=-7;x<=7;x++)for(int z=-2;z<=10;z++) {level.setBlockAndUpdate(base.offset(x,-1,z),Blocks.STONE.defaultBlockState());for(int y=0;y<9;y++)level.setBlockAndUpdate(base.offset(x,y,z),Blocks.AIR.defaultBlockState());}
            level.setBlockAndUpdate(base.offset(-2,0,3),ArcaneContent.WORKBENCH.get().defaultBlockState());level.setBlockAndUpdate(base.offset(-2,1,3),ArcaneContent.CHARGER.get().defaultBlockState());
            var table=(ArcaneWorkbenchEntity)level.getBlockEntity(base.offset(-2,0,3));table.inventory.setItem(0,new ItemStack(SimpleContent.ITEMS.get("plate_iron").get(),4));table.inventory.setItem(1,new ItemStack(Items.QUARTZ,4));table.inventory.setItem(9,CrystalContent.essence("aer",4));table.inventory.setItem(11,CrystalContent.essence("aqua",4));
            level.setBlockAndUpdate(base.offset(2,0,3),DeviceContent.SIPHON.get().defaultBlockState());
            var rift=new FluxRiftEntity(RiftContent.RIFT.get(),level);rift.setPos(base.getX()+3.5,base.getY()+3,base.getZ()+6.5);rift.setSize(80);rift.setSeed(42);rift.setStability(40);level.addFreshEntity(rift);
            AuraData.get(level).put(AuraCell.key(0,0),new AuraCell(200,450,0));
            player.teleportTo(level,base.getX()+.5,base.getY()+2,base.getZ()-1,Set.of(),0,18,true);player.getAbilities().flying=true;player.onUpdateAbilities();
            Thaumcraft.LOGGER.info("ARCANE_CLIENT_SCENE_READY: {}",base);
        }
        if(elapsed==120) {var table=(ArcaneWorkbenchEntity)player.level().getBlockEntity(center.offset(-2,0,3));player.openMenu(table);}
    }
}
