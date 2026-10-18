package thaumcraft.test;

import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.*;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.aura.*;
import thaumcraft.crafting.*;
import thaumcraft.content.SimpleContent;
import thaumcraft.crystals.CrystalContent;
import thaumcraft.research.*;

@Mod.EventBusSubscriber(modid=Thaumcraft.MODID)
public final class KnowledgeProbe {
    private static final UUID ID=UUID.fromString("fb504654-807d-410b-a845-06d325b85fc2");
    public static final BlockPos TABLE=new BlockPos(8,205,8);
    private static String mode(){return System.getProperty("thaumcraft.knowledgeProbe","");}
    @SubscribeEvent public static void start(ServerStartedEvent event){
        if(!mode().equals("write")&&!mode().equals("read"))return;
        var server=event.getServer();var level=server.overworld();server.tickRateManager().setFrozen(true);
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)level.getChunk(x,z);
        var real=new ServerPlayer(server,level,new GameProfile(ID,"KnowledgeProbe"),ClientInformation.createDefault());var storage=server.getPlayerList().getPlayerIo();
        if(mode().equals("read")){
            var saved=storage.load(new NameAndId(ID,"KnowledgeProbe")).orElseThrow();real.load(TagValueInput.create(ProblemReporter.DISCARDING,server.registryAccess(),saved));
            var state=KnowledgeStore.read(real).getOrThrow();require(ResearchCatalog.knows(state,"FIRSTSTEPS")&&state.flags().contains("!aer")&&state.crafts().contains("thaumcraft:thaumometer")&&state.scans().contains("item:thaumcraft:crystal_essence/aer"),"Knowledge lost on reload");
            var clone=new ServerPlayer(server,level,new GameProfile(ID,"KnowledgeProbe"),ClientInformation.createDefault());PlayerEvent.Clone.BUS.post(new PlayerEvent.Clone(clone,real,true));require(KnowledgeStore.read(clone).getOrThrow().equals(state),"Knowledge lost on death clone");
            Thaumcraft.LOGGER.info("KNOWLEDGE_RESTART_OK: real player file, stages, aspect discoveries, scan identities, raw points, crafts and death clone");return;
        }
        var player=new Player(level,new GameProfile(UUID.randomUUID(),"KnowledgeActions")){@Override public GameType gameMode(){return GameType.SURVIVAL;}};player.setPos(8.5,206,8.5);
        require(KnowledgeStore.read(player).getOrThrow().equals(KnowledgeState.EMPTY)&&player.getPersistentData().get(KnowledgeStore.ID)==null,"Reading created state");
        bootstrap(level,player);scanAndProgress(level,player);recipeLocks(level,player);atomicCosts(player);rays(level,player);
        var state=KnowledgeStore.read(player).getOrThrow();var original=player.getPersistentData().get(KnowledgeStore.ID).copy();var invalid=StringTag.valueOf("corrupt data preserved");player.getPersistentData().put(KnowledgeStore.ID,invalid);
        require(KnowledgeStore.read(player).result().isEmpty()&&!KnowledgeStore.write(player,KnowledgeState.EMPTY)&&!ResearchService.advance(player,"FIRSTSTEPS"),"Corrupt state accepted or overwritten");require(player.getPersistentData().get(KnowledgeStore.ID).equals(invalid),"Corrupt state changed");player.getPersistentData().put(KnowledgeStore.ID,original);
        require(KnowledgeStore.write(real,state),"Real player save failed");storage.save(real);require(storage.load(new NameAndId(ID,"KnowledgeProbe")).isPresent(),"Missing player file");
        Thaumcraft.LOGGER.info("KNOWLEDGE_RUNTIME_OK: dream/book/table bootstrap, original First Steps stages, gated meter craft, unique primal scans, exact observations, atomic requirements, locked recipe no debit, ray occlusion, scan-only entries, corrupt-save protection");
    }
    private static UseOnContext context(Player player){return new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(TABLE),Direction.UP,TABLE,false));}
    private static void bootstrap(ServerLevel level,Player player){
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ArcaneContent.SALIS.get(),2));level.setBlockAndUpdate(TABLE,Blocks.BOOKSHELF.defaultBlockState());
        require(ArcaneContent.SALIS.get().useOn(context(player))==InteractionResult.FAIL&&player.getMainHandItem().getCount()==2&&level.getBlockState(TABLE).is(Blocks.BOOKSHELF),"Book bypassed dream or consumed ingredients on failure");
        require(!ResearchService.advance(player,"FIRSTSTEPS"),"Parents bypassed");ResearchService.flag(player,"!gotcrystals");net.minecraftforge.event.entity.player.PlayerWakeUpEvent.BUS.post(new net.minecraftforge.event.entity.player.PlayerWakeUpEvent(player,false,true));
        boolean journal=false;for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i).is(Items.WRITTEN_BOOK))journal=true;require(journal,"Wake event did not provide dream journal");
        ArcaneContent.SALIS.get().useOn(context(player));require(ResearchService.hasBook(player)&&player.getMainHandItem().getCount()==1&&level.getBlockState(TABLE).isAir(),"Salis did not create book");
        ResearchService.openBook(player);require(ResearchService.knows(player,"FIRSTSTEPS@1")&&ResearchService.knows(player,"KNOWLEDGETYPES"),"Book bootstrap failed");
        level.setBlockAndUpdate(TABLE,Blocks.CRAFTING_TABLE.defaultBlockState());ArcaneContent.SALIS.get().useOn(context(player));
        require(level.getBlockEntity(TABLE) instanceof ArcaneWorkbenchEntity&&ResearchService.state(player).crafts().contains("thaumcraft:arcane_workbench"),"Workbench proof missing");
        require(ResearchService.advance(player,"FIRSTSTEPS")&&ResearchService.knows(player,"FIRSTSTEPS@2"),"Workbench did not advance first stage");
        require(!ResearchService.advance(player,"FIRSTSTEPS"),"Missing craft/observations accepted");
        var table=(ArcaneWorkbenchEntity)level.getBlockEntity(TABLE);table.inventory.clearContent();
        for(int i:List.of(1,3,5,7))table.inventory.setItem(i,new ItemStack(Items.GOLD_INGOT));table.inventory.setItem(4,new ItemStack(Items.GLASS_PANE));
        for(int i=0;i<6;i++)table.inventory.setItem(9+i,CrystalContent.essence(ArcaneRecipe.PRIMALS.get(i),2));
        AuraData.get(level).put(AuraCell.key(0,0),new AuraCell(200,200.75F,0));require(table.craft(player)&&table.inventory.getItem(15).is(ResearchContent.METER.get())&&AuraManager.get(level,TABLE).vis()==180.75F,"Gated thaumometer crafting failed");
        require(ResearchService.state(player).crafts().contains("thaumcraft:thaumometer"),"Meter craft not recorded");
    }
    private static void scanAndProgress(ServerLevel level,Player player){
        for(var aspect:ArcaneRecipe.PRIMALS)require(ScanningService.observe(player,CrystalContent.essence(aspect,1)),"Primal scan failed: "+aspect);
        require(ScanningService.observe(player,new ItemStack(Blocks.STONE))&&ScanningService.observe(player,new ItemStack(Blocks.DIRT)),"Material scans failed");
        var before=ResearchService.state(player);require(before.raw("OBSERVATION","BASICS")==16,"Original observation formula differs");
        for(var aspect:ArcaneRecipe.PRIMALS)require(!ScanningService.observe(player,CrystalContent.essence(aspect,64)),"Duplicate scan awarded knowledge");require(ResearchService.state(player).equals(before),"Repeat scan changed state");
        require(ResearchService.advance(player,"FIRSTSTEPS")&&ResearchService.knows(player,"FIRSTSTEPS"),"First Steps could not complete");require(ResearchService.state(player).raw("OBSERVATION","BASICS")==0,"Knowledge was not spent exactly");require(!ResearchService.advance(player,"FIRSTSTEPS"),"Completed research repeated");
        require(!ResearchService.advance(player,"!Wisp"),"Scan-only discovery unlocked through book");
        var menu=new ResearchMenu(3,player.getInventory());var state=ResearchService.state(player);require(!menu.clickMenuButton(player,Integer.MAX_VALUE),"Invalid button accepted");require(ResearchService.state(player).equals(state),"Invalid packet changed state");
    }
    private static void recipeLocks(ServerLevel level,Player player){
        var table=(ArcaneWorkbenchEntity)level.getBlockEntity(TABLE);table.inventory.clearContent();table.inventory.setItem(0,new ItemStack(SimpleContent.ITEMS.get("plate_iron").get()));table.inventory.setItem(1,new ItemStack(Items.QUARTZ));table.inventory.setItem(9,CrystalContent.essence("aer",1));table.inventory.setItem(11,CrystalContent.essence("aqua",1));
        float before=AuraManager.get(level,TABLE).vis();require(!table.craft(player)&&AuraManager.get(level,TABLE).vis()==before&&table.inventory.getItem(0).getCount()==1,"Locked recipe consumed resources");
        var state=ResearchService.state(player);KnowledgeStore.write(player,state.stage("UNLOCKAUROMANCY",1));require(!table.craft(player),"Recipe ignored required @stage");KnowledgeStore.write(player,state.stage("UNLOCKAUROMANCY",2));
        require(table.craft(player)&&table.inventory.getItem(15).is(SimpleContent.ITEMS.get("vis_resonator").get())&&AuraManager.get(level,TABLE).vis()==before-50,"Correct research did not unlock recipe");
    }
    private static void atomicCosts(Player player){
        var state=ResearchService.state(player).flag("ORE").flag("!ORECRYSTAL").stage("INFUSION",ResearchCatalog.BY_KEY.get("INFUSION").stages().size()+1).stage("CRYSTALFARMER",1).addPoints("OBSERVATION","AUROMANCY",16);
        KnowledgeStore.write(player,state);for(int i=0;i<6;i++)player.getInventory().setItem(4+i,CrystalContent.essence(ArcaneRecipe.PRIMALS.get(i),2));
        require(!ResearchService.advance(player,"CRYSTALFARMER"),"Missing knowledge accepted");for(int i=0;i<6;i++)require(player.getInventory().getItem(4+i).getCount()==2,"Failed stage consumed crystals");require(ResearchService.state(player).equals(state),"Failed stage consumed points");
        KnowledgeStore.write(player,state.addPoints("OBSERVATION","BASICS",16));require(ResearchService.advance(player,"CRYSTALFARMER"),"Valid crystal requirements failed");for(int i=0;i<6;i++)require(player.getInventory().getItem(4+i).getCount()==1,"Crystal count incorrect");
    }
    private static void rays(ServerLevel level,Player player){
        player.setPos(8.5,206,8.5);player.setYRot(0);player.setXRot(0);var wall=new BlockPos(8,(int)Math.floor(player.getEyeY()),12);level.setBlockAndUpdate(wall,Blocks.STONE.defaultBlockState());
        var item=new ItemEntity(level,8.5,player.getEyeY(),14.5,new ItemStack(Items.DIAMOND));level.addFreshEntity(item);
        var before=ResearchService.state(player);ScanningService.scan(player);require(!ResearchService.state(player).scans().contains("item:minecraft:diamond"),"Scanner saw through wall");
        level.setBlockAndUpdate(wall,Blocks.AIR.defaultBlockState());require(ScanningService.scan(player)&&ResearchService.state(player).scans().contains("item:minecraft:diamond"),"Visible item entity not scanned");item.discard();
        player.setPos(160000,206,160000);ScanningService.scan(player);require(level.getChunkSource().getChunkNow(10000,10000)==null,"Scan loaded distant terrain");player.setPos(8.5,206,8.5);
    }
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
