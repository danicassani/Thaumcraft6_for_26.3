package thaumcraft.test;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.*;
import thaumcraft.thaumaturgy.*;
import static thaumcraft.test.ThaumaturgyProbe.*;
import static thaumcraft.test.PlanProbe.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID)
public final class PlanClient {
    static int stage,ticks;static FocalEntity table;
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(!System.getProperty("thaumcraft.thaumaturgyProbe","").equals("plan-client")||e.server().isDedicatedServer()||e.server().getPlayerList().getPlayers().isEmpty()||++ticks<80)return;var p=e.server().getPlayerList().getPlayers().getFirst();var l=p.level();
        if(stage==0){l.getGameRules().set(net.minecraft.world.level.gamerules.GameRules.SPAWN_MOBS,false,e.server());l.getGameRules().set(net.minecraft.world.level.gamerules.GameRules.SPAWN_PHANTOMS,false,e.server());for(var mob:l.getEntitiesOfClass(LivingEntity.class,new AABB(Q).inflate(64)))if(mob!=p)mob.discard();p.setHealth(p.getMaxHealth());for(var slot:List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET))p.setItemSlot(slot,net.minecraft.world.item.ItemStack.EMPTY);for(var q:BlockPos.betweenClosed(Q.offset(-4,-1,-4),Q.offset(10,5,12)))l.setBlockAndUpdate(q,q.getY()==204?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(Q,ThaumaturgyContent.TABLE.get().defaultBlockState());table=(FocalEntity)l.getBlockEntity(Q);effects(p);prepareJob(p,table);p.teleportTo(l,20.5,206,8.5,Set.of(),0,0,true);p.setDeltaMovement(Vec3.ZERO);stage=1;return;}
        if(stage==1&&!table.working()){check(program().equals(table.inventory.getItem(0).get(ThaumaturgyContent.PROGRAM.get())),"Plan job output settings changed");p.setGameMode(GameType.CREATIVE);p.openMenu(table);stage=2;Thaumcraft.LOGGER.info("PLAN_CLIENT_EFFECTS_OK: native effects, isolated settings editor and completed focus");}
        if(stage==2&&p.containerMenu instanceof net.minecraft.world.inventory.InventoryMenu){for(var q:BlockPos.betweenClosed(W.offset(-1,-1,0),W.offset(1,1,0)))l.setBlockAndUpdate(q,Blocks.STONE.defaultBlockState());var preview=new FocusProgram("plan",program().effects(),"none",2,10,1,5,false,List.of(),new FocusProgram.PlanSettings(1,1,0,false));equip(p,preview);p.teleportTo(l,24.5,205,8.5,Set.of(),0,0,true);p.setDeltaMovement(Vec3.ZERO);stage=3;Thaumcraft.LOGGER.info("PLAN_PREVIEW_READY: nine block outlines at sixteen-block maximum range");}
    }
}
