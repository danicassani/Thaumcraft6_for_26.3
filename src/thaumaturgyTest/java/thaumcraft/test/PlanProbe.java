package thaumcraft.test;
import java.util.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.*;
import thaumcraft.thaumaturgy.*;
import thaumcraft.aura.*;
import thaumcraft.crystals.*;
import thaumcraft.research.*;
import static thaumcraft.test.ThaumaturgyProbe.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID)
public final class PlanProbe {
    public static final BlockPos Q=new BlockPos(20,205,8),W=new BlockPos(24,206,12);
    static ServerPlayer owner;static FocalEntity table;static int ticks;static boolean prepared,done;static BlockPos protectedBreak,protectedPlace;
    public static FocusProgram program(){return new FocusProgram("plan",List.of(new FocusProgram.Effect("break",1,1)),"none",2,10,1,5,false,List.of(),new FocusProgram.PlanSettings(1,2,3,true));}
    public static void equip(ServerPlayer p,FocusProgram spell){var caster=item("caster_basic");var focus=item("focus_3");focus.set(ThaumaturgyContent.PROGRAM.get(),spell);caster.set(ThaumaturgyContent.FOCUS.get(),focus);p.setItemInHand(InteractionHand.MAIN_HAND,caster);p.setYRot(0);p.setXRot(0);}
    public static void effects(ServerPlayer p){var l=p.level();knowledge(p);p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setPos(24.5,205,8.5);p.setDeltaMovement(Vec3.ZERO);var full=new FocusProgram("plan",List.of(new FocusProgram.Effect("break",1,1)),"none",2,10,1,5,false,List.of(),new FocusProgram.PlanSettings(1,1,1,false));
        for(var q:BlockPos.betweenClosed(W.offset(-2,-2,-1),W.offset(2,2,4)))l.setBlockAndUpdate(q,Blocks.AIR.defaultBlockState());for(var q:BlockPos.betweenClosed(W.offset(-1,-1,0),W.offset(1,1,2)))l.setBlockAndUpdate(q,Blocks.STONE.defaultBlockState());
        var bedrock=W.offset(-1,-1,1);var chest=W.offset(1,1,1);protectedBreak=W.offset(1,-1,1);l.setBlockAndUpdate(bedrock,Blocks.BEDROCK.defaultBlockState());l.setBlockAndUpdate(chest,Blocks.CHEST.defaultBlockState());equip(p,full);AuraManager.addVis(l,p.blockPosition(),1000);
        var state=ResearchService.state(p);KnowledgeStore.write(p,state.stage("FOCUSPLAN",0));float before=AuraManager.get(l,p.blockPosition()).vis();check(p.getMainHandItem().getItem().use(l,p,InteractionHand.MAIN_HAND)==InteractionResult.FAIL&&AuraManager.get(l,p.blockPosition()).vis()==before,"Unresearched Plan paid or executed");KnowledgeStore.write(p,state);
        check(p.getMainHandItem().getItem().use(l,p,InteractionHand.MAIN_HAND)==InteractionResult.SUCCESS,"Plan glove launch failed");check(Math.abs(before-AuraManager.get(l,p.blockPosition()).vis()-full.vis())<.002,"Plan paid more than once");int remaining=0;for(var q:BlockPos.betweenClosed(W.offset(-1,-1,0),W.offset(1,1,2)))if(!l.getBlockState(q).isAir())remaining++;check(remaining==3&&l.getBlockState(bedrock).is(Blocks.BEDROCK)&&l.getBlockState(chest).is(Blocks.CHEST)&&l.getBlockState(protectedBreak).is(Blocks.STONE),"Volume selection/protection lost: remaining="+remaining);protectedBreak=null;
        // Exact-state surface flood: a mismatched middle block separates the far patch.
        for(var q:BlockPos.betweenClosed(W.offset(-3,-2,0),W.offset(3,2,2)))l.setBlockAndUpdate(q,Blocks.AIR.defaultBlockState());for(int x=0;x<4;x++)l.setBlockAndUpdate(W.east(x),x==2?Blocks.DIRT.defaultBlockState():Blocks.STONE.defaultBlockState());var hit=new BlockHitResult(Vec3.atCenterOf(W),Direction.NORTH,W,false);var surface=new FocusProgram.PlanSettings(0,0,3,true);check(PlanSelection.select(l,hit,surface).equals(List.of(W,W.east())),"Surface crossed unlike/disconnected blocks");
        for(var direction:Direction.values())l.setBlockAndUpdate(W.relative(direction),Blocks.STONE.defaultBlockState());check(!PlanSelection.select(l,hit,surface).contains(W),"Surface included fully enclosed block");l.setBlockAndUpdate(W.north(),Blocks.GLASS.defaultBlockState());check(PlanSelection.select(l,hit,surface).contains(W),"Non-opaque neighbor did not expose surface");l.setBlockAndUpdate(W.north(),Blocks.AIR.defaultBlockState());
        // Placement rejection must not consume the replacement; successful placements consume one each.
        var exchange=new FocusProgram("plan",List.of(new FocusProgram.Effect("exchange",1,1)),"none",2,10,1,5,false,List.of(),new FocusProgram.PlanSettings(1,0,0,false));protectedPlace=W.west();for(int x=-1;x<=1;x++)l.setBlockAndUpdate(W.east(x),Blocks.STONE.defaultBlockState());p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.DIRT,5));equip(p,exchange);SpellEngine.cast(p,exchange);check(p.getOffhandItem().getCount()==3&&l.getBlockState(W).is(Blocks.DIRT)&&l.getBlockState(W.east()).is(Blocks.DIRT)&&l.getBlockState(protectedPlace).isAir(),"Plan exchange ignored native placement/consumption");protectedPlace=null;
        Thaumcraft.LOGGER.info("PLAN_EFFECTS_OK: native glove, single aura debit, research denial, 27-block volume, protected blocks, exact connected surface and paid/cancelled placement");
    }
    public static void prepareJob(ServerPlayer p,FocalEntity f){p.setPos(Q.getX()+.5,Q.getY()+1,Q.getZ()+.5);knowledge(p);p.experienceLevel=30;f.inventory.setItem(0,item("focus_1"));var menu=new FocalMenu(72,p.getInventory(),f);f.draft=new FocusProgram("plan",program().effects());check(menu.clickMenuButton(p,14)&&f.draft.plan().x()==0,"Plan X control failed");check(menu.clickMenuButton(p,17)&&f.draft.plan().surface(),"Plan mode control failed");check(menu.clickMenuButton(p,7)&&f.draft.modifier().equals("split_target")&&menu.clickMenuButton(p,7)&&f.draft.modifier().equals("none"),"Plan modifier cycle entered an incompatible graph");f.draft=program();int slot=1;for(var c:program().crystals().entrySet())f.inventory.setItem(slot++,CrystalContent.essence(c.getKey(),c.getValue()));AuraManager.addVis(p.level(),Q,1000);check(f.start(p),"Cannot start paid Plan job");check(!menu.clickMenuButton(p,14),"Running Plan settings changed");}
    @SubscribeEvent public static void start(ServerStartedEvent e){var mode=System.getProperty("thaumcraft.thaumaturgyProbe","");if(!mode.equals("plan-write")&&!mode.equals("plan-read"))return;var l=e.getServer().overworld();for(int z=0;z<=1;z++)l.setChunkForced(1,z,true);owner=new ServerPlayer(e.getServer(),l,new GameProfile(UUID.fromString("7893f6bc-00e4-44ae-bad4-c44f5c738c39"),"PlanProbe"),ClientInformation.createDefault()){@Override public void sendSystemMessage(net.minecraft.network.chat.Component c,boolean overlay){}@Override protected ItemCooldowns createItemCooldowns(){return new ItemCooldowns();}};owner.setPos(20.5,206,8.5);if(mode.equals("plan-read")){table=(FocalEntity)l.getBlockEntity(Q);check(table!=null&&table.working()&&table.remaining()<table.total()&&table.draft.equals(program()),"Plan pending settings/job not recovered");}else{l.setBlockAndUpdate(Q,Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(Q,ThaumaturgyContent.TABLE.get().defaultBlockState());table=(FocalEntity)l.getBlockEntity(Q);}}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){var mode=System.getProperty("thaumcraft.thaumaturgyProbe","");if(done||(!mode.equals("plan-write")&&!mode.equals("plan-read"))||++ticks<40)return;if(mode.equals("plan-read")){if(table.working())return;check(program().equals(table.inventory.getItem(0).get(ThaumaturgyContent.PROGRAM.get())),"Completed Plan settings changed");done=true;Thaumcraft.LOGGER.info("PLAN_RESTART_OK: paid job resumed with exact dimensions/method and completed once");e.server().halt(false);return;}if(!prepared){prepareJob(owner,table);prepared=true;return;}if(table.remaining()==table.total())return;done=true;Thaumcraft.LOGGER.info("PLAN_WRITE_OK: saved paid Plan job and editor settings");e.server().halt(false);}
    @SubscribeEvent public static void protectBreak(net.minecraftforge.event.level.BlockEvent.BreakEvent e){if(System.getProperty("thaumcraft.thaumaturgyProbe","").startsWith("plan-")&&e.getPos().equals(protectedBreak))e.setResult(net.minecraftforge.common.util.Result.DENY);}
    @SubscribeEvent public static boolean protectPlace(net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent e){return System.getProperty("thaumcraft.thaumaturgyProbe","").startsWith("plan-")&&e.getPos().equals(protectedPlace);}
}
