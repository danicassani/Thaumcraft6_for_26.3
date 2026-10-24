package thaumcraft.test;

import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.research.*;

@Mod.EventBusSubscriber(modid="thaumcraft")
public final class ScanProbe {
    static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
    static void tick(Player p,int n){for(int i=0;i<n;i++)p.tick();}
    static int scans(Player p){return ResearchService.state(p).scans().size();}
    @SubscribeEvent public static void start(ServerStartedEvent event){
        if(!System.getProperty("thaumcraft.bookProbe","").equals("scan"))return;
        var server=event.getServer();var level=server.overworld();server.tickRateManager().setFrozen(true);
        var pos=new BlockPos(57008,180,8);for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)level.getChunk((pos.getX()>>4)+x,z);
        Player p=new Player(level,new GameProfile(UUID.randomUUID(),"ScanActions")){public GameType gameMode(){return GameType.SURVIVAL;}};
        p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);p.setNoGravity(true);p.setYRot(0);p.setXRot(0);
        for(int z=0;z<=9;z++)for(int y=0;y<3;y++)level.setBlockAndUpdate(pos.offset(0,y,z),Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pos.offset(0,1,3),Blocks.DIAMOND_BLOCK.defaultBlockState());
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ResearchContent.METER.get()));
        KnowledgeStore.write(p,KnowledgeState.EMPTY);
        ResearchContent.METER.get().use(level,p,InteractionHand.MAIN_HAND);tick(p,10);check(scans(p)==0,"Scan happened before one second");p.releaseUsingItem();tick(p,15);check(scans(p)==0,"Early release granted scan");
        p.setShiftKeyDown(true);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.GOLD_INGOT));
        ResearchContent.METER.get().use(level,p,InteractionHand.MAIN_HAND);p.setShiftKeyDown(false);tick(p,19);check(scans(p)==0,"Other-hand scan completed early");tick(p,1);
        check(ResearchService.state(p).scans().stream().anyMatch(s->s.contains("gold_ingot")),"Shift mode was not latched at start");
        check(p.getOffhandItem().getCount()==1&&p.getMainHandItem().is(ResearchContent.METER.get()),"Scanning consumed or moved held items");
        tick(p,21);int count=scans(p);p.setShiftKeyDown(true);ResearchContent.METER.get().use(level,p,InteractionHand.MAIN_HAND);tick(p,20);check(scans(p)==count,"Duplicate observation awarded twice");
        tick(p,21);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_INGOT));p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ResearchContent.METER.get()));
        ResearchContent.METER.get().use(level,p,InteractionHand.OFF_HAND);tick(p,20);check(ResearchService.state(p).scans().stream().anyMatch(s->s.contains("iron_ingot")),"Offhand meter did not scan the opposite hand");
        tick(p,21);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);count=scans(p);ResearchContent.METER.get().use(level,p,InteractionHand.OFF_HAND);tick(p,20);check(scans(p)==count,"Empty other hand scanned world instead");
        tick(p,21);p.setShiftKeyDown(false);ResearchContent.METER.get().use(level,p,InteractionHand.OFF_HAND);tick(p,20);check(ResearchService.state(p).scans().stream().anyMatch(s->s.contains("diamond_block")),"World target was not scanned after one second");
        thaumcraft.Thaumcraft.LOGGER.info("SCAN57_NATIVE_OK: exact 20 ticks, release cancellation, latched Shift mode, both hands, empty hand, no consumption, no duplicate knowledge, world ray");server.halt(false);
    }
}
