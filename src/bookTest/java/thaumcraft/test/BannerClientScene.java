package thaumcraft.test;
import java.util.*;
import java.nio.file.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.banners.*;
import thaumcraft.mirrors.*;
import thaumcraft.rifts.*;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class BannerClientScene {
 public static final BlockPos P=new BlockPos(68408,150,0),M=P.east(30);public static volatile boolean ready,photo;public static volatile String view="paint";static ServerPlayer actor;static int ticks,stage;
 @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(System.getProperty("thaumcraft.bookProbe","").equals("banner-client")&&e.getEntity() instanceof ServerPlayer p)actor=p;}
 static void position(double x,double y,double z,Vec3 target){actor.teleportTo(actor.level(),x,y,z,Set.of(),180,10,true);actor.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,target);}
 static void camera(String name){actor.level().getServer().clockManager().setTotalTicks(actor.level().dimensionType().defaultClock().orElseThrow(),6000);view=name;actor.getAbilities().flying=true;actor.onUpdateAbilities();switch(name){case "details"->position(P.getX()+4,P.getY()+.2,P.getZ()+6,Vec3.atCenterOf(P.east(4)).add(0,.3,0));case "wall"->position(P.getX()+7,P.getY()+2,P.getZ()+12,Vec3.atCenterOf(P.offset(7,2,6)));case "mirrors"->position(M.getX()+4,M.getY()+3,M.getZ()+7,Vec3.atCenterOf(M.east(2)));case "rift"->position(M.getX()+13,M.getY()+4,M.getZ()+9,Vec3.atCenterOf(M.offset(12,5,0)));default->position(P.getX()+8,P.getY()+5,P.getZ()+17,Vec3.atCenterOf(P.east(8)).add(0,.2,0));}thaumcraft.Thaumcraft.LOGGER.info("BANNER72_CLIENT_VIEW_OK: {}",name);}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(actor==null)return;var l=actor.level();if(stage==0&&++ticks>80){e.server().tickRateManager().setFrozen(false);l.getServer().clockManager().setTotalTicks(l.dimensionType().defaultClock().orElseThrow(),6000);for(int x=-3;x<=49;x++)for(int z=-7;z<=14;z++)BannerProbe.set(l,P.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());actor.gameMode.changeGameModeForPlayer(GameType.CREATIVE);actor.getAbilities().flying=false;actor.onUpdateAbilities();actor.getInventory().clearContent();var aspects=List.of("aer","ignis","alienis","aqua","lux","herba","vitium","perditio","ordo","gelum","praecantatio","volatus","terra","victus","aversio","tenebrae");for(var d:DyeColor.values()){var at=P.offset(d.getId()%8*2,0,-(d.getId()/8)*4);var b=BannerProbe.banner(l,at,d,false,0);if(d!=DyeColor.WHITE){var item=new ItemStack(BannerContent.COLOURS.get(d).get());item.set(BannerContent.ASPECT.get(),aspects.get(d.getId()));b.placeContents(item);}}BannerProbe.banner(l,P.east(17),null,false,0);for(int i=0;i<4;i++){var d=List.of(DyeColor.PURPLE,DyeColor.BLUE,DyeColor.RED,DyeColor.CYAN).get(i);var b=BannerProbe.banner(l,P.offset(4+i*2,2,6),d,true,0);var item=new ItemStack(BannerContent.COLOURS.get(d).get());item.set(BannerContent.ASPECT.get(),List.of("praecantatio","aqua","ignis","aer").get(i));b.placeContents(item);}
 for(var face:Direction.values()){var block=(face.ordinal()%2==0?MirrorContent.MIRROR:MirrorContent.ESSENTIA).get();var at=M.east(face.ordinal()*2);var back=at.relative(face.getOpposite());BannerProbe.set(l,back,Blocks.STONE_BRICKS.defaultBlockState());BannerProbe.set(l,at,block.defaultBlockState().setValue(MirrorBlock.FACING,face));var peerAt=at.north(5);BannerProbe.set(l,peerAt.relative(face.getOpposite()),Blocks.STONE_BRICKS.defaultBlockState());BannerProbe.set(l,peerAt,block.defaultBlockState().setValue(MirrorBlock.FACING,face));((MirrorEntity)l.getBlockEntity(at)).bind(((MirrorEntity)l.getBlockEntity(peerAt)).address());}
 var oldRifts=l.getEntitiesOfClass(FluxRiftEntity.class,new net.minecraft.world.phys.AABB(M).inflate(25));oldRifts.forEach(Entity::discard);var rift=new FluxRiftEntity(RiftContent.RIFT.get(),l);rift.setPos(Vec3.atCenterOf(M.offset(12,5,0)));rift.setSeed(72);rift.setSize(60);rift.setStability(0);l.addFreshEntity(rift);
 actor.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,thaumcraft.alchemy.AlchemyContent.filled(thaumcraft.alchemy.AlchemyContent.PHIAL.get(),"aer",10));position(P.getX()+.5,P.getY(),P.getZ()+3.3,Vec3.atCenterOf(P).add(0,.4,0));ready=true;stage=1;thaumcraft.Thaumcraft.LOGGER.info("BANNER72_CLIENT_READY: original banners, six linked mirror faces and isolated live rift");return;}
 if(stage==1&&((AspectBannerEntity)l.getBlockEntity(P)).aspect().equals("aer")){actor.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,ItemStack.EMPTY);stage=2;photo=true;camera("banners");thaumcraft.Thaumcraft.LOGGER.info("BANNER72_CLIENT_PAINT_OK: native right-click applied the phial glyph");}
 if(stage==2&&Files.exists(Path.of("../work/banner72-view"))){try{var name=Files.readString(Path.of("../work/banner72-view")).trim();Files.delete(Path.of("../work/banner72-view"));camera(name);}catch(Exception ex){throw new RuntimeException(ex);}}
 }
}
