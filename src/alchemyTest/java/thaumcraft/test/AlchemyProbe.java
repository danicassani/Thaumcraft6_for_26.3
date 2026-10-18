package thaumcraft.test;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.*;
import thaumcraft.aura.*;
import thaumcraft.content.SimpleContent;
import thaumcraft.crafting.*;
import thaumcraft.crystals.*;
import thaumcraft.research.*;
import thaumcraft.trees.*;

@Mod.EventBusSubscriber(modid=Thaumcraft.MODID)
public final class AlchemyProbe {
    public static final BlockPos POS=new BlockPos(8,205,8);
    private static final java.util.List<AlumentumProjectile> THROWN=new java.util.ArrayList<>();
    private static final java.util.List<ItemEntity> SPAWNED=new java.util.ArrayList<>();
    @SubscribeEvent public static void joined(net.minecraftforge.event.entity.EntityJoinLevelEvent e){if(System.getProperty("thaumcraft.alchemyProbe","").equals("write")){if(e.getEntity() instanceof ItemEntity item)SPAWNED.add(item);if(e.getEntity() instanceof AlumentumProjectile shot)THROWN.add(shot);}}
    private static final UUID ID=UUID.fromString("fb504654-807d-410b-a845-06d325b85fc4");
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    private static AlchemyMachine machine(ServerLevel level,BlockPos p,AlchemyBlock block){level.setBlockAndUpdate(p,Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(p,block.defaultBlockState());return (AlchemyMachine)level.getBlockEntity(p);}
    private static void load(AlchemyMachine m,AlchemyState state,ServerLevel level){var tag=m.saveWithoutMetadata(level.registryAccess());tag.put("alchemy",AlchemyState.CODEC.encodeStart(NbtOps.INSTANCE,state).getOrThrow());m.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),tag));m.setChanged();}
    @SubscribeEvent public static void start(ServerStartedEvent e){
        String mode=System.getProperty("thaumcraft.alchemyProbe","");if(!mode.equals("write")&&!mode.equals("read"))return;var server=e.getServer();var level=server.overworld();level.getChunkAt(POS);server.tickRateManager().setFrozen(false);
        var p=new ServerPlayer(server,level,new GameProfile(ID,"AlchemyProbe"),ClientInformation.createDefault()){@Override public void sendSystemMessage(net.minecraft.network.chat.Component message,boolean overlay){} @Override protected net.minecraft.world.item.ItemCooldowns createItemCooldowns(){return new net.minecraft.world.item.ItemCooldowns();}};p.setPos(8.5,206,8.5);
        if(mode.equals("read")){
            var saved=server.getPlayerList().getPlayerIo().load(new NameAndId(ID,"AlchemyProbe")).orElseThrow();p.load(TagValueInput.create(ProblemReporter.DISCARDING,server.registryAccess(),saved));
            var crucible=(AlchemyMachine)level.getBlockEntity(POS);var smelter=(AlchemyMachine)level.getBlockEntity(POS.east(2));var jar=(AlchemyMachine)level.getBlockEntity(POS.south(2));
            check(crucible!=null&&crucible.snapshot().equals(new AlchemyState(1,Map.of("lux",17,"vitium",3),950,187,0,0,0,-250)),"Crucible state lost after restart");
            check(smelter.burn()==1234&&smelter.cook()==7&&smelter.inventory.getItem(0).getCount()==3,"Smelter fuel/progress/inventory lost");check(jar.total()==30&&jar.aspects().get("aer")==30,"Jar essence lost");check(ResearchService.knows(p,"UNLOCKALCHEMY")&&ResearchService.state(p).crafts().contains("thaumcraft:nitor_yellow"),"Player alchemy progress lost");
            server.tickRateManager().setFrozen(true);Thaumcraft.LOGGER.info("ALCHEMY_RESTART_OK: player craft proofs, crucible water/heat/aspects, furnace fuel/progress/inventory and jar essence");return;
        }
        for(var b:BlockPos.betweenClosed(POS.offset(-3,-1,-3),POS.offset(5,6,5)))level.setBlockAndUpdate(b,Blocks.AIR.defaultBlockState());
        KnowledgeStore.write(p,KnowledgeState.EMPTY.stage("FIRSTSTEPS",4).stage("KNOWLEDGETYPES",2).stage("UNLOCKALCHEMY",1).addPoints("OBSERVATION","ALCHEMY",16).addPoints("OBSERVATION","BASICS",16));check(ResearchService.advance(p,"UNLOCKALCHEMY"),"Could not start alchemy stage 2");
        level.setBlockAndUpdate(POS,Blocks.CAULDRON.defaultBlockState());p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ArcaneContent.SALIS.get(),2));var use=new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(POS),Direction.UP,POS,false));check(ArcaneContent.SALIS.get().useOn(use)==InteractionResult.SUCCESS&&p.getMainHandItem().getCount()==1,"Cauldron did not transform with one Salis");var c=(AlchemyMachine)level.getBlockEntity(POS);
        level.setBlockAndUpdate(POS.below(),Blocks.MAGMA_BLOCK.defaultBlockState());p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.WATER_BUCKET));check(c.interact(p,InteractionHand.MAIN_HAND)==InteractionResult.SUCCESS&&p.getMainHandItem().is(Items.BUCKET)&&c.water.getFluidAmount()==1000,"Bucket transfer failed");
        var coal=new ItemStack(Items.COAL);check(!c.process(coal,p)&&coal.getCount()==1,"Cold crucible consumed item");for(int i=0;i<151;i++)c.serverTick();check(c.heat()==151,"Crucible heating is not 151 ticks");check(c.process(coal,p)&&c.process(new ItemStack(Items.GLOWSTONE_DUST),p),"Ingredients did not dissolve");
        var locked=new ItemStack(Items.GLOWSTONE_DUST);var before=c.snapshot();check(!c.process(locked,p)&&locked.getCount()==1&&before.equals(c.snapshot()),"Locked catalyst caused partial costs");check(ResearchService.advance(p,"UNLOCKALCHEMY"),"Crucible craft proof did not advance book");check(c.process(locked,p)&&c.water.getFluidAmount()==950&&c.aspects().equals(Map.of("sensus",5)),"Nitor did not debit exact aspects/water");
        check(ResearchService.advance(p,"UNLOCKALCHEMY")&&ResearchService.knows(p,"UNLOCKALCHEMY"),"Nitor proof did not finish alchemy");check(ResearchService.advance(p,"BASEALCHEMY"),"Basic alchemy did not unlock");
        var drops=SPAWNED;check(drops.stream().anyMatch(d->d.getItem().is(thaumcraft.content.LuminousContent.BLOCKS.get("nitor_yellow").get().asItem())&&d.getPersistentData().getBooleanOr("thaumcraft:alchemy_output",false)),"Crafted output is not protected from re-ingestion");
        c.process(CrystalContent.essence("terra",1),p);c.process(CrystalContent.essence("terra",1),p);check(c.process(new ItemStack(SimpleContent.ITEMS.get("nugget_quartz").get()),p)&&c.water.getFluidAmount()==900,"Vis crystal recipe failed");
        check(AspectResolver.item(level,new ItemStack(Items.CRAFTING_TABLE)).size()>0,"Crafting aspect inference failed");check(AspectResolver.item(level,new ItemStack(Items.BRICK)).size()>0,"Cooking aspect inference failed");
        var cap=c.getCapability(ForgeCapabilities.FLUID_HANDLER,null).orElseThrow(IllegalStateException::new);int w=c.water.getFluidAmount();check(cap.fill(new FluidStack(net.minecraft.world.level.material.Fluids.LAVA,1000),IFluidHandler.FluidAction.EXECUTE)==0,"Crucible accepted lava as water");cap.drain(50,IFluidHandler.FluidAction.SIMULATE);check(c.water.getFluidAmount()==w,"Fluid simulation mutated tank");
        var smelter=machine(level,POS.east(2),AlchemyContent.SMELTER.get());var alembic=machine(level,POS.east(2).above(),AlchemyContent.ALEMBIC.get());
        var in=smelter.getCapability(ForgeCapabilities.ITEM_HANDLER,Direction.UP).orElseThrow(IllegalStateException::new);var fuel=smelter.getCapability(ForgeCapabilities.ITEM_HANDLER,Direction.NORTH).orElseThrow(IllegalStateException::new);check(in.insertItem(0,new ItemStack(Items.COAL,3),true).isEmpty()&&smelter.inventory.getItem(0).isEmpty(),"Item simulation changed inventory");in.insertItem(0,new ItemStack(Items.COAL,3),false);fuel.insertItem(1,new ItemStack(Items.COAL),false);check(!in.extractItem(0,1,false).isEmpty()==false,"Input automation extracted feedstock");
        for(int i=0;i<160;i++)smelter.serverTick();check(smelter.inventory.getItem(0).isEmpty()&&smelter.burn()>0&&smelter.total()+alembic.total()>0,"Smelting did not use native fuel and distill");
        var jar=machine(level,POS.south(2),AlchemyContent.JAR.get());check(jar.insertEssence("aer",20,false),"Jar fill failed");var j=jar.snapshot();check(!jar.insertEssence("ignis",10,false)&&j.equals(jar.snapshot()),"Jar mixed aspects");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(AlchemyContent.PHIAL.get()));check(jar.phial(p,InteractionHand.MAIN_HAND)==InteractionResult.SUCCESS&&AlchemyContent.amount(p.getMainHandItem())==10&&jar.total()==10,"Empty phial did not extract 10 units");check(jar.phial(p,InteractionHand.MAIN_HAND)==InteractionResult.SUCCESS&&AlchemyContent.amount(p.getMainHandItem())==0&&jar.total()==20,"Filled phial did not return 10 units");
        check(jar.insertEssence("aer",230,false)&&!jar.insertEssence("aer",1,false),"Jar capacity exceeded");var upgrade=new VoidJarRecipe();var ci=CraftingInput.of(1,1,List.of(jar.jarItem()));check(upgrade.matches(ci,level)&&AlchemyContent.amount(upgrade.assemble(ci))==250,"Void jar upgrade erased contents");var voidJar=machine(level,POS.south(3),AlchemyContent.VOID_JAR.get());voidJar.placeContents(upgrade.assemble(ci));check(voidJar.insertEssence("aer",10,false)&&voidJar.total()==250&&!voidJar.insertEssence("ignis",10,false),"Void jar overflow/mixing is wrong");
        var loot=Block.getDrops(jar.getBlockState(),level,jar.getBlockPos(),jar);check(loot.size()==1&&AlchemyContent.amount(loot.getFirst())==250,"Jar block loot lost contents");
        var phialRecipe=new PhialScribingRecipe();var dye=new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse("minecraft:black_dye")));check(!phialRecipe.matches(CraftingInput.of(3,1,List.of(AlchemyContent.filled(AlchemyContent.PHIAL.get(),"aer",10),new ItemStack(Items.FEATHER),dye)),level),"Scribing recipe consumed a filled phial");
        var bad=machine(level,POS.west(2),AlchemyContent.CRUCIBLE.get());var tag=bad.saveWithoutMetadata(level.registryAccess());tag.put("alchemy",StringTag.valueOf("future-format"));bad.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),tag));check(!bad.supported()&&bad.water.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER,1000),IFluidHandler.FluidAction.EXECUTE)==0&&bad.saveWithoutMetadata(level.registryAccess()).get("alchemy").equals(StringTag.valueOf("future-format")),"Unsupported data was overwritten");
        ResearchService.grant(p,"METALLURGY");for(int i=0;i<5;i++)check(c.process(CrystalContent.essence("instrumentum",1),p),"Could not dissolve tool aspect");check(c.process(new ItemStack(Items.IRON_INGOT),p)&&ResearchService.state(p).crafts().contains("thaumcraft:ingot_brass"),"Brass alchemy failed");
        for(int i=0;i<5;i++){c.process(CrystalContent.essence("praecantatio",1),p);c.process(CrystalContent.essence("terra",1),p);}check(c.process(new ItemStack(Items.IRON_INGOT),p)&&ResearchService.state(p).crafts().contains("thaumcraft:ingot_thaumium"),"Thaumium alchemy failed");
        var workPos=POS.east(4);level.setBlockAndUpdate(workPos,ArcaneContent.WORKBENCH.get().defaultBlockState());var work=(ArcaneWorkbenchEntity)level.getBlockEntity(workPos);work.inventory.setItem(0,jar.jarItem());work.inventory.setItem(14,CrystalContent.essence("perditio",1));ResearchService.grant(p,"WARDEDJARS");AuraData.get(level).put(AuraCell.key(0,0),new AuraCell(500,500,0));check(work.craft(p)&&work.inventory.getItem(15).is(AlchemyContent.VOID_JAR.get().asItem())&&AlchemyContent.amount(work.inventory.getItem(15))==250&&AuraManager.get(level,workPos).vis()==450,"Native arcane void jar upgrade lost essence or skipped costs");
        check(smelter.fuelTime(new ItemStack(AlchemyContent.ALUMENTUM.get()))==4800,"Alumentum native fuel time differs");p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(AlchemyContent.ALUMENTUM.get(),2));AlchemyContent.ALUMENTUM.get().use(level,p,InteractionHand.MAIN_HAND);check(THROWN.size()==1&&p.getMainHandItem().getCount()==1,"Throw did not create one projectile for one alumentum");var shot=THROWN.getFirst();var hit=new BlockPos(80,206,11);level.getChunkAt(hit);for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)level.setBlockAndUpdate(hit.offset(x,y,0),Blocks.STONE.defaultBlockState());shot.setPos(80.5,206.5,8.5);shot.setDeltaMovement(0,0,1);for(int i=0;i<10&&!shot.isRemoved();i++)shot.tick();check(shot.isRemoved(),"Alumentum projectile did not explode/discard on impact");
        tree(level);
        load(c,new AlchemyState(1,Map.of("lux",17,"vitium",3),950,187,0,0,0,-250),level);load(smelter,new AlchemyState(1,Map.of("aqua",12),0,0,1234,1600,7,-100),level);smelter.inventory.setItem(0,new ItemStack(Items.COAL,3));load(jar,new AlchemyState(1,Map.of("aer",30),0,0,0,0,0,-100),level);server.getPlayerList().getPlayerIo().save(p);server.tickRateManager().setFrozen(true);
        Thaumcraft.LOGGER.info("ALCHEMY_RUNTIME_OK: Salis progression, hot crucible, native recipes and exact costs, protected outputs, recipe inference, Forge capabilities/simulation, smelting/distillation, phials, jar loot/upgrade/overflow, corrupt-state preservation and special tree growth");
    }
    private static void tree(ServerLevel level){
        for(var wood:List.of("greatwood","silverwood")){var origin=new BlockPos(32+(wood.equals("silverwood")?24:0),205,8);for(int x=-5;x<=6;x++)for(int z=-5;z<=6;z++){level.getChunkAt(origin.offset(x,0,z));level.setBlockAndUpdate(origin.offset(x,-1,z),Blocks.GRASS_BLOCK.defaultBlockState());for(int y=0;y<=16;y++)level.setBlockAndUpdate(origin.offset(x,y,z),Blocks.AIR.defaultBlockState());}
            var sapling=TreeContent.BLOCKS.get("sapling_"+wood).get();level.setBlockAndUpdate(origin,sapling.defaultBlockState());((SaplingBlock)sapling).advanceTree(level,origin,level.getBlockState(origin),RandomSource.create(5));((SaplingBlock)sapling).advanceTree(level,origin,level.getBlockState(origin),RandomSource.create(5));check(level.getBlockState(origin).is(SimpleContent.BLOCKS.get("log_"+wood).get()),"Sapling failed to grow: "+wood);
            var blocked=origin.south(16);level.getChunkAt(blocked);for(int x=0;x<2;x++)for(int z=0;z<2;z++)level.setBlockAndUpdate(blocked.offset(x,-1,z),Blocks.GRASS_BLOCK.defaultBlockState());level.setBlockAndUpdate(blocked.above(),Blocks.DIAMOND_BLOCK.defaultBlockState());check(!new SpecialTreeFeature(wood.equals("silverwood")).place(level,level.getChunkSource().getGenerator(),RandomSource.create(5),blocked)&&level.getBlockState(blocked.above()).is(Blocks.DIAMOND_BLOCK),"Tree overwrote obstruction");
        }
        var pos=new BlockPos(76,206,8);level.getChunkAt(pos);var leaf=TreeContent.BLOCKS.get("leaves_silverwood").get().defaultBlockState().setValue(LeavesBlock.DISTANCE,1);level.setBlockAndUpdate(pos,leaf);AuraData.get(level).put(AuraCell.key(pos.getX()>>4,pos.getZ()>>4),new AuraCell(200,100,0));leaf.randomTick(level,pos,RandomSource.create(1));check(Math.abs(AuraManager.get(level,pos).vis()-100.01F)<.001F,"Natural silverwood leaf did not regenerate aura");
        var persistent=leaf.setValue(LeavesBlock.PERSISTENT,true);level.setBlockAndUpdate(pos,persistent);float before=AuraManager.get(level,pos).vis();persistent.randomTick(level,pos,RandomSource.create(1));check(AuraManager.get(level,pos).vis()==before,"Player-placed leaf generated vis");
        var dying=TreeContent.BLOCKS.get("leaves_greatwood").get().defaultBlockState();level.setBlockAndUpdate(pos,dying);dying.randomTick(level,pos,RandomSource.create(1));check(level.getBlockState(pos).isAir(),"Unsupported natural leaves did not decay");
    }
}
