package thaumcraft.test;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.crystals.*;
import thaumcraft.infusion.*;
import thaumcraft.research.*;
import thaumcraft.theory.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID)
public final class InfusionClientScene{
    public static final BlockPos M=new BlockPos(8,207,11),T=M.south(6).below(2);
    static final Set<UUID> ready=new HashSet<>(),started=new HashSet<>(),finished=new HashSet<>(),played=new HashSet<>();static int expected;
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent.Post e){if(!System.getProperty("thaumcraft.infusionProbe","").equals("client")||!(e.player() instanceof ServerPlayer p)||p.tickCount<60)return;var l=p.level();
        if(ready.add(p.getUUID())){
            l.getServer().tickRateManager().setFrozen(false);l.dimensionType().defaultClock().ifPresent(c->l.getServer().clockManager().setTotalTicks(c,6000));for(var k:List.of("INFUSION","CRYSTALFARMER","UNLOCKINFUSION","THEORYRESEARCH","UNLOCKGOLEMANCY"))ResearchService.grant(p,k);InfusionProbe.altar(l,M,p);p.getInventory().clearContent();p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.teleportTo(l,8.5,205,7.5,Set.of(),0,-13.4F,true);Thaumcraft.LOGGER.info("INFUSION_CLIENT_SCENE_READY: empty-hand native right click on assembled matrix starts crystal infusion");
        }
        var m=(InfusionMatrix)l.getBlockEntity(M);
        if(m.job()!=null&&started.add(p.getUUID()))Thaumcraft.LOGGER.info("INFUSION_CLIENT_START_OK: real matrix block interaction started native infusion");
        if(started.contains(p.getUUID())&&m.job()==null&&((InfusionPedestal)l.getBlockEntity(M.below(2))).item().is(CrystalContent.BLOCKS.get("aer").get().asItem())&&finished.add(p.getUUID())){
            InfusionProbe.check(ResearchService.state(p).crafts().contains("thaumcraft:crystal_aer"),"Online owner did not receive infusion craft proof");l.getServer().tickRateManager().setFrozen(true);
            l.setBlockAndUpdate(T.below(),Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(T,TheoryContent.TABLE.get().defaultBlockState());var table=(ResearchTableEntity)l.getBlockEntity(T);table.inventory.setItem(0,new ItemStack(TheoryContent.SCRIBE.get()));table.inventory.setItem(1,new ItemStack(Items.PAPER,32));p.teleportTo(l,8.5,205,14.5,Set.of(),0,15,true);InfusionProbe.check(table.start(p,List.of()),"Cannot prepare native theory menu");var channel=TheoryCards.generate(TheoryCards.Kind.CHANNEL,p,table.session(),31,false);var infuse=TheoryCards.generate(TheoryCards.Kind.INFUSE,p,table.session(),37,false);var sculpt=TheoryCards.generate(TheoryCards.Kind.SCULPTING,p,table.session(),41,false);table.session().choices=List.of(channel,infuse,sculpt);table.setChanged();expected=infuse.amount();p.getInventory().setItem(9,infuse.needs().get(0).stack());p.getInventory().setItem(10,infuse.needs().get(1).stack());p.getInventory().setItem(11,channel.needs().getFirst().stack());p.getInventory().setItem(12,new ItemStack(Items.CLAY_BALL));p.inventoryMenu.broadcastChanges();p.openMenu(table);Thaumcraft.LOGGER.info("INFUSION_CLIENT_CRAFT_OK: native tickers crafted crystal and recorded owner proof; opening real theory menu with Channel/Infuse/Sculpting");
        }
        if(finished.contains(p.getUUID())&&l.getBlockEntity(T) instanceof ResearchTableEntity table&&table.session().played==1&&played.add(p.getUUID())){InfusionProbe.check(table.session().total("INFUSION")==expected&&p.getInventory().getItem(9).isEmpty()&&p.getInventory().getItem(10).isEmpty()&&table.inventory.getItem(0).getDamageValue()==1,"Native Infuse card costs/result differ");Thaumcraft.LOGGER.info("INFUSION_CLIENT_CARD_OK: real menu button consumed material, filled phial and ink, granted exact progress and cleared choices");}
    }
}
