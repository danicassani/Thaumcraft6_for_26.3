package thaumcraft.test;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.aura.*;
import thaumcraft.crystals.*;
import thaumcraft.devices.*;

/** Isolated opt-in visual scene; no gameplay/demo blocks enter the production JAR. */
@Mod.EventBusSubscriber(modid = Thaumcraft.MODID)
public final class M05ClientScene {
    private static final Set<java.util.UUID> PREPARED = new HashSet<>();
    public static volatile BlockPos center;
    private M05ClientScene() {}
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent.Post event) {
        if (!System.getProperty("thaumcraft.m05Probe", "").equals("client") || !(event.player() instanceof ServerPlayer player) || !PREPARED.add(player.getUUID())) return;
        var level = player.level(); var base = player.blockPosition(); center = base;
        for (int x=-5;x<=5;x++) for(int z=-1;z<=7;z++) {
            level.setBlockAndUpdate(base.offset(x,-1,z),Blocks.STONE.defaultBlockState());
            for(int y=0;y<=4;y++)level.setBlockAndUpdate(base.offset(x,y,z),Blocks.AIR.defaultBlockState());
        }
        String[] aspects={"aer","ignis","aqua","terra","ordo","perditio","vitium"};
        for(int i=0;i<aspects.length;i++) {
            var pos=base.offset(i-3,0,4);var block=CrystalContent.BLOCKS.get(aspects[i]).get();
            level.setBlockAndUpdate(pos,block.supportedState(block.defaultBlockState().setValue(CrystalBlock.SIZE,3),level,pos));
        }
        level.setBlockAndUpdate(base.offset(-2,0,2),DeviceContent.DIOPTRA.get().defaultBlockState());
        level.setBlockAndUpdate(base.offset(0,0,2),DeviceContent.GENERATOR.get().defaultBlockState().setValue(AuraDeviceBlock.FACING,Direction.UP));
        var pedestalPos=base.offset(2,0,2);level.setBlockAndUpdate(pedestalPos,DeviceContent.PEDESTAL.get().defaultBlockState());
        var pedestal=(RechargePedestalEntity)level.getBlockEntity(pedestalPos);
        pedestal.getCapability(ForgeCapabilities.ITEM_HANDLER,Direction.UP).ifPresent(handler->handler.insertItem(0,new ItemStack(M05TestRegistry.rechargeable),false));
        AuraData.get(level).put(AuraCell.key(base.getX()>>4,base.getZ()>>4),new AuraCell(200,450,250));
        ((DioptraEntity)level.getBlockEntity(base.offset(-2,0,2))).scan();
        player.teleportTo(level,player.getX(),player.getY()+1,player.getZ(),Set.of(),0,25,true);
        player.getAbilities().flying=true;player.onUpdateAbilities();
        // Actual component-based variants are given to verify model selection and names.
        player.getInventory().setItem(0,CrystalContent.essence("aer",1));player.getInventory().setItem(1,CrystalContent.essence("ignis",1));
        player.getInventory().setItem(2,CrystalContent.essence("vitium",1));player.inventoryMenu.broadcastChanges();
        Thaumcraft.LOGGER.info("M05_CLIENT_SCENE_READY: {}",base);
    }
}
