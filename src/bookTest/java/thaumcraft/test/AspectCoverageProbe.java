package thaumcraft.test;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.alchemy.AspectResolver;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class AspectCoverageProbe {
 @SubscribeEvent public static void start(ServerStartedEvent e){
  String mode=System.getProperty("thaumcraft.bookProbe","");if(!mode.equals("aspect-audit")&&!mode.equals("aspect-coverage"))return;
  var l=e.getServer().overworld();e.getServer().tickRateManager().setFrozen(true);var missing=new JsonArray();int total=0,covered=0;
  for(var item:BuiltInRegistries.ITEM){var id=BuiltInRegistries.ITEM.getKey(item);if(!Set.of("minecraft","thaumcraft").contains(id.getNamespace())||item==Items.AIR)continue;total++;var values=AspectResolver.item(l,new ItemStack(item));if(values.isEmpty())missing.add(id.toString());else covered++;}
  var out=new JsonObject();out.addProperty("items",total);out.addProperty("covered",covered);out.add("missing",missing);
  try{Files.writeString(Path.of("../../work/aspect79-"+mode+".json"),new GsonBuilder().setPrettyPrinting().create().toJson(out));}catch(Exception ex){throw new RuntimeException(ex);}
  if(mode.equals("aspect-coverage")){
   var p=new net.minecraft.world.entity.player.Player(l,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"AspectCoverage")){public net.minecraft.world.level.GameType gameMode(){return net.minecraft.world.level.GameType.SURVIVAL;}};
   thaumcraft.research.KnowledgeStore.write(p,thaumcraft.research.KnowledgeState.EMPTY);
   for(var item:BuiltInRegistries.ITEM){var id=BuiltInRegistries.ITEM.getKey(item);if(!Set.of("minecraft","thaumcraft").contains(id.getNamespace())||item==Items.AIR)continue;var stack=new ItemStack(item);var target=new thaumcraft.research.ScanTarget(2,1,net.minecraft.core.BlockPos.ZERO,stack,null,new net.minecraft.world.phys.AABB(0,0,0,1,1,1),false);
    var preview=thaumcraft.research.ScanInspection.describe(p,target);ScanProbe.check(preview!=null&&preview.scannable()&&!preview.known()&&preview.aspects().isEmpty(),"Unstudyable or leaked preview: "+id);
    var before=thaumcraft.research.ResearchService.state(p);ScanProbe.check(thaumcraft.research.ScanningService.observe(p,stack),"Item could not be studied: "+id);
    var known=thaumcraft.research.ScanInspection.describe(p,target);ScanProbe.check(known.known()&&!known.scannable(),"Item did not become studied: "+id);
    var after=thaumcraft.research.ResearchService.state(p);ScanProbe.check(!thaumcraft.research.ScanningService.observe(p,stack)&&after.equals(thaumcraft.research.ResearchService.state(p)),"Duplicate knowledge: "+id);
    if(AspectResolver.item(l,stack).isEmpty())ScanProbe.check(before.points().equals(after.points()),"Unknown composition invented points: "+id);
   }
   ScanProbe.check(!thaumcraft.research.ScanningService.observe(p,ItemStack.EMPTY),"Empty hand became a discovery");
   ScanProbe.check(AspectResolver.item(l,new ItemStack(thaumcraft.content.BasicContent.AMBER.get())).equals(Map.of("vinculum",10,"vitreus",10)),"Amber lost original aspects");
   for(var id:List.of("minecraft:amethyst_shard","minecraft:raw_copper","minecraft:netherite_sword","minecraft:sculk","minecraft:bamboo_planks","minecraft:oxidized_copper","thaumcraft:mirror","thaumcraft:traveller_boots"))ScanProbe.check(!AspectResolver.item(l,new ItemStack(BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(id)))).isEmpty(),"Representative lacks aspects: "+id);
   var supplement=thaumcraft.research.ResearchCatalog.load("aspect-supplement.json").getAsJsonObject();for(var entry:supplement.getAsJsonObject("aliases").entrySet())for(var id:List.of(entry.getKey(),entry.getValue().getAsString()))ScanProbe.check(BuiltInRegistries.ITEM.containsKey(net.minecraft.resources.Identifier.parse(id)),"Unknown material equivalent: "+id);
   for(var aspect:thaumcraft.crystals.CrystalAspects.COLORS.keySet()){
    var crystal=thaumcraft.crystals.CrystalContent.essence(aspect,1);ScanProbe.check(AspectResolver.item(l,crystal).equals(Map.of(aspect,1)),"Crystal variant lost its aspect");
    var phial=thaumcraft.alchemy.AlchemyContent.filled(thaumcraft.alchemy.AlchemyContent.PHIAL.get(),aspect,10);ScanProbe.check(AspectResolver.item(l,phial).equals(Map.of(aspect,10)),"Phial variant lost its aspect");
   }
   thaumcraft.Thaumcraft.LOGGER.info("ASPECT79_COVERAGE_OK: every registered nonempty item previewed, studied, repeated without duplicate rewards; amber and modern materials verified");
  }
  thaumcraft.Thaumcraft.LOGGER.info("ASPECT79_AUDIT: covered {} / {}; missing {}",covered,total,missing.size());e.getServer().halt(false);
 }
}
