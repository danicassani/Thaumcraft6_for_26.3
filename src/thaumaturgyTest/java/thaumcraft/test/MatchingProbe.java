package thaumcraft.test;
import java.util.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.*;
import thaumcraft.golems.*;
import static thaumcraft.test.ThaumaturgyProbe.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID)
public final class MatchingProbe {
 static final BlockPos Q=new BlockPos(1400,205,8);static ServerPlayer p;static int ticks;static boolean done;
 static boolean mode(){return System.getProperty("thaumcraft.thaumaturgyProbe","").startsWith("matching-")&&!System.getProperty("thaumcraft.thaumaturgyProbe","").equals("matching-client");}
 @SubscribeEvent public static void start(ServerStartedEvent e){if(!mode())return;var l=e.getServer().overworld();l.setChunkForced(87,0,true);l.setChunkForced(75,0,true);l.setChunkForced(56,0,true);l.setChunkForced(65,0,true);p=new ServerPlayer(e.getServer(),l,new GameProfile(AreaProbe.OWNER,"MatchingProbe"),ClientInformation.createDefault()){@Override public void sendSystemMessage(Component c,boolean o){}@Override protected ItemCooldowns createItemCooldowns(){return new ItemCooldowns();}};knowledge(p);}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(!mode()||done||++ticks<60)return;var l=p.level();MatchingRules.verify();boolean write=System.getProperty("thaumcraft.thaumaturgyProbe").equals("matching-write");SealEntity s;
 if(write){s=GolemProbe.seal(p,SealKind.PICKUP_ADVANCED,Q);p.setPos(Vec3.atCenterOf(Q));s.sample(8,MatchingRules.tool(7,"Saved inspector"));check(s.edit(p,24)&&s.edit(p,25)&&s.edit(p,26),"Matching controls rejected");s.setChanged();var tag=s.saveWithoutMetadata(l.registryAccess());tag.remove("match_damage");tag.remove("match_tags");tag.remove("match_mod");for(boolean exact:new boolean[]{true,false}){tag.putBoolean("exact",exact);var old=new SealEntity(Q,s.getBlockState());old.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),tag));check(old.exact==exact&&old.matchDamage==exact&&!old.matchTags&&!old.matchMod,"Legacy exact migration changed matching");}int revision=s.revision();p.setPos(Vec3.atCenterOf(Q).add(20,0,0));check(!s.edit(p,24)&&s.revision()==revision,"Remote matching edit accepted");var basic=GolemProbe.seal(p,SealKind.PICKUP,Q.east(3));p.setPos(Vec3.atCenterOf(basic.getBlockPos()));check(!basic.edit(p,24)&&!basic.edit(p,25)&&!basic.edit(p,26),"Basic seal accepted advanced matching");}
 else{s=(SealEntity)l.getBlockEntity(Q);check(s!=null&&s.exact&&!s.matchDamage&&s.matchTags&&s.matchMod&&s.sample(8).getDamageValue()==7&&s.sample(8).getHoverName().getString().equals("Saved inspector")&&s.owned(p),"Matching state lost on restart");p.setPos(Vec3.atCenterOf(Q));check(s.edit(p,7)&&!s.exact&&!s.matchDamage&&s.edit(p,7)&&s.exact&&s.matchDamage,"Exact preset no longer resets damage");check(s.edit(p,24),"Cannot restore saved damage mode");}
 var previous=(SealEntity)l.getBlockEntity(AreaProbe.Q.relative(Direction.DOWN));check(previous!=null&&previous.oriented()&&previous.areaConfig().equals(new SealArea(3,4,2))&&previous.exact&&previous.matchDamage&&!previous.matchTags&&!previous.matchMod,"Actual port.27 area/filter changed");
 Thaumcraft.LOGGER.info(write?"MATCHING_WRITE_OK: native comparison rules, permissions, old-data fallback and saved settings":"MATCHING_RESTART_OK: persisted matching, ninth named damaged sample, owner and actual port.27 area preserved");done=true;e.server().halt(false);}
}
