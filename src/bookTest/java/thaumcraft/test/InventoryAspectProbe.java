package thaumcraft.test;
import java.util.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.research.*;
import thaumcraft.crystals.CrystalContent;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class InventoryAspectProbe {
 @SubscribeEvent public static void start(ServerStartedEvent e){if(!System.getProperty("thaumcraft.bookProbe","").equals("inventory-aspects"))return;var l=e.getServer().overworld();e.getServer().tickRateManager().setFrozen(true);
 Player p=new Player(l,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"TooltipAspects")){public GameType gameMode(){return GameType.SURVIVAL;}};
 var coal=new ItemStack(Items.COAL,64);p.getInventory().setItem(0,coal);KnowledgeStore.write(p,KnowledgeState.EMPTY);int id=p.containerMenu.containerId;
 var q=new InventoryInspection.Request(1,id,36);var hidden=InventoryInspection.inspect(p,q);ScanProbe.check(!hidden.known()&&hidden.aspects().isEmpty(),"Unknown item leaked aspects");ScanProbe.check(ScanningService.observe(p,coal),"Coal study failed");var saved=ResearchService.state(p);var known=InventoryInspection.inspect(p,q);ScanProbe.check(known.known()&&known.aspects().equals(thaumcraft.alchemy.AspectResolver.item(l,coal.copyWithCount(1))),"Tooltip does not show per-item aspects");ScanProbe.check(ItemStack.isSameItemSameComponents(InventoryInspection.item(known,l.registryAccess()),coal)&&coal.getCount()==64&&ResearchService.state(p).equals(saved),"Inspection mutated item or knowledge or lost components");
 for(int index:new int[]{-1,9999})ScanProbe.check(InventoryInspection.inspect(p,new InventoryInspection.Request(1,id,index)).aspects().isEmpty(),"Invalid slot leaked");ScanProbe.check(InventoryInspection.inspect(p,new InventoryInspection.Request(1,id+1,36)).aspects().isEmpty(),"Stale menu leaked");
 var air=CrystalContent.essence("aer",9);var fire=CrystalContent.essence("ignis",12);p.getInventory().setItem(0,air);ScanProbe.check(ScanningService.observe(p,air),"Air crystal study failed");ScanProbe.check(InventoryInspection.inspect(p,q).aspects().equals(Map.of("aer",1)),"Crystal count multiplied");p.getInventory().setItem(0,fire);var unseen=InventoryInspection.inspect(p,q);ScanProbe.check(!unseen.known()&&unseen.aspects().isEmpty(),"Study leaked across crystal variants");ScanProbe.check(ScanningService.observe(p,fire),"Fire crystal study failed");var seen=InventoryInspection.inspect(p,q);ScanProbe.check(seen.aspects().equals(Map.of("ignis",1))&&ItemStack.isSameItemSameComponents(fire,InventoryInspection.item(seen,l.registryAccess())),"Variant response lost identity");
 KnowledgeStore.write(p,KnowledgeState.EMPTY);ScanProbe.check(!InventoryInspection.inspect(p,q).known(),"Reset knowledge retained study");thaumcraft.Thaumcraft.LOGGER.info("ASPECTS82_SERVER_OK: unknown privacy, paid study, exact components and variants, single item quantities, stale menu, invalid slots, no mutations");e.getServer().halt(false);
 }
}
