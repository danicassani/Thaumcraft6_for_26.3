package thaumcraft.test;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.infusion.*;
import thaumcraft.Thaumcraft;
import static thaumcraft.test.InfusionProbe.check;
import static thaumcraft.infusion.InfusionEnchantments.Kind.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class AdvancedInfusionClient {
    static int stage,wait;static final BlockPos P=new BlockPos(40,204,24);static ServerPlayer player;static ItemStack tool;
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent.Post e){
        if(!System.getProperty("thaumcraft.infusionProbe","").equals("advanced-client")||!(e.player() instanceof ServerPlayer p)||p.tickCount<60||stage==9)return;
        var l=p.level();if(stage==0){l.getServer().tickRateManager().setFrozen(false);player=p;p.setGameMode(GameType.SURVIVAL);for(var k:List.of("INFUSION","CRYSTALFARMER","UNLOCKINFUSION","THEORYRESEARCH","UNLOCKGOLEMANCY"))thaumcraft.research.ResearchService.grant(p,k);AdvancedInfusionChecks.run(l,p);p.teleportTo(l,P.getX()+.5,P.getY()+1,P.getZ()+.5,Set.of(),0,40,true);p.getInventory().clearContent();for(var q:BlockPos.betweenClosed(P.offset(-4,-1,-4),P.offset(4,4,4))){l.getChunkAt(q);l.setBlockAndUpdate(q,q.getY()==P.getY()+4||q.getY()==P.getY()-1||Math.abs(q.getX()-P.getX())==4||Math.abs(q.getZ()-P.getZ())==4?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());}for(var q:BlockPos.betweenClosed(P.offset(-1,0,-1),P.offset(1,0,1)))l.setBlockAndUpdate(q,Blocks.IRON_ORE.defaultBlockState());tool=new ItemStack(Items.DIAMOND_PICKAXE);for(var k:List.of(DESTRUCTIVE,COLLECTOR,LAMPLIGHT,REFINING))tool=InfusionEnchantments.apply(tool,k);for(int i=1;i<4;i++)tool=InfusionEnchantments.apply(tool,REFINING);p.setItemInHand(InteractionHand.MAIN_HAND,tool);p.setXRot(90);stage=10;wait=0;return;}
        if(++wait<(stage==10?40:3))return;wait=0;
        if(stage==10){check(l.getMaxLocalRawBrightness(P)<10,"Test room is not dark: "+l.getMaxLocalRawBrightness(P));net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.BUS.post(new net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock(p,P,Direction.UP,net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action.START));check(p.gameMode.destroyBlock(P),"Native primary mine rejected");stage=1;return;}
        if(stage==1){for(var q:BlockPos.betweenClosed(P.offset(-1,0,-1),P.offset(1,0,1)))if(!q.equals(P.east()))check(!l.getBlockState(q).is(Blocks.IRON_ORE),"Destructive native mining did not remove plane");check(l.getBlockState(P.east()).is(Blocks.IRON_ORE),"Area mining bypassed protection event");check(l.getBlockState(P).is(InfusionContent.GLIMMER.get())&&l.getBlockState(P).getLightEmission(l,P)==15,"Lamplight did not produce light");check(tool.getDamageValue()==8,"Area mining wrong durability "+tool.getDamageValue());var drops=l.getEntitiesOfClass(ItemEntity.class,p.getBoundingBox().inflate(3));check(drops.stream().anyMatch(d->d.getItem().is(thaumcraft.content.SimpleContent.ITEMS.get("cluster_iron").get())),"Refining never produced cluster");check(drops.stream().allMatch(d->d.distanceToSqr(p)<9),"Collector did not redirect drops");
            // Sneaking suppresses area mining and collection.
            for(var q:BlockPos.betweenClosed(P.offset(-1,0,-1),P.offset(1,0,1)))l.setBlockAndUpdate(q,Blocks.STONE.defaultBlockState());p.setShiftKeyDown(true);check(p.gameMode.destroyBlock(P),"Sneak mine failed");p.setShiftKeyDown(false);stage=2;return;}
        if(stage==2){check(l.getBlockState(P.east()).is(Blocks.STONE),"Sneak did not disable destructive");var origin=P.north(2);for(int i=0;i<4;i++)l.setBlockAndUpdate(origin.above(i),Blocks.OAK_LOG.defaultBlockState());p.setItemInHand(InteractionHand.MAIN_HAND,InfusionEnchantments.apply(new ItemStack(Items.DIAMOND_AXE),BURROWING));check(p.gameMode.destroyBlock(origin),"Native burrowing break failed");stage=3;return;}
        if(stage==3){check(l.getBlockState(P.north(2).above(3)).isAir(),"Burrowing did not follow connected log");combat(p);stage=4;return;}
        if(stage==4){
            p.setItemInHand(InteractionHand.MAIN_HAND,InfusionEnchantments.apply(new ItemStack(Items.DIAMOND_PICKAXE),SOUNDING));p.setShiftKeyDown(true);var ore=P.east(3);l.setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.BUS.post(new net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock(p,ore,Direction.UP,net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action.START));check(p.getCooldowns().isOnCooldown(p.getMainHandItem()),"Sounding did not activate native scan");p.setShiftKeyDown(false);
            // Show the charged network and synchronized enchanted tool in the graphical client.
            var s=AdvancedInfusionChecks.S;for(var q:BlockPos.betweenClosed(s.offset(-2,-1,-6),s.offset(6,-1,2)))l.setBlockAndUpdate(q,Blocks.STONE.defaultBlockState());p.teleportTo(l,s.getX()+1.5,s.getY()+1,s.getZ()-4.5,Set.of(),0,16,true);p.getInventory().setItem(1,InfusionEnchantments.apply(new ItemStack(Items.DIAMOND_PICKAXE),COLLECTOR));p.inventoryMenu.broadcastChanges();stage=9;Thaumcraft.LOGGER.info("INFUSION_ADVANCED_CLIENT_EFFECTS_OK: native 3x3 mining/durability, refinement, collector, glimmer, sneak suppression, connected logs, arcing/essence combat and sounding scan");
        }
    }
    @SubscribeEvent public static void protection(net.minecraftforge.event.level.BlockEvent.BreakEvent e){if(System.getProperty("thaumcraft.infusionProbe","").equals("advanced-client")&&stage==1&&e.getPos().equals(P.east()))e.setResult(net.minecraftforge.common.util.Result.DENY);}
    static void combat(ServerPlayer p){var l=p.level();var a=new net.minecraft.world.entity.monster.zombie.Zombie(l);var b=new net.minecraft.world.entity.monster.zombie.Zombie(l);a.setPos(p.getX()+2,p.getY(),p.getZ());b.setPos(p.getX()+3,p.getY(),p.getZ());a.setNoAi(true);b.setNoAi(true);l.addFreshEntity(a);l.addFreshEntity(b);p.setItemInHand(InteractionHand.MAIN_HAND,InfusionEnchantments.apply(new ItemStack(Items.DIAMOND_SWORD),ARCING));a.hurtServer(l,l.damageSources().playerAttack(p),4);check(b.getHealth()<b.getMaxHealth(),"Arcing did not damage secondary mob");a.discard();b.discard();var sword=new ItemStack(Items.DIAMOND_SWORD);for(int i=0;i<5;i++)sword=InfusionEnchantments.apply(sword,ESSENCE);sword=InfusionEnchantments.apply(sword,COLLECTOR);p.setItemInHand(InteractionHand.MAIN_HAND,sword);var zombie=new net.minecraft.world.entity.monster.zombie.Zombie(l);zombie.setPos(p.getX()+2,p.getY(),p.getZ());zombie.getRandom().setSeed(1);var drops=new ArrayList<ItemEntity>();net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.post(new net.minecraftforge.event.entity.living.LivingDropsEvent(zombie,l.damageSources().playerAttack(p),drops,true));check(drops.stream().anyMatch(d->d.getItem().is(thaumcraft.crystals.CrystalContent.ESSENCE.get()))&&drops.stream().allMatch(d->d.distanceToSqr(p)<2),"Essence or combat collector failed");}
}
