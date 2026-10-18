package thaumcraft.test;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.*;
import thaumcraft.essentia.*;

@Mod.EventBusSubscriber(modid=Thaumcraft.MODID)
public final class EssentiaClientScene{
    public static final BlockPos C=new BlockPos(8,205,11),J=C.east(3).below();
    static final Set<UUID> ready=new HashSet<>(),opened=new HashSet<>(),flowed=new HashSet<>();
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent.Post e){
        if(!System.getProperty("thaumcraft.essentiaProbe","").equals("client")||!(e.player() instanceof ServerPlayer p)||p.tickCount<60)return;var level=p.level();
        if(ready.add(p.getUUID())){
            level.getServer().tickRateManager().setFrozen(false);level.dimensionType().defaultClock().ifPresent(clock->level.getServer().clockManager().setTotalTicks(clock,6000));
            for(var b:BlockPos.betweenClosed(C.offset(-5,-2,-4),C.offset(6,4,4)))level.setBlockAndUpdate(b,b.getY()==203?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());
            // Keep the player at y=205 while the lower jar remains visible.
            for(int x=-2;x<=2;x++)for(int z=-4;z<=-2;z++)level.setBlockAndUpdate(C.offset(x,-1,z),Blocks.STONE.defaultBlockState());
            var source=EssentiaProbe.machine(level,C.west(2),AlchemyContent.ALEMBIC.get());source.insertEssence("aer",128,false);
            var filter=EssentiaProbe.tube(level,C.west(),"tube_filter",Direction.NORTH);filter.setFilter("aer");
            var valve=EssentiaProbe.tube(level,C,"tube_valve",Direction.NORTH);valve.toggleValve();
            EssentiaProbe.tube(level,C.east(),"tube_oneway",Direction.EAST);EssentiaProbe.tube(level,C.east(2),"tube",Direction.NORTH);EssentiaProbe.tube(level,C.east(4).south(2),"tube_buffer",Direction.NORTH);EssentiaProbe.tube(level,C.east(3),"tube_restrict",Direction.NORTH);
            var jar=EssentiaProbe.machine(level,J,AlchemyContent.JAR.get());EssentiaProbe.label(p,jar,"aer");level.setBlockAndUpdate(J,jar.getBlockState().setValue(AlchemyBlock.FACING,Direction.NORTH));
            p.getInventory().clearContent();p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.getInventory().setItem(1,new ItemStack(EssentiaContent.RESONATOR.get()));p.getInventory().setItem(2,AlchemyContent.filled(AlchemyContent.PHIAL.get(),"aer",10));p.getInventory().setItem(3,EssentiaContent.label("aer"));p.inventoryMenu.broadcastChanges();
            p.teleportTo(level,8.5,205,8.5,Set.of(),0,21.3F,true);
            Thaumcraft.LOGGER.info("ESSENTIA_CLIENT_SCENE_READY: closed valve at crosshair; empty-hand right click opens native flow");
        }
        var valve=(TubeEntity)level.getBlockEntity(C);var jar=(AlchemyMachine)level.getBlockEntity(J);
        if(valve.snapshot().flow()&&opened.add(p.getUUID()))Thaumcraft.LOGGER.info("ESSENTIA_CLIENT_USE_OK: real block interaction opened valve");
        if(jar.total()>=20&&flowed.add(p.getUUID())){level.getServer().tickRateManager().setFrozen(true);Thaumcraft.LOGGER.info("ESSENTIA_CLIENT_FLOW_OK: native tickers delivered {} aer to labeled jar",jar.total());}
    }
}
