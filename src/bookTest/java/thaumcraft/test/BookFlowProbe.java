package thaumcraft.test;
import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.crafting.*;
import thaumcraft.aura.*;
import thaumcraft.content.SimpleContent;
import thaumcraft.crystals.CrystalContent;
import thaumcraft.research.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class BookFlowProbe {
 public static final BlockPos P=new BlockPos(56008,180,8);static final UUID ID=UUID.fromString("fed3cfd4-d018-463b-a648-f0f578d63256");
 public static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 static String mode(){return System.getProperty("thaumcraft.bookProbe","");}
 static UseOnContext context(Player p,BlockPos at){return new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(at),Direction.UP,at,false));}
 static float vis(ServerLevel l){return AuraManager.get(l,P).vis();}
 static void resetAura(ServerLevel l){AuraData.get(l).put(AuraCell.key(P.getX()>>4,P.getZ()>>4),new AuraCell(200,200,0));}
 public static void meter(ArcaneWorkbenchEntity table,int count,boolean pane){table.inventory.clearContent();for(int i:new int[]{1,3,5,7})table.inventory.setItem(i,new ItemStack(Items.GOLD_INGOT,count));table.inventory.setItem(4,new ItemStack(pane?Items.GLASS_PANE:Items.GLASS,count));for(int i=0;i<6;i++)table.inventory.setItem(9+i,CrystalContent.essence(ArcaneRecipe.PRIMALS.get(i),count));}
 static int count(Player p,Item item){int n=0;for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(item))n+=p.getInventory().getItem(i).getCount();return n;}
 @SubscribeEvent public static void start(ServerStartedEvent event){if(!mode().equals("write")&&!mode().equals("read"))return;var server=event.getServer();var l=server.overworld();server.tickRateManager().setFrozen(true);for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)l.getChunk((P.getX()>>4)+x,z);
  var saved=new ServerPlayer(server,l,new GameProfile(ID,"BookFlow"),ClientInformation.createDefault());var io=server.getPlayerList().getPlayerIo();
  if(mode().equals("read")){saved.load(TagValueInput.create(ProblemReporter.DISCARDING,server.registryAccess(),io.load(new NameAndId(ID,"BookFlow")).orElseThrow()));check(ResearchService.knows(saved,"FIRSTSTEPS")&&ResearchService.knows(saved,"UNLOCKALCHEMY@2")&&!ResearchService.knows(saved,"UNLOCKALCHEMY"),"Knowledge changed on restart");var table=(ArcaneWorkbenchEntity)l.getBlockEntity(P);check(table!=null&&table.inventory.getItem(15).getCount()==4&&table.inventory.getItem(15).is(Items.IRON_NUGGET),"Paid partial output lost on restart");Thaumcraft.LOGGER.info("BOOK56_RESTART_OK: real player progression and four already-paid output items recovered");server.halt(false);return;}
  Player p=new Player(l,new GameProfile(UUID.randomUUID(),"BookActions")){public GameType gameMode(){return GameType.SURVIVAL;}};p.setPos(P.getX()+.5,P.getY()+1,P.getZ()+.5);ResearchService.flag(p,"!gotdream");p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ArcaneContent.SALIS.get(),8));
  l.setBlockAndUpdate(P,Blocks.BOOKSHELF.defaultBlockState());ArcaneContent.SALIS.get().useOn(context(p,P));check(ResearchService.hasBook(p),"Book not created");ResearchService.openBook(p);check(ResearchService.knows(p,"FIRSTSTEPS@1"),"Book opening did not start first steps");var book=new ResearchMenu(1,p.getInventory());for(int c=1;c<7;c++)check(!book.categoryUnlocked(c),"Category revealed before discovery "+c);for(int i=0;i<ResearchCatalog.ENTRIES.size();i++){var entry=ResearchCatalog.ENTRIES.get(i);if(book.visible(i)&&ResearchService.state(p).stages().getOrDefault(entry.key(),0)==0&&!ResearchService.state(p).flags().contains(entry.key()))check(ResearchService.parents(ResearchService.state(p),entry),"Future topic revealed before prerequisites: "+entry.key());}
  l.setBlockAndUpdate(P,Blocks.CRAFTING_TABLE.defaultBlockState());ArcaneContent.SALIS.get().useOn(context(p,P));check(ResearchService.advance(p,"FIRSTSTEPS"),"Workbench craft not recorded");var table=(ArcaneWorkbenchEntity)l.getBlockEntity(P);resetAura(l);
  meter(table,1,false);check(table.match()==null,"Glass block matched glass pane recipe");meter(table,1,true);table.inventory.setItem(1,new ItemStack(Items.IRON_INGOT));check(table.match()==null,"Iron matched gold recipe");meter(table,1,true);check(table.match()!=null&&table.match().vis()==20,"Exact meter recipe not recognized");
  var menu=new ArcaneMenu(2,p.getInventory(),table);p.containerMenu=menu;float before=vis(l);menu.clicked(ArcaneMenu.RESULT,0,ContainerInput.PICKUP,p);check(menu.getCarried().is(ResearchContent.METER.get())&&menu.getCarried().getCount()==1&&vis(l)==before-20,"Native output pickup failed or wrong debit");for(int i=9;i<15;i++)check(table.inventory.getItem(i).isEmpty(),"Crystal charged incorrectly");menu.clicked(ArcaneMenu.RESULT,0,ContainerInput.PICKUP,p);check(menu.getCarried().getCount()==1&&vis(l)==before-20,"Repeated pickup duplicated output or charge");p.getInventory().add(menu.getCarried());menu.setCarried(ItemStack.EMPTY);
  meter(table,2,true);menu.broadcastChanges();before=vis(l);int initial=count(p,ResearchContent.METER.get());menu.clicked(ArcaneMenu.RESULT,0,ContainerInput.QUICK_MOVE,p);check(count(p,ResearchContent.METER.get())==initial+2&&vis(l)==before-40,"Native shift crafting did not pay exactly twice");check(table.inventory.getItem(15).isEmpty(),"Shift crafting left duplicated paid output");
  KnowledgeStore.write(p,ResearchService.state(p).addPoints("OBSERVATION","BASICS",16));check(ResearchService.advance(p,"FIRSTSTEPS")&&ResearchService.knows(p,"FIRSTSTEPS"),"First Steps did not complete");
  var cauldron=P.east(2);l.setBlockAndUpdate(cauldron,Blocks.CAULDRON.defaultBlockState());check(ArcaneContent.SALIS.get().useOn(context(p,cauldron))==InteractionResult.FAIL&&l.getBlockState(cauldron).is(Blocks.CAULDRON),"Crucible bypassed alchemy research");var startMenu=new ResearchMenu(6,p.getInventory());int alchemy=ResearchCatalog.ENTRIES.indexOf(ResearchCatalog.BY_KEY.get("UNLOCKALCHEMY"));check(startMenu.clickMenuButton(p,1000+alchemy)&&ResearchService.knows(p,"UNLOCKALCHEMY@1"),"Opening available Basic Alchemy did not start its first page");KnowledgeStore.write(p,ResearchService.state(p).addPoints("OBSERVATION","BASICS",16).addPoints("OBSERVATION","ALCHEMY",16));check(ResearchService.advance(p,"UNLOCKALCHEMY"),"Basic Alchemy stage two blocked");check(ArcaneContent.SALIS.get().useOn(context(p,cauldron))!=InteractionResult.FAIL&&l.getBlockState(cauldron).is(thaumcraft.alchemy.AlchemyContent.CRUCIBLE.get()),"Opened book and eligible alchemy still could not create crucible");
  var checkpoint=ResearchService.state(p);check(!ResearchCatalog.categoryUnlocked(checkpoint,"ALCHEMY"),"Alchemy tab revealed before completing unlock");ResearchService.grant(p,"UNLOCKALCHEMY");book=new ResearchMenu(3,p.getInventory());check(book.categoryUnlocked(0)&&book.categoryUnlocked(2)&&!book.categoryUnlocked(6),"Category discovery gates wrong");KnowledgeStore.write(p,checkpoint);
  var inventoryCopy=new java.util.ArrayList<ItemStack>();for(int i=0;i<36;i++){inventoryCopy.add(p.getInventory().getItem(i).copy());p.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));}table.inventory.clearContent();table.inventory.setItem(0,new ItemStack(Items.IRON_INGOT));menu=new ArcaneMenu(4,p.getInventory(),table);p.containerMenu=menu;menu.clicked(ArcaneMenu.RESULT,0,ContainerInput.QUICK_MOVE,p);check(table.inventory.getItem(0).getCount()==1&&table.inventory.getItem(15).isEmpty(),"Full inventory consumed recipe inputs");for(int i=0;i<36;i++)p.getInventory().setItem(i,inventoryCopy.get(i));
  table.inventory.clearContent();table.inventory.setItem(0,new ItemStack(Items.IRON_INGOT));menu=new ArcaneMenu(4,p.getInventory(),table);p.containerMenu=menu;menu.clicked(ArcaneMenu.RESULT,1,ContainerInput.PICKUP,p);check(menu.getCarried().getCount()==5&&table.inventory.getItem(15).getCount()==4&&table.inventory.getItem(0).isEmpty(),"Partial paid output lost or duplicated");
  KnowledgeStore.write(saved,checkpoint);io.save(saved);Thaumcraft.LOGGER.info("BOOK56_NATIVE_OK: real dream/book/table flow, original category unlocks, wrong glass/iron rejected, normal and shift pickup, exact vis/crystals, partial result, First Steps completion and gated crucible conversion");server.halt(false);
 }
}
