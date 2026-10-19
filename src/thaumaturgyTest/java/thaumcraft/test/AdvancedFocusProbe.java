package thaumcraft.test;
import java.util.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.*;
import thaumcraft.thaumaturgy.*;
import thaumcraft.aura.*;
import thaumcraft.research.*;
import thaumcraft.crystals.*;
import static thaumcraft.test.ThaumaturgyProbe.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID)
public final class AdvancedFocusProbe {
    public static final BlockPos Q=new BlockPos(40,205,8);static ServerPlayer owner;static FocalEntity table;static int ticks;static boolean prepared,done;
    public static FocusProgram graph(){return new FocusProgram("cloud",List.of(new FocusProgram.Effect("heal",1,1)),"split_trajectory",2,10,2,30,false,List.of(new FocusProgram.Branch("mine",List.of(new FocusProgram.Effect("earth",1,1)),1,5,false)));}
    private static List<FocusFieldEntity> fields(ServerLevel l){return l.getEntitiesOfClass(FocusFieldEntity.class,new AABB(Q).inflate(10));}
    @SubscribeEvent public static void start(ServerStartedEvent e){String mode=System.getProperty("thaumcraft.thaumaturgyProbe","");if(!mode.equals("advanced-write")&&!mode.equals("advanced-read"))return;var l=e.getServer().overworld();l.setChunkForced(2,0,true);owner=new ServerPlayer(e.getServer(),l,new GameProfile(UUID.fromString("6862702f-2b67-4cd7-bdbf-4aba4800230a"),"AdvancedFocus"),ClientInformation.createDefault()){@Override public void sendSystemMessage(net.minecraft.network.chat.Component c,boolean overlay){}@Override protected ItemCooldowns createItemCooldowns(){return new ItemCooldowns();}};owner.setPos(40.5,206,8.5);knowledge(owner);if(mode.equals("advanced-read")){table=(FocalEntity)l.getBlockEntity(Q);check(table!=null&&table.working()&&table.remaining()<table.total(),"Advanced job did not resume");check(table.draft.equals(graph()),"Saved graph settings changed");check(!FocusCooldowns.get(l).acquire("entity:persisted-probe",l.getGameTime()),"Shared cooldown lost across restart");}else{l.setBlockAndUpdate(Q,Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(Q,ThaumaturgyContent.TABLE.get().defaultBlockState());table=(FocalEntity)l.getBlockEntity(Q);}}
    public static void editorAndCosts(ServerPlayer p,FocalEntity f){knowledge(p);p.experienceLevel=40;p.getAbilities().instabuild=false;f.draft=graph();var focus=item("focus_3");focus.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Branched focus"));f.inventory.setItem(0,focus);for(int i=1;i<10;i++)f.inventory.setItem(i,ItemStack.EMPTY);for(int i=0;i<36;i++)p.getInventory().setItem(i,ItemStack.EMPTY);
        // A missing crystal on the second path must leave XP and every other crystal unchanged.
        int slot=0;for(var e:graph().crystals().entrySet())if(!e.getKey().equals("terra"))p.getInventory().setItem(slot++,CrystalContent.essence(e.getKey(),e.getValue()));int levels=p.experienceLevel;check(!f.start(p)&&p.experienceLevel==levels,"Missing branch ingredient partially paid");p.getInventory().setItem(slot,CrystalContent.essence("terra",1));AuraManager.addVis(p.level(),Q,1000);check(f.start(p)&&p.experienceLevel==levels-graph().levels(),"Inventory crystal reservation failed");for(int i=0;i<36;i++)check(!p.getInventory().getItem(i).is(CrystalContent.ESSENCE.get()),"Crystals were not consumed exactly");check(!f.edit(p,7)&&!f.edit(p,3,1),"Running graph can be changed");
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,p.registryAccess());f.saveWithoutMetadata(out);var copy=new FocalEntity(Q,f.getBlockState());copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,p.registryAccess(),out.buildResult()));check(copy.working()&&copy.draft.equals(graph()),"Graph job codec failed");
        Thaumcraft.LOGGER.info("FOCUS_ADVANCED_COSTS_OK: two-path graph pays all crystals and XP atomically, inventory fallback, locked work and exact snapshot");
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){String mode=System.getProperty("thaumcraft.thaumaturgyProbe","");if(done||!mode.startsWith("advanced-")||mode.equals("advanced-client")||++ticks<40)return;var l=e.server().overworld();if(mode.equals("advanced-read")){if(ticks==40){var list=fields(l);check(list.stream().anyMatch(f->f.mine()&&f.program().friendly())&&list.stream().anyMatch(f->!f.mine()&&f.program().radius()==2),"Fields lost mine settings or cloud radius on restart");for(var field:list)check(field.expires()>l.getGameTime(),"Field lifetime reset/expired unexpectedly");}
            if(table.working())return;check(table.inventory.getItem(0).get(ThaumaturgyContent.PROGRAM.get()).equals(graph())&&table.inventory.getItem(0).getHoverName().getString().equals("Branched focus"),"Advanced output changed after restart");done=true;Thaumcraft.LOGGER.info("FOCUS_ADVANCED_RESTART_OK: clouds/mines/settings and shared cooldown persisted; branched job completed once");e.server().halt(false);return;}
        if(!prepared){for(var field:fields(l))field.discard();editorAndCosts(owner,table);var support=Q.east(3).below();l.setBlockAndUpdate(support,Blocks.STONE.defaultBlockState());var mine=new FocusFieldEntity(ThaumaturgyContent.FIELD.get(),l);mine.configure(owner,new FocusProgram("mine",List.of(new FocusProgram.Effect("heal",1,1)),"none",2,10,1,5,true,List.of()),Vec3.atCenterOf(support).add(0,.55,0),1,support);l.addFreshEntity(mine);var cloud=new FocusFieldEntity(ThaumaturgyContent.FIELD.get(),l);cloud.configure(owner,new FocusProgram("cloud",List.of(new FocusProgram.Effect("earth",1,1)),"none",2,10,2,30,false,List.of()),Vec3.atCenterOf(Q.east(4).above()),.75F,null);l.addFreshEntity(cloud);prepared=true;return;}
        if(table.remaining()==table.total())return;check(FocusCooldowns.get(l).acquire("entity:persisted-probe",l.getGameTime()),"Cannot prepare shared cooldown");done=true;Thaumcraft.LOGGER.info("FOCUS_ADVANCED_WRITE_OK: saved live cloud, friendly mine, shared cooldown and paid branched job");e.server().halt(false);
    }
}
