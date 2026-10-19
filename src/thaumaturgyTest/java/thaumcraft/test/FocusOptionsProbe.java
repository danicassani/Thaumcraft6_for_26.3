package thaumcraft.test;

import java.util.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.*;
import thaumcraft.thaumaturgy.*;
import thaumcraft.aura.*;
import thaumcraft.crystals.*;
import static thaumcraft.test.ThaumaturgyProbe.*;

@Mod.EventBusSubscriber(modid=Thaumcraft.MODID)
public final class FocusOptionsProbe {
    public static final BlockPos Q=new BlockPos(96,205,8);
    public static final UUID OWNER=UUID.fromString("9409bdc0-99b5-4995-bea4-b5c755a70101");
    static ServerPlayer owner;static FocalEntity table;static int ticks;static boolean prepared,done;
    public static FocusProgram graph(){return new FocusProgram("projectile",List.of(new FocusProgram.Effect("break",1,1,8,2,true)),"split_trajectory",2,10,1,5,false,List.of(new FocusProgram.Branch("projectile",List.of(new FocusProgram.Effect("heal",1,1)),1,5,false,new FocusProgram.ProjectileSettings(1,3))),FocusProgram.PlanSettings.DEFAULT,new FocusProgram.ProjectileSettings(5,1));}
    public static void costs(ServerPlayer p,FocalEntity f){
        knowledge(p);f.draft=graph();f.inventory.setItem(0,item("focus_3"));
        var menu=new FocalMenu(79,p.getInventory(),f);
        f.draft=new FocusProgram("touch",List.of(new FocusProgram.Effect("fire",1,1)));check(menu.clickMenuButton(p,1)&&menu.clickMenuButton(p,1)&&f.draft.projectile().speed()==1,"New projectile must start at original speed one");f.draft=graph();
        check(menu.clickMenuButton(p,18)&&f.draft.projectile().speed()==1,"Projectile speed did not wrap");
        check(menu.clickMenuButton(p,19)&&f.draft.projectile().option()==2,"Projectile option edit failed");
        check(menu.clickMenuButton(p,20)&&f.draft.effects().getFirst().fortune()==3,"Fortune edit failed");
        check(menu.clickMenuButton(p,21)&&!f.draft.effects().getFirst().silk(),"Silk edit failed");
        check(menu.clickMenuButton(p,3)&&f.draft.effects().getFirst().fortune()==3,"Power edit erased harvest settings");
        check(menu.clickMenuButton(p,8)&&menu.clickMenuButton(p,18)&&f.draft.branches().getFirst().projectile().speed()==2&&f.draft.projectile().speed()==1,"Per-path projectile settings leaked");
        check(!menu.clickMenuButton(p,20),"Healing acquired fortune");f.draft=graph();int slot=1;for(var c:graph().crystals().entrySet())f.inventory.setItem(slot++,CrystalContent.essence(c.getKey(),c.getValue()));p.experienceLevel=40;AuraManager.addVis(p.level(),f.getBlockPos(),1000);
        var full=thaumcraft.research.ResearchService.state(p);thaumcraft.research.KnowledgeStore.write(p,full.stage("FOCUSPROJECTILE",2));check(!f.start(p)&&p.experienceLevel==40&&!f.working(),"Special projectile options bypassed completed research");check(new FocusProgram("projectile",List.of(new FocusProgram.Effect("fire",1,1))).knows(p),"Normal projectile stage-two research was blocked");thaumcraft.research.KnowledgeStore.write(p,full);
        check(f.start(p)&&p.experienceLevel==40-graph().levels(),"Advanced settings focus payment failed");check(!f.edit(p,18),"Pending projectile settings editable");
    }
    @SubscribeEvent public static void start(ServerStartedEvent e){var mode=System.getProperty("thaumcraft.thaumaturgyProbe","");if(!mode.equals("options-write")&&!mode.equals("options-read"))return;var l=e.getServer().overworld();l.setChunkForced(6,0,true);owner=new ServerPlayer(e.getServer(),l,new GameProfile(OWNER,"OptionsProbe"),ClientInformation.createDefault()){@Override public void sendSystemMessage(net.minecraft.network.chat.Component c,boolean overlay){}@Override protected ItemCooldowns createItemCooldowns(){return new ItemCooldowns();}};owner.setPos(Vec3.atCenterOf(Q.east(2)));if(mode.equals("options-write")){l.setBlockAndUpdate(Q,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(Q,ThaumaturgyContent.TABLE.get().defaultBlockState());}table=(FocalEntity)l.getBlockEntity(Q);check(table!=null,"Options table missing");}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){var mode=System.getProperty("thaumcraft.thaumaturgyProbe","");if(done||(!mode.equals("options-write")&&!mode.equals("options-read"))||++ticks<40)return;var l=e.server().overworld();
        if(mode.equals("options-read")){if(ticks==40){check(table.working()&&table.draft.equals(graph())&&table.remaining()<table.total(),"Paid advanced draft failed to resume");var caster=table.inventory.getItem(9);check(caster.get(ThaumaturgyContent.PLAN_SELECTION.get()).equals(new PlanControls.Selection(1,2,3,2)),"Caster Plan selection not persisted");var shots=l.getEntitiesOfClass(FocusProjectile.class,new AABB(Q).inflate(12));check(shots.stream().anyMatch(s->graph().path(0).equals(s.program())&&s.expires()>l.getGameTime()),"Projectile settings/deadline missing after restart");for(var s:shots)s.discard();}if(table.working())return;check(graph().equals(table.inventory.getItem(0).get(ThaumaturgyContent.PROGRAM.get())),"Advanced output differs after restart");done=true;Thaumcraft.LOGGER.info("FOCUS_OPTIONS_RESTART_OK: exact paid graph, harvest/projectile branches, caster dimensions and in-flight projectile deadline persisted");e.server().halt(false);return;}
        if(!prepared){costs(owner,table);var caster=item("caster_basic");caster.set(ThaumaturgyContent.PLAN_SELECTION.get(),new PlanControls.Selection(1,2,3,2));table.inventory.setItem(9,caster);for(var s:l.getEntitiesOfClass(FocusProjectile.class,new AABB(Q).inflate(12)))s.discard();var shot=new FocusProjectile(ThaumaturgyContent.PROJECTILE.get(),l);shot.setOwner(owner);shot.setPos(Vec3.atCenterOf(Q.above(3)));var focus=item("focus_3");focus.set(ThaumaturgyContent.PROGRAM.get(),graph().path(0));shot.setItem(focus);l.addFreshEntity(shot);prepared=true;return;}
        if(table.remaining()==table.total())return;done=true;Thaumcraft.LOGGER.info("FOCUS_OPTIONS_WRITE_OK: reserved graph costs and persisted projectile and caster selection");e.server().halt(false);
    }
}
