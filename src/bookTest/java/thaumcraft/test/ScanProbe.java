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
        // A physical item resting above a solid floor must win over the floor ray hit.
        KnowledgeStore.write(p,KnowledgeState.EMPTY);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ResearchContent.METER.get()));p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        for(int z=0;z<=9;z++){level.setBlockAndUpdate(pos.offset(0,-1,z),Blocks.STONE.defaultBlockState());for(int y=0;y<3;y++)level.setBlockAndUpdate(pos.offset(0,y,z),Blocks.AIR.defaultBlockState());}
        var drop=p.drop(new ItemStack(Items.DIAMOND,7),true,net.minecraft.util.Prediction.SERVER_ONLY);check(drop!=null&&level.getEntity(drop.getId())==drop,"Native player toss did not create an item entity");drop.setPos(pos.getX()+.5,pos.getY()+.05,pos.getZ()+3.5);drop.setNoGravity(true);drop.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);drop.setPickUpDelay(6000);
        p.setXRot((float)-Math.toDegrees(Math.atan2(drop.getY()+.125-p.getEyeY(),3)));
        var target=ScanTarget.find(p);check(target!=null&&target.entity()==drop,"Floor occluded the dropped item");
        var unseen=ScanInspection.describe(p,target);check(unseen!=null&&unseen.scannable()&&!unseen.known()&&unseen.aspects().isEmpty()&&scans(p)==0,"Read-only preview leaked aspects or awarded knowledge");
        ResearchContent.METER.get().use(level,p,InteractionHand.MAIN_HAND);tick(p,19);check(scans(p)==0,"Dropped scan completed early");tick(p,1);
        check(ResearchService.state(p).scans().contains("item:minecraft:diamond")&&!ResearchService.state(p).scans().contains("entity:minecraft:item"),"Scan did not inspect the dropped stack");
        check(drop.isAlive()&&drop.getItem().getCount()==7&&drop.getItem().is(Items.DIAMOND),"Scanning moved or consumed the ground stack");
        var known=ScanInspection.describe(p,ScanTarget.find(p));check(known.known()&&!known.scannable()&&known.aspects().equals(thaumcraft.alchemy.AspectResolver.item(level,new ItemStack(Items.DIAMOND))),"Known target aspect amounts are incorrect or multiplied by stack size");
        var savedKnowledge=ResearchService.state(p);ResearchContent.METER.get().use(level,p,InteractionHand.MAIN_HAND);tick(p,20);check(ResearchService.state(p).equals(savedKnowledge),"Repeated ground scan awarded more knowledge");
        Player other=new Player(level,new GameProfile(UUID.randomUUID(),"OtherScanner")){public GameType gameMode(){return GameType.SURVIVAL;}};
        var privateView=ScanInspection.describe(other,ScanTarget.find(p));check(!privateView.known()&&privateView.aspects().isEmpty(),"Another player's discovery was exposed");
        level.setBlockAndUpdate(pos.offset(0,0,2),Blocks.STONE.defaultBlockState());level.setBlockAndUpdate(pos.offset(0,1,2),Blocks.STONE.defaultBlockState());
        target=ScanTarget.find(p);check(target!=null&&target.kind()==ScanTarget.BLOCK&&target.entity()!=drop&&!known.matches(target),"Wall failed to block target preview");
        KnowledgeStore.write(p,KnowledgeState.EMPTY);ResearchContent.METER.get().use(level,p,InteractionHand.MAIN_HAND);tick(p,20);check(!ResearchService.state(p).scans().contains("item:minecraft:diamond"),"Scanned item through solid wall");
        level.setBlockAndUpdate(pos.offset(0,0,2),Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(pos.offset(0,1,2),Blocks.AIR.defaultBlockState());
        drop.setItem(new ItemStack(Items.EMERALD));check(!known.matches(ScanTarget.find(p)),"Changed dropped stack retained stale aspect preview");
        drop.setPos(pos.getX()+.5,pos.getY()+.05,pos.getZ()+12.5);target=ScanTarget.find(p);check(target==null||target.entity()!=drop,"Out-of-range item could be inspected");drop.discard();
        thaumcraft.Thaumcraft.LOGGER.info("SIGHT61_NATIVE_OK: ground stack, twenty-tick scan, seven items retained, unit aspects, no duplicate knowledge, private discovery, solid occlusion, changed stack and range");
        thaumcraft.Thaumcraft.LOGGER.info("SCAN57_NATIVE_OK: exact 20 ticks, release cancellation, latched Shift mode, both hands, empty hand, no consumption, no duplicate knowledge, world ray");server.halt(false);
    }
}
