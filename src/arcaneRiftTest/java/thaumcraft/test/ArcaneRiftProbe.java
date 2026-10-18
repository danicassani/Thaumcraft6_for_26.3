package thaumcraft.test;

import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.*;
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
public final class ArcaneRiftProbe {
    public static final BlockPos TABLE=new BlockPos(8,205,8),SIPHON=new BlockPos(8,205,20);
    private static final UUID RIFT_UUID=UUID.fromString("d4ef52ef-0196-4eb7-96ac-089da769ead7");
    private static boolean readChecked, spawnChecked, writeStarted;
    private static int readTicks;
    private static String mode(){return System.getProperty("thaumcraft.arcaneRiftProbe","");}
    @SubscribeEvent public static void started(ServerStartedEvent event){
        if(!mode().equals("write")&&!mode().equals("read"))return;
        var level=event.getServer().overworld();for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)level.getChunk(x,z);
        // Keep simulated time fixed while testing exact resource transactions and saved values.
        event.getServer().tickRateManager().setFrozen(true);
        level.setChunkForced(0,0,true);level.setChunkForced(0,1,true);
    }
    private static void runWriteTests(ServerLevel level) {
        for(var entity:level.getEntitiesOfClass(FluxRiftEntity.class,new AABB(-32,0,-32,130,320,130)))entity.discard();
        for(var pos:List.of(TABLE,TABLE.above(),SIPHON))level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
        var player=new Player(level,new GameProfile(UUID.fromString("a9712d81-0e84-41b1-a7ed-31a8a265a066"),"ArcaneProbe")) { @Override public GameType gameMode(){return GameType.SURVIVAL;} };
        var known=new java.util.HashMap<String,Integer>();for(var entry:thaumcraft.research.ResearchCatalog.ENTRIES)known.put(entry.key(),entry.stages().size()+1);
        thaumcraft.research.KnowledgeStore.write(player,new thaumcraft.research.KnowledgeState(1,known,java.util.Set.of(),java.util.Set.of(),java.util.Set.of(),java.util.Map.of()));
        player.setPos(TABLE.getX()+.5,TABLE.getY()+1,TABLE.getZ()+.5);
        level.setBlockAndUpdate(TABLE,ArcaneContent.WORKBENCH.get().defaultBlockState());var table=(ArcaneWorkbenchEntity)level.getBlockEntity(TABLE);
        tableTests(level,table,player);salisTests(level);siphonTests(level);
        table=(ArcaneWorkbenchEntity)level.getBlockEntity(TABLE);
        // Keep stable fixtures for a complete server shutdown and reload.
        clear(table);table.inventory.setItem(0,new ItemStack(Items.DIAMOND,7));table.inventory.setItem(9,CrystalContent.essence("aer",3));
        var siphon=(VoidSiphonEntity)level.getBlockEntity(SIPHON);var tag=siphon.saveWithoutMetadata(level.registryAccess());tag.putInt("progress",1234);siphon.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,level.registryAccess(),tag));siphon.setChanged();
        var rift=(FluxRiftEntity)level.getEntity(RIFT_UUID);rift.setSize(40);rift.setSeed(42);rift.setStability(-12.5F);
        Thaumcraft.LOGGER.info("ARCANE_RIFT_RUNTIME_OK: arcane/vanilla recipes, crystals, no partial debit, 3x3 charger/fractions, salis, blocked/full/redstone siphon, output-only automation, collapse; waiting for entity chunk load to test natural spawning");
    }
    private static void aura(ServerLevel level,float vis){for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)AuraData.get(level).put(AuraCell.key(x,z),new AuraCell(200,vis,0));}
    private static float total(ServerLevel level){float n=0;for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)n+=AuraManager.get(level,x,z).vis();return n;}
    private static void clear(ArcaneWorkbenchEntity table){table.inventory.clearContent();table.inventory.setChanged();}
    private static void tableTests(ServerLevel level,ArcaneWorkbenchEntity table,Player player){
        clear(table);table.inventory.setItem(1,new ItemStack(Items.STRING));table.inventory.setItem(3,new ItemStack(Items.STRING));table.inventory.setItem(4,new ItemStack(Blocks.WOOL.pick(DyeColor.WHITE)));table.inventory.setItem(5,new ItemStack(Items.STRING));table.inventory.setItem(7,new ItemStack(Items.STRING));
        aura(level,2.75F);require(table.match()!=null&&table.match().vis()==5,"Arcane recipe not loaded");
        require(level.recipeAccess().getRecipeFor(RecipeType.CRAFTING,table.input().input(),level).isEmpty(),"Vanilla table bypasses arcane cost");
        float before=total(level);require(!table.craft(player)&&total(level)==before&&table.inventory.getItem(1).getCount()==1,"Insufficient aura consumed resources");
        level.setBlockAndUpdate(TABLE.above(),ArcaneContent.CHARGER.get().defaultBlockState());
        require(table.charged()&&table.availableVis()==18,"Charger did not sum nine chunks");
        require(table.craft(player)&&total(level)==before-5&&table.inventory.getItem(15).is(SimpleContent.ITEMS.get("fabric").get()),"Charged arcane craft/debit failed");
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)require(AuraManager.get(level,x,z).vis()%1==.75F,"Fractional aura lost");
        require(!table.craft(player)&&total(level)==before-5,"Repeated packet duplicated output");
        clear(table);table.inventory.setItem(0,new ItemStack(SimpleContent.ITEMS.get("plate_iron").get(),2));table.inventory.setItem(1,new ItemStack(Items.QUARTZ,2));aura(level,100.75F);before=total(level);
        table.inventory.setItem(9,CrystalContent.essence("aer",2));table.inventory.setItem(11,CrystalContent.essence("terra",2));
        require(!table.craft(player)&&total(level)==before,"Wrong crystal accepted");table.inventory.setItem(11,CrystalContent.essence("aqua",2));
        require(table.craft(player)&&total(level)==before-50&&table.inventory.getItem(9).getCount()==1&&table.inventory.getItem(11).getCount()==1,"Crystal/vis debit incorrect");
        table.inventory.setItem(15,new ItemStack(Items.STONE,64));before=total(level);require(!table.craft(player)&&total(level)==before&&table.inventory.getItem(0).getCount()==1,"Full output consumed inputs");
        player.setPos(1000,205,1000);require(!table.craft(player),"Remote craft accepted");player.setPos(8.5,206,8.5);
        clear(table);for(int i=0;i<3;i++)table.inventory.setItem(i,new ItemStack(Items.MILK_BUCKET));table.inventory.setItem(3,new ItemStack(Items.SUGAR));table.inventory.setItem(4,new ItemStack(Items.EGG));table.inventory.setItem(5,new ItemStack(Items.SUGAR));for(int i=6;i<9;i++)table.inventory.setItem(i,new ItemStack(Items.WHEAT));
        before=total(level);require(table.craft(player)&&table.inventory.getItem(15).is(Items.CAKE)&&total(level)==before,"Vanilla recipe charged aura or failed");for(int i=0;i<3;i++)require(table.inventory.getItem(i).is(Items.BUCKET),"Crafting remainder lost");
        require(!table.spendVis(-1),"Negative cost accepted");
        var unloaded=new BlockPos(160000,205,160000);require(AuraManager.get(level,unloaded)==null,"Precondition: distant chunk already loaded");require(!RiftSpawning.trySpawn(level,10000,10000)&&level.getChunkSource().getChunkNow(10000,10000)==null,"Spawning loaded terrain");
        level.setBlockAndUpdate(TABLE,Blocks.AIR.defaultBlockState());require(!ArcaneContent.CHARGER.get().defaultBlockState().canSurvive(level,TABLE.above()),"Unsupported charger survived");level.getBlockState(TABLE.above()).tick(level,TABLE.above(),level.getRandom());require(level.getBlockState(TABLE.above()).isAir(),"Charger did not break");
        level.setBlockAndUpdate(TABLE,ArcaneContent.WORKBENCH.get().defaultBlockState());
    }
    private static void salisTests(ServerLevel level){
        var items=new ArrayList<>(List.of(new ItemStack(Items.FLINT),new ItemStack(Items.BOWL),new ItemStack(Items.REDSTONE),CrystalContent.essence("aer",1),CrystalContent.essence("terra",1),CrystalContent.essence("ignis",1)));
        var recipe=new SalisRecipe();var input=CraftingInput.of(3,2,items);require(recipe.matches(input,level),"Salis recipe failed");var remains=recipe.getRemainingItems(input);require(remains.get(0).is(Items.FLINT)&&remains.get(1).is(Items.BOWL),"Salis tools consumed");
        items.set(5,CrystalContent.essence("aer",1));require(!recipe.matches(CraftingInput.of(3,2,items),level),"Duplicate primal crystals accepted");
    }
    private static void siphonTests(ServerLevel level){
        for(int x=7;x<=14;x++)for(int y=204;y<211;y++)for(int z=19;z<=22;z++)level.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(SIPHON,DeviceContent.SIPHON.get().defaultBlockState());var siphon=(VoidSiphonEntity)level.getBlockEntity(SIPHON);
        var rift=new FluxRiftEntity(RiftContent.RIFT.get(),level);rift.setUUID(RIFT_UUID);rift.setPos(12.5,207.5,20.5);rift.setSize(100);rift.setSeed(331);require(level.addFreshEntity(rift),"Rift spawn failed");
        level.setBlockAndUpdate(SIPHON.east().above(),Blocks.STONE.defaultBlockState());level.setBlockAndUpdate(SIPHON.east().above(2),Blocks.STONE.defaultBlockState());siphon.harvest();require(siphon.progress()==0,"Siphon harvested through wall");level.setBlockAndUpdate(SIPHON.east().above(),Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(SIPHON.east().above(2),Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(SIPHON,level.getBlockState(SIPHON).setValue(AuraDeviceBlock.POWERED,true));siphon.harvest();require(siphon.progress()==0,"Redstone ignored");level.setBlockAndUpdate(SIPHON,level.getBlockState(SIPHON).setValue(AuraDeviceBlock.POWERED,false));
        for(int i=0;i<200;i++){rift.setSize(100);siphon.harvest();}require(siphon.output().is(SimpleContent.ITEMS.get("void_seed").get())&&siphon.output().getCount()==1&&siphon.progress()==0&&rift.stability()<0,"Siphon did not produce a seed and destabilize rift");
        var handler=siphon.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);require(!handler.insertItem(0,new ItemStack(Items.DIAMOND),false).isEmpty(),"Siphon accepted insertion");require(handler.extractItem(0,1,true).getCount()==1&&siphon.output().getCount()==1,"Simulated extraction changed output");require(handler.extractItem(0,1,false).getCount()==1&&siphon.output().isEmpty(),"Output extraction failed");
        var tag=siphon.saveWithoutMetadata(level.registryAccess());tag.putInt("progress",1995);siphon.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,level.registryAccess(),tag));rift.setSize(100);siphon.harvest();require(siphon.progress()==5&&siphon.output().getCount()==1,"Siphon overflow lost");
        var saved=siphon.saveWithoutMetadata(level.registryAccess());var full=saved.copy();full.put("output",ItemStack.CODEC.encodeStart(level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),new ItemStack(SimpleContent.ITEMS.get("void_seed").get(),64)).getOrThrow());
        siphon.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,level.registryAccess(),full));float stability=rift.stability();siphon.harvest();require(siphon.output().getCount()==64&&siphon.progress()==5&&rift.stability()==stability,"Full siphon continued harvesting");
        siphon.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,level.registryAccess(),saved));
        var test=new FluxRiftEntity(RiftContent.RIFT.get(),level);test.setPos(30,210,30);test.setSize(4);test.beginCollapse();level.addFreshEntity(test);for(int i=0;i<4&&!test.isRemoved();i++)test.tick();require(test.isRemoved(),"Rift collapse did not complete");
        try{rift.setStability(Float.NaN);throw new AssertionError("NaN accepted");}catch(IllegalArgumentException expected){}
        require(RiftShape.create(42,50).equals(RiftShape.create(42,50)),"Rift mesh not deterministic");
    }
    private static void spawningTests(ServerLevel level) {
        // A real aura tick must trigger generation; the test contains no spawning replacement.
        AuraData.get(level).put(AuraCell.key(0,0),new AuraCell(1,0,32766));
        thaumcraft.config.ThaumcraftConfig.generateFluxRifts.set(false);AuraManager.tick(level);
        require(level.getEntitiesOfClass(FluxRiftEntity.class,new AABB(0,-64,0,16,180,16)).isEmpty(),"Rift config ignored");
        thaumcraft.config.ThaumcraftConfig.generateFluxRifts.set(true);AuraManager.tick(level);
        var natural=level.getEntitiesOfClass(FluxRiftEntity.class,new AABB(0,-64,0,16,180,16));require(!natural.isEmpty(),"Excess flux did not create a rift");
        require(!RiftSpawning.trySpawn(level,0,0),"Rift exclusion distance ignored");natural.forEach(FluxRiftEntity::discard);
        aura(level,100);
        Thaumcraft.LOGGER.info("ARCANE_RIFT_SPAWNING_OK: real aura tick, config and rift exclusion distance");
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post event){
        var level=event.server().overworld();
        if(mode().equals("write")&&!writeStarted&&level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.pack(0,0))&&level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.pack(0,1))) {runWriteTests(level);writeStarted=true;}
        if(mode().equals("write")&&writeStarted&&!spawnChecked&&level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.pack(0,0))) { spawningTests(level);spawnChecked=true; }
        if(!mode().equals("read")||readChecked||++readTicks<40||!level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.pack(0,1)))return;
        var table=(ArcaneWorkbenchEntity)level.getBlockEntity(TABLE);var siphon=(VoidSiphonEntity)level.getBlockEntity(SIPHON);var rift=level.getEntity(RIFT_UUID);
        require(table!=null&&table.inventory.getItem(0).is(Items.DIAMOND)&&table.inventory.getItem(0).getCount()==7&&CrystalContent.aspect(table.inventory.getItem(9)).equals("aer"),"Table inventory lost on restart");
        require(siphon!=null&&siphon.progress()==1234&&siphon.output().getCount()==1,"Siphon progress/output lost");
        require(rift instanceof FluxRiftEntity flux&&flux.size()==40&&flux.seed()==42&&flux.stability()==-12.5F,"Rift state lost on restart: "+(rift instanceof FluxRiftEntity flux?"size="+flux.size()+",seed="+flux.seed()+",stability="+flux.stability():rift));readChecked=true;
        Thaumcraft.LOGGER.info("ARCANE_RIFT_RESTART_OK: table inventory/components, siphon progress/output, rift UUID/size/seed/fractional stability");
    }
    @SubscribeEvent public static void stopping(ServerStoppingEvent event){if(mode().equals("write"))Thaumcraft.LOGGER.info("ARCANE_RIFT_SAVE_READY");}
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
