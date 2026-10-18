package thaumcraft.test;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.*;
import thaumcraft.research.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID)
public final class AlchemyClientScene{
    private static final Set<UUID> ready=new HashSet<>(),crafted=new HashSet<>(),bottled=new HashSet<>();
    private static final BlockPos C=new BlockPos(8,205,11),J=C.east(2);
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent.Post e){
        if(!System.getProperty("thaumcraft.alchemyProbe","").equals("client")||!(e.player() instanceof ServerPlayer p)||p.tickCount<60)return;
        var level=p.level();
        if(ready.add(p.getUUID())){
            for(var b:BlockPos.betweenClosed(C.offset(-4,-1,-4),C.offset(5,5,4)))level.setBlockAndUpdate(b,b.getY()==204?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(C,AlchemyContent.CRUCIBLE.get().defaultBlockState());level.setBlockAndUpdate(C.below(),Blocks.MAGMA_BLOCK.defaultBlockState());var c=(AlchemyMachine)level.getBlockEntity(C);var tag=c.saveWithoutMetadata(level.registryAccess());tag.put("alchemy",AlchemyState.CODEC.encodeStart(NbtOps.INSTANCE,new AlchemyState(1,Map.of("potentia",10,"ignis",10,"lux",10,"sensus",5),1000,200,0,0,0,-250)).getOrThrow());c.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),tag));level.sendBlockUpdated(C,c.getBlockState(),c.getBlockState(),3);
            level.setBlockAndUpdate(J,AlchemyContent.JAR.get().defaultBlockState());((AlchemyMachine)level.getBlockEntity(J)).insertEssence("aer",100,false);level.setBlockAndUpdate(C.west(2),AlchemyContent.SMELTER.get().defaultBlockState());var furnace=(AlchemyMachine)level.getBlockEntity(C.west(2));furnace.inventory.setItem(0,new ItemStack(Items.COAL));var ftag=furnace.saveWithoutMetadata(level.registryAccess());ftag.put("alchemy",AlchemyState.CODEC.encodeStart(NbtOps.INSTANCE,new AlchemyState(1,Map.of(),0,0,1234,1600,7,-100)).getOrThrow());furnace.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),ftag));if(furnace.cook()!=7)throw new AssertionError("Cook progress lost on load");furnace.inventory.setItem(0,new ItemStack(Items.IRON_INGOT));if(furnace.cook()!=0)throw new AssertionError("Cook progress carried across different input");level.setBlockAndUpdate(C.west(2).above(),AlchemyContent.ALEMBIC.get().defaultBlockState());((AlchemyMachine)level.getBlockEntity(C.west(2).above())).insertEssence("ignis",80,false);
            KnowledgeStore.write(p,KnowledgeState.EMPTY.stage("FIRSTSTEPS",4).stage("UNLOCKALCHEMY",3).stage("KNOWLEDGETYPES",2));p.getInventory().clearContent();p.getInventory().setItem(0,new ItemStack(Items.GLOWSTONE_DUST));p.getInventory().setItem(1,new ItemStack(AlchemyContent.PHIAL.get(),4));p.getInventory().setItem(2,new ItemStack(ResearchContent.METER.get()));p.getInventory().setItem(9,AlchemyContent.filled(AlchemyContent.PHIAL.get(),"aer",10));p.getInventory().setItem(10,AlchemyContent.filled(AlchemyContent.PHIAL.get(),"vitium",10));p.getInventory().setItem(11,AlchemyContent.filled(AlchemyContent.JAR.get().asItem(),"aqua",250));p.inventoryMenu.broadcastChanges();
            p.teleportTo(level,8.5,205,8.5,Set.of(),0,15,true);level.getServer().tickRateManager().setFrozen(true);
            Thaumcraft.LOGGER.info("ALCHEMY_CLIENT_SCENE_READY: frozen display fixture; native use glowstone on prepared hot crucible, then empty-hand inspect; slot 2 empty phials");
        }
        if(!crafted.contains(p.getUUID())&&ResearchService.state(p).crafts().contains("thaumcraft:nitor_yellow")){crafted.add(p.getUUID());Thaumcraft.LOGGER.info("ALCHEMY_CLIENT_CRAFT_OK: native block use crafted nitor and recorded proof; water={}",((AlchemyMachine)level.getBlockEntity(C)).water.getFluidAmount());}
        if(!bottled.contains(p.getUUID())&&level.getBlockEntity(J) instanceof AlchemyMachine jar&&jar.total()==90){bottled.add(p.getUUID());Thaumcraft.LOGGER.info("ALCHEMY_CLIENT_PHIAL_OK: native block use bottled exactly 10 aer from jar");}
    }
}
