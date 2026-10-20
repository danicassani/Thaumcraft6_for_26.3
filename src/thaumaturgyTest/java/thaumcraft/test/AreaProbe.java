package thaumcraft.test;
import java.util.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.*;
import thaumcraft.golems.*;
import static thaumcraft.test.ThaumaturgyProbe.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID)
public final class AreaProbe {
 static final BlockPos Q=new BlockPos(1200,205,8);static final UUID OWNER=UUID.fromString("9a4a4e8b-2893-457f-80b4-78d25a76285c");static ServerPlayer p;static int ticks;static boolean done;
 static SealEntity seal(Direction face){var support=Q.east(face.ordinal()*4);var at=support.relative(face);var l=p.level();l.setBlockAndUpdate(support,Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(at,GolemContent.SEALS.get(SealKind.PICKUP_ADVANCED).get().defaultBlockState().setValue(SealBlock.FACING,face));var s=(SealEntity)l.getBlockEntity(at);s.claimOwner(p);p.setPos(Vec3.atCenterOf(at));return s;}
 static void set(SealEntity s,SealArea target){p.setPos(Vec3.atCenterOf(s.getBlockPos()));if(!s.oriented())check(s.edit(p,17),"Cannot enable oriented area");for(int axis=0;axis<3;axis++){int expected=axis==0?target.x():axis==1?target.y():target.z();for(int n=0;n<8;n++){var a=s.areaConfig();int actual=axis==0?a.x():axis==1?a.y():a.z();if(actual==expected)break;check(s.edit(p,18+axis*2+(actual<expected?1:0)),"Area dimension edit rejected");}}}
 @SubscribeEvent public static void start(ServerStartedEvent e){var mode=System.getProperty("thaumcraft.thaumaturgyProbe","");if(!mode.equals("area-write")&&!mode.equals("area-read"))return;var l=e.getServer().overworld();for(int x=74;x<=77;x++)for(int z=-1;z<=1;z++)l.setChunkForced(x,z,true);l.setChunkForced(56,0,true);l.setChunkForced(65,0,true);p=new ServerPlayer(e.getServer(),l,new GameProfile(OWNER,"AreaProbe"),ClientInformation.createDefault()){@Override public void sendSystemMessage(Component c,boolean o){}@Override protected ItemCooldowns createItemCooldowns(){return new ItemCooldowns();}};knowledge(p);}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){var mode=System.getProperty("thaumcraft.thaumaturgyProbe","");if(!mode.equals("area-write")&&!mode.equals("area-read")||done||++ticks<60)return;var l=p.level();for(var face:Direction.values()){SealEntity s;if(mode.equals("area-write")){s=seal(face);check(!s.oriented()&&s.area().equals(new AABB(s.getBlockPos()).inflate(3)),"New seal changed legacy default");set(s,new SealArea(3,4,2));s.channel=7;s.priority=4;s.sample(8,new ItemStack(Items.EMERALD));s.setChanged();var tag=s.saveWithoutMetadata(l.registryAccess());tag.remove("seal_area");tag.remove("oriented_area");var old=new SealEntity(s.getBlockPos(),s.getBlockState());old.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),tag));check(!old.oriented()&&old.radius==3&&old.area().equals(new AABB(s.getBlockPos()).inflate(3)),"Missing area data changed old radius");int revision=s.revision();p.setPos(Vec3.atCenterOf(s.getBlockPos()).add(20,0,0));check(!s.edit(p,19)&&s.revision()==revision,"Out-of-reach area edit accepted");}else{s=(SealEntity)l.getBlockEntity(Q.east(face.ordinal()*4).relative(face));check(s!=null&&s.oriented()&&s.areaConfig().equals(new SealArea(3,4,2))&&s.channel==7&&s.priority==4&&s.sample(8).is(Items.EMERALD),"Saved six-face area/config/filter changed");check(s.area().equals(new SealArea(3,4,2).bounds(s.support(),face)),"Area orientation changed after restart");p.setPos(Vec3.atCenterOf(s.getBlockPos()));check(!s.edit(p,2),"Oriented area accepted legacy radius control");check(s.edit(p,17)&&!s.oriented()&&s.area().equals(new AABB(s.getBlockPos()).inflate(3))&&!s.edit(p,19),"Mode toggle changed old radius or enabled inactive axis control");check(s.edit(p,17)&&s.oriented()&&s.areaConfig().equals(new SealArea(3,4,2)),"Returning to oriented mode lost dimensions");}}
 var old=(SealEntity)l.getBlockEntity(DartProbe.Q.east(4));check(old!=null&&!old.oriented()&&old.radius==3&&old.area().equals(new AABB(old.getBlockPos()).inflate(3)),"Actual port.26 seal area changed");check(l.getEntitiesOfClass(WorkerGolem.class,new AABB(RankProbe.Q).inflate(3)).stream().anyMatch(g->g.progress().rank()==10&&g.getHealth()==7&&g.cargo.getItem(0).getCount()==9&&Math.abs(g.getBbWidth()-.56)<.000001&&Math.abs(g.getBbHeight()-1.08)<.000001),"Actual veteran changed");Thaumcraft.LOGGER.info(mode.equals("area-write")?"AREA_WRITE_OK: six faces, independent dimensions, permissions, old-data fallback, actual previous seal and veteran":"AREA_RESTART_OK: six oriented areas, ninth filter, owner/channel/priority and actual previous seal/veteran retained");done=true;e.server().halt(false);}
}
