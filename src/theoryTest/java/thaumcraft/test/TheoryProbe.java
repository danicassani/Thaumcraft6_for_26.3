package thaumcraft.test;

import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.research.*;
import thaumcraft.theory.*;

@Mod.EventBusSubscriber(modid=Thaumcraft.MODID)
public final class TheoryProbe {
    private static final UUID ID=UUID.fromString("fb504654-807d-410b-a845-06d325b85fc3");
    private static final BlockPos POS=new BlockPos(8,205,8);
    static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    private static ServerPlayer player(net.minecraft.server.MinecraftServer server,ServerLevel level,UUID id){var p=new ServerPlayer(server,level,new GameProfile(id,"TheoryProbe"),ClientInformation.createDefault()){
            @Override public void sendSystemMessage(net.minecraft.network.chat.Component message,boolean overlay){}
        };p.setPos(8.5,206,8.5);return p;}
    @SubscribeEvent public static void start(ServerStartedEvent e){
        String mode=System.getProperty("thaumcraft.theoryProbe","");if(!mode.equals("write")&&!mode.equals("read"))return;
        var server=e.getServer();var level=server.overworld();server.tickRateManager().setFrozen(true);level.getChunkAt(POS);var p=player(server,level,ID);
        if(mode.equals("read")){
            var saved=server.getPlayerList().getPlayerIo().load(new NameAndId(ID,"TheoryProbe")).orElseThrow();p.load(TagValueInput.create(ProblemReporter.DISCARDING,server.registryAccess(),saved));
            var table=(ResearchTableEntity)level.getBlockEntity(POS);require(table!=null&&table.session()!=null&&table.session().owner.equals(ID.toString())&&table.session().choices.size()==2,"Table session lost after restart");
            require(table.inventory.getItem(0).getDamageValue()==7&&table.inventory.getItem(1).getCount()==13,"Table inventory lost");
            require(ResearchService.state(p).raw("THEORY","BASICS")==32,"Theory rewards lost after restart");require(table.session().choices.getFirst().seed()==987654321012345L,"Draw seed rerolled on restart");
            Thaumcraft.LOGGER.info("THEORY_RESTART_OK: actual block entity and player file, owner, ink, paper, pending choices/seed and raw theory points");server.halt(false);return;
        }
        for(var b:BlockPos.betweenClosed(POS.offset(-4,0,-4),POS.offset(4,5,4)))level.setBlockAndUpdate(b,Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(POS,TheoryContent.WOOD.get().defaultBlockState());p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(TheoryContent.SCRIBE.get()));
        var context=new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(POS),Direction.UP,POS,false));
        require(ResearchTableBlock.convert(context)==InteractionResult.FAIL,"Unresearched player converted table");
        KnowledgeStore.write(p,KnowledgeState.EMPTY.stage("KNOWLEDGETYPES",2).stage("THEORYRESEARCH",1).craft("thaumcraft:scribing_tools"));
        require(ResearchTableBlock.convert(context)==InteractionResult.SUCCESS&&p.getMainHandItem().isEmpty(),"Table conversion failed to transfer tool");
        var table=(ResearchTableEntity)level.getBlockEntity(POS);require(table.inventory.getItem(0).is(TheoryContent.SCRIBE.get()),"Table has no tool");
        require(ResearchService.advance(p,"THEORYRESEARCH")&&ResearchService.knows(p,"THEORYRESEARCH"),"Research table craft proof did not advance book");
        require(!table.start(p,List.of()),"Started with no paper");table.inventory.setItem(1,new ItemStack(Items.PAPER,20));
        require(!table.start(p,List.of("minecraft:beacon")),"Spoofed nonexistent aid accepted");level.setBlockAndUpdate(POS.east(),Blocks.BEACON.defaultBlockState());
        require(table.start(p,List.of("minecraft:beacon"))&&table.session().inspiration==4,"Aid inspiration cost incorrect");
        require(table.draw(p,false)&&table.session().choices.size()==2&&table.inventory.getItem(1).getCount()==19,"Draw did not cost exactly one paper");
        var choices=List.copyOf(table.session().choices);require(!table.draw(p,true)&&choices.equals(table.session().choices),"Redraw/replay rerolled choices");
        var other=player(server,level,UUID.randomUUID());KnowledgeStore.write(other,ResearchService.state(p));require(!table.play(other,0)&&!table.cancel(other)&&!table.finish(other),"Other player stole session");
        require(!table.play(p,99)&&!table.finish(p),"Invalid card or early finish accepted");require(table.cancel(p),"Owner could not abandon");
        // A fixed legal offer lets native menu packets test replay protection without random test flakiness.
        require(table.start(p,List.of()),"Start failed");var s=table.session();s.choices=List.of(new TheoryCards.Card(TheoryCards.Kind.CALIBRATE,3,0,0,0,1,false));
        var menu=new TheoryMenu(7,p.getInventory(),table);int packet=menu.packet(4),ink=table.inventory.getItem(0).getDamageValue();require(menu.clickMenuButton(p,packet),"Native card action failed");
        require(!menu.clickMenuButton(p,packet)&&table.session().played==1&&table.inventory.getItem(0).getDamageValue()==ink+1,"Replayed packet charged or awarded twice");
        s=table.session();s.choices=List.of(new TheoryCards.Card(TheoryCards.Kind.ANALYZE,3,0,0,0,2,false));var before=ResearchService.state(p);ink=table.inventory.getItem(0).getDamageValue();
        require(!table.play(p,0)&&before.equals(ResearchService.state(p))&&table.inventory.getItem(0).getDamageValue()==ink,"Failed observation cost was partial");
        s.choices=List.of(new TheoryCards.Card(TheoryCards.Kind.CELESTIAL,0,0,1,0,5,false));p.getInventory().clearContent();p.getInventory().setItem(1,TheoryContent.note(0));
        require(!table.play(p,0)&&p.getInventory().getItem(1).getCount()==1,"Missing second note consumed the first");p.getInventory().setItem(2,TheoryContent.note(1));require(table.play(p,0)&&p.getInventory().getItem(1).isEmpty()&&p.getInventory().getItem(2).isEmpty(),"Celestial card did not consume exact variants");
        table.session().choices=List.of(new TheoryCards.Card(TheoryCards.Kind.SCRIPTING,5,0,0,0,1,false));table.inventory.setItem(1,ItemStack.EMPTY);ink=table.inventory.getItem(0).getDamageValue();require(!table.play(p,0)&&table.inventory.getItem(0).getDamageValue()==ink,"Scripting consumed ink without paper");
        table.session().choices=List.of();table.session().totals.clear();table.session().add("BASICS",100);table.session().inspiration=0;require(table.finish(p)&&ResearchService.state(p).raw("THEORY","BASICS")==32&&!table.finish(p),"Theory finish duplicated or changed progression");
        var tool=new ItemStack(TheoryContent.SCRIBE.get());tool.setDamageValue(100);var refill=new ScribingRefillRecipe();var dye=new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse("minecraft:black_dye")));var input=CraftingInput.of(2,1,List.of(tool,dye));require(refill.matches(input,level)&&refill.assemble(input).getDamageValue()==0,"Empty tool cannot be refilled");
        sky(level,p);
        for(var entry:ResearchCatalog.ENTRIES)ResearchService.grant(p,entry.key());
        p.giveExperienceLevels(10);
        var observations=ResearchService.state(p);for(var category:ResearchCatalog.CATEGORIES)observations=observations.addPoints("OBSERVATION",category,64);KnowledgeStore.write(p,observations);
        for(var variant:thaumcraft.infusion.CuriosityContent.VARIANTS)p.getInventory().add(thaumcraft.infusion.CuriosityContent.curio(variant));
        for(var kind:TheoryCards.Kind.values()){
            var sample=TheorySession.start(p.getUUID(),5,List.of());sample.add("BASICS",100);sample.add("ARTIFICE",80);sample.add("ALCHEMY",60);
            var card=TheoryCards.generate(kind,p,sample,42,true);require(card!=null,"Card could not initialize: "+kind);
            var first=sample.copy();var second=sample.copy();require(TheoryCards.apply(card,first,p,ResearchService.state(p))!=null,"Card failed: "+kind);TheoryCards.apply(card,second,p,ResearchService.state(p));
            require(TheorySession.CODEC.encodeStart(NbtOps.INSTANCE,first).getOrThrow().equals(TheorySession.CODEC.encodeStart(NbtOps.INSTANCE,second).getOrThrow()),"Card rerolled its effect: "+kind);
        }
        p.getInventory().clearContent();var layout=new TheoryMenu(99,p.getInventory(),table);
        require(layout.slots.size()==38&&layout.getSlot(0).x==16&&layout.getSlot(0).y==15&&layout.getSlot(1).x==224&&layout.getSlot(2).x==77&&layout.getSlot(29).x==20&&layout.getSlot(37).y==226,"Original research inventory layout incorrect");
        var savedTool=table.inventory.getItem(0).copy();require(!layout.quickMoveStack(p,0).isEmpty()&&table.inventory.getItem(0).isEmpty(),"Could not remove scribing tools");require(!layout.quickMoveStack(p,37).isEmpty()&&ItemStack.isSameItemSameComponents(savedTool,table.inventory.getItem(0))&&p.getInventory().getItem(8).isEmpty(),"Could not restore tools without duplication");
        table.inventory.setItem(0,new ItemStack(TheoryContent.SCRIBE.get()));table.inventory.setItem(1,new ItemStack(Items.PAPER,13));require(table.start(p,List.of()),"Could not save session");table.inventory.getItem(0).setDamageValue(7);table.session().choices=List.of(new TheoryCards.Card(TheoryCards.Kind.EXPERIMENTATION,0,0,0,0,987654321012345L,false),new TheoryCards.Card(TheoryCards.Kind.CALIBRATE,3,0,0,0,4,false));table.setChanged();server.getPlayerList().getPlayerIo().save(p);
        // Unsupported data is round-tripped unchanged and cannot start a new session.
        var badPos=POS.west(2);level.setBlockAndUpdate(badPos,TheoryContent.TABLE.get().defaultBlockState());var bad=(ResearchTableEntity)level.getBlockEntity(badPos);var tag=bad.saveWithoutMetadata(server.registryAccess());tag.put("theory",StringTag.valueOf("future data"));bad.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,server.registryAccess(),tag));require(!bad.supported()&&!bad.start(p,List.of())&&bad.saveWithoutMetadata(server.registryAccess()).get("theory").equals(StringTag.valueOf("future data")),"Malformed theory overwritten");
        Thaumcraft.LOGGER.info("THEORY_RUNTIME_OK: table bootstrap, aid validation, paper and ink costs, session ownership, native replay rejection, atomic cards, celestial variants, exact finish, refill, server sky checks and corrupt-data preservation; original menu layout and physical tools shift transfer");server.halt(false);
    }
    private static void sky(ServerLevel level,ServerPlayer p){
        ResearchService.grant(p,"CELESTIALSCANNING");p.getInventory().clearContent();p.getInventory().setItem(0,new ItemStack(ResearchContent.METER.get()));p.getInventory().setItem(1,new ItemStack(TheoryContent.SCRIBE.get()));p.getInventory().setItem(2,new ItemStack(Items.PAPER,10));p.setYRot(0);p.setXRot(-90);
        // Set the native overworld clock to noon; the sky service reads environment angles, not a client claim.
        level.getServer().clockManager().setTotalTicks(level.dimensionType().defaultClock().orElseThrow(),6000);
        require(CelestialObservations.scan(p),"Noon sun scan failed");require(p.getInventory().getItem(2).getCount()==9&&p.getInventory().getItem(1).getDamageValue()==1,"Sky supplies not paid");
        require(!CelestialObservations.scan(p)&&p.getInventory().getItem(2).getCount()==9,"Daily scan duplicated notes");
        level.setBlockAndUpdate(p.blockPosition().above(3),Blocks.STONE.defaultBlockState());require(!CelestialObservations.scan(p),"Scanned sky through roof");level.removeBlock(p.blockPosition().above(3),false);
        level.getServer().clockManager().setTotalTicks(level.dimensionType().defaultClock().orElseThrow(),18000);p.setXRot(-45);p.setYRot(180);require(CelestialObservations.scan(p),"North star observation failed");
        p.setXRot(-90);require(CelestialObservations.scan(p),"Moon observation failed");
    }
}
