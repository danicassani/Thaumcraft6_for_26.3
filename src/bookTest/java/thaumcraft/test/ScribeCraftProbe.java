package thaumcraft.test;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.research.*;
import thaumcraft.theory.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class ScribeCraftProbe {
 static void check(boolean b,String reason){if(!b)throw new AssertionError(reason);}
 @SubscribeEvent public static void start(ServerStartedEvent e){String mode=System.getProperty("thaumcraft.bookProbe","");if(!mode.equals("scribe-baseline")&&!mode.equals("scribe-craft"))return;var server=e.getServer();var l=server.overworld();server.tickRateManager().setFrozen(true);var at=new BlockPos(8,180,8);l.getChunkAt(at);l.setBlockAndUpdate(at,Blocks.CRAFTING_TABLE.defaultBlockState());
  var p=new ServerPlayer(server,l,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"ScribeCraft"),ClientInformation.createDefault());new net.minecraft.server.network.ServerGamePacketListenerImpl(server,new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false)){@Override public void send(net.minecraft.network.protocol.Packet<?> packet){}@Override public void send(net.minecraft.network.protocol.Packet<?> packet,io.netty.channel.ChannelFutureListener listener){}};p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);p.setPos(8.5,181,8.5);
  net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent.BUS.addListener(event->thaumcraft.Thaumcraft.LOGGER.info("SCRIBE80_EVENT: count={} item={} grid={}",event.getCrafting().getCount(),event.getCrafting().getItem(),event.getContainer().getClass().getSimpleName()));
  for(int area:List.of(0,1,2))for(boolean shift:List.of(false,true))for(boolean phial:List.of(false,true)){
   boolean personal=area==1,arcane=area==2;
   p.getInventory().clearContent();KnowledgeStore.write(p,KnowledgeState.EMPTY.stage("KNOWLEDGETYPES",2).stage("THEORYRESEARCH",1));
   thaumcraft.crafting.ArcaneWorkbenchEntity workbench=null;
   if(arcane){l.setBlockAndUpdate(at.south(2),thaumcraft.crafting.ArcaneContent.WORKBENCH.get().defaultBlockState());workbench=(thaumcraft.crafting.ArcaneWorkbenchEntity)l.getBlockEntity(at.south(2));}
   AbstractContainerMenu m=arcane?new thaumcraft.crafting.ArcaneMenu(70,p.getInventory(),workbench):personal?p.inventoryMenu:new CraftingMenu(70,p.getInventory(),ContainerLevelAccess.create(l,at));p.containerMenu=m;m.setCarried(ItemStack.EMPTY);
   int result=arcane?thaumcraft.crafting.ArcaneMenu.RESULT:0,first=arcane?0:1;for(int i=first;i<first+(personal?4:9);i++)m.getSlot(i).setByPlayer(ItemStack.EMPTY);
   m.getSlot(first).setByPlayer(new ItemStack(phial?thaumcraft.alchemy.AlchemyContent.PHIAL.get():Items.GLASS_BOTTLE));m.getSlot(first+1).setByPlayer(new ItemStack(Items.FEATHER));m.getSlot(first+2).setByPlayer(new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse("minecraft:black_dye"))));
   m.broadcastChanges();check(m.getSlot(result).getItem().is(TheoryContent.SCRIBE.get()),"Native recipe did not produce tools");check(!ResearchService.state(p).crafts().contains("thaumcraft:scribing_tools"),"Preview granted craft proof");
   m.clicked(result,0,shift?ContainerInput.QUICK_MOVE:ContainerInput.PICKUP,p);boolean recorded=ResearchService.state(p).crafts().contains("thaumcraft:scribing_tools");
   thaumcraft.Thaumcraft.LOGGER.info("SCRIBE80_CASE: personal={} arcane={} shift={} phial={} proof={}",personal,arcane,shift,phial,recorded);
   check(m.getSlot(first).getItem().isEmpty()&&m.getSlot(first+1).getItem().isEmpty()&&m.getSlot(first+2).getItem().isEmpty(),"Craft did not consume exactly its ingredients");
   if(mode.equals("scribe-craft")){
    check(recorded,"Native crafting did not record tools");check(!ResearchService.canAdvance(p,ResearchCatalog.BY_KEY.get("THEORYRESEARCH")),"Tools bypassed required research table");
    var book=new ResearchMenu(71,p.getInventory());int index=ResearchCatalog.ENTRIES.indexOf(ResearchCatalog.BY_KEY.get("THEORYRESEARCH"));p.getInventory().setItem(7,new ItemStack(ResearchContent.BOOK.get()));check(book.clickMenuButton(p,1000+index)&&book.requirementDone(0)&&!book.requirementDone(1),"Thaumonomicon did not show completed tools requirement");
    ItemStack tool=ItemStack.EMPTY;if(!shift){tool=m.getCarried();m.setCarried(ItemStack.EMPTY);}else for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(TheoryContent.SCRIBE.get())){tool=p.getInventory().removeItemNoUpdate(i);break;}
    check(tool.is(TheoryContent.SCRIBE.get())&&tool.getCount()==1,"Crafted tool was lost or duplicated");p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,tool);l.setBlockAndUpdate(at.east(),TheoryContent.WOOD.get().defaultBlockState());
    var use=new net.minecraft.world.item.context.UseOnContext(p,net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(at.east()),net.minecraft.core.Direction.UP,at.east(),false));check(ResearchTableBlock.convert(use).consumesAction()&&p.getMainHandItem().isEmpty(),"Could not convert table with actual crafted tool");
    check(ResearchService.canAdvance(p,ResearchCatalog.BY_KEY.get("THEORYRESEARCH"))&&ResearchService.advance(p,"THEORYRESEARCH")&&ResearchService.knows(p,"THEORYRESEARCH"),"Full legitimate Theorycrafting progression did not unlock");
   }
  }
  if(mode.equals("scribe-craft")){
   p.getInventory().clearContent();KnowledgeStore.write(p,KnowledgeState.EMPTY);var m=new CraftingMenu(72,p.getInventory(),ContainerLevelAccess.create(l,at));p.containerMenu=m;for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.STONE,64));
   m.getSlot(1).setByPlayer(new ItemStack(Items.GLASS_BOTTLE));m.getSlot(2).setByPlayer(new ItemStack(Items.FEATHER));m.getSlot(3).setByPlayer(new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse("minecraft:black_dye"))));int beforeStat=p.getStats().getValue(net.minecraft.stats.Stats.ITEM_CRAFTED.get(TheoryContent.SCRIBE.get()));m.clicked(0,0,ContainerInput.QUICK_MOVE,p);
   check(ResearchService.state(p).crafts().isEmpty()&&m.getSlot(0).getItem().is(TheoryContent.SCRIBE.get())&&m.getSlot(1).getItem().is(Items.GLASS_BOTTLE),"Failed full-inventory pickup granted proof or consumed materials");
   check(p.getStats().getValue(net.minecraft.stats.Stats.ITEM_CRAFTED.get(TheoryContent.SCRIBE.get()))==beforeStat,"Failed pickup advanced paid-craft statistic");
   var recovered=ResearchEvents.craftingKnowledge(p,KnowledgeState.EMPTY);check(recovered.crafts().contains("thaumcraft:scribing_tools")&&recovered.points().isEmpty()&&recovered.stages().isEmpty(),"Missing legitimate craft proof could not be recovered");
   check(ResearchEvents.craftingKnowledge(p,recovered).equals(recovered),"Recovered proof repeats or changes progress");
   var fresh=new ServerPlayer(server,l,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"ScribeNotCrafted"),ClientInformation.createDefault());fresh.getInventory().setItem(0,new ItemStack(TheoryContent.SCRIBE.get()));check(ResearchEvents.craftingKnowledge(fresh,KnowledgeState.EMPTY).crafts().isEmpty(),"Mere possession granted craft proof");
   thaumcraft.Thaumcraft.LOGGER.info("SCRIBE80_RECOVERY_OK: paid vanilla statistic restores lost proof once; possession and failed pickup do not grant it");
   thaumcraft.Thaumcraft.LOGGER.info("SCRIBE80_CRAFT_OK: real 2x2/3x3/arcane, normal/shift, bottle/phial crafts; book checkbox; actual table conversion; required progression; no preview or failed pickup grants");
  }
  server.halt(false);
 }
}
