package thaumcraft.test;

import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.content.SimpleContent;
import thaumcraft.worldgen.MagicalForest;

/** Opt-in checks of real new terrain; never included in the release JAR. */
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class ForestSceneryProbe {
    static int ticks;static boolean done;
    public static volatile boolean ready;
    public static volatile BlockPos mushroom,rock;
    static String mode(){return System.getProperty("thaumcraft.worldgenProbe","");}
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    static void save(){try{Files.writeString(Path.of("decoration53-proof.txt"),mushroom.getX()+","+mushroom.getY()+","+mushroom.getZ()+","+rock.getX()+","+rock.getY()+","+rock.getZ());}catch(Exception e){throw new RuntimeException(e);}}
    static void read(ServerLevel l){try{var a=Files.readString(Path.of("decoration53-proof.txt")).split(",");mushroom=new BlockPos(Integer.parseInt(a[0]),Integer.parseInt(a[1]),Integer.parseInt(a[2]));rock=new BlockPos(Integer.parseInt(a[3]),Integer.parseInt(a[4]),Integer.parseInt(a[5]));check(l.getBlockState(mushroom).is(Blocks.RED_MUSHROOM_BLOCK)||l.getBlockState(mushroom).is(Blocks.BROWN_MUSHROOM_BLOCK),"Mushroom lost on reload");check(l.getBlockState(rock).is(Blocks.MOSSY_COBBLESTONE),"Rock lost on reload");check(l.getBiome(mushroom).is(MagicalForest.KEY),"Saved biome lost");}catch(Exception e){throw new RuntimeException(e);}}
    static BlockPos freshForest(ServerLevel l){
        var source=l.getChunkSource().getGenerator().getBiomeSource();var resolver=source.createUncachedResolver(l.getChunkSource().randomState());
        var regions=net.minecraft.world.level.dimension.DimensionType.getStorageFolder(l.dimension(),l.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)).resolve("region");
        check(Files.isDirectory(regions),"Native dimension region directory missing: "+regions);
        for(int radius=0;radius<=8192;radius+=64)for(int dx=-radius;dx<=radius;dx+=64)for(int dz:new int[]{-radius,radius}){
            int x=100000+dx,z=100000+dz;boolean valid=true;
            for(int ox:new int[]{-64,0,64})for(int oz:new int[]{-64,0,64}){
                valid&=!Files.exists(regions.resolve("r."+((x+ox)>>9)+"."+((z+oz)>>9)+".mca"));
            }
            for(int ox:new int[]{-16,0,16})for(int oz:new int[]{-16,0,16})valid&=resolver.getNoiseBiome(QuartPos.fromBlock(x+ox),18,QuartPos.fromBlock(z+oz)).is(MagicalForest.KEY);
            if(valid){Thaumcraft.LOGGER.info("DECORATION_FRESH_REGION: forest {},{}, all sampled region files absent before generation",x,z);return new BlockPos(x,72,z);}
        }
        throw new AssertionError("No fresh forest interior found");
    }
    static void natural(ServerLevel l){
        var registry=l.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
        var boulders=registry.getOrThrow(net.minecraft.resources.ResourceKey.create(Registries.PLACED_FEATURE,SimpleContent.id("magical_boulders")));
        var mushrooms=registry.getOrThrow(net.minecraft.resources.ResourceKey.create(Registries.PLACED_FEATURE,SimpleContent.id("magical_mushrooms")));
        check(boulders.value().feature().is(net.minecraft.resources.ResourceKey.create(Registries.FEATURE,net.minecraft.resources.Identifier.parse("minecraft:forest_rock"))),"Not native mossy rock");
        var biome=l.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(MagicalForest.KEY).value();
        check(biome.getGenerationSettings().features().get(2).contains(boulders),"Rocks not in local modifications");
        check(biome.getGenerationSettings().features().get(9).contains(mushrooms),"Mushrooms not in vegetation");
        var at=freshForest(l);var rocks=new ArrayList<BlockPos>();var caps=new ArrayList<BlockPos>();int stems=0,red=0,brown=0;
        for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++){
            var chunk=l.getChunk((at.getX()>>4)+dx,(at.getZ()>>4)+dz);var cp=chunk.getPos();
            for(var p:BlockPos.betweenClosed(new BlockPos(cp.getMinBlockX(),40,cp.getMinBlockZ()),new BlockPos(cp.getMaxBlockX(),160,cp.getMaxBlockZ()))){
                var state=chunk.getBlockState(p);
                if(state.is(Blocks.MUSHROOM_STEM))stems++;
                if(state.is(Blocks.RED_MUSHROOM_BLOCK)||state.is(Blocks.BROWN_MUSHROOM_BLOCK)){
                    if(!l.getBiome(p).is(MagicalForest.KEY))continue;
                    caps.add(p.immutable());if(state.is(Blocks.RED_MUSHROOM_BLOCK))red++;else brown++;
                }
                if(state.is(Blocks.MOSSY_COBBLESTONE)&&chunk.getBlockState(p.above()).isAir()
                    &&p.getY()>=l.getHeight(Heightmap.Types.OCEAN_FLOOR,p.getX(),p.getZ())-2&&l.getBiome(p).is(MagicalForest.KEY))rocks.add(p.immutable());
            }
        }
        check(!rocks.isEmpty()&&red>0&&brown>0&&stems>0,"Missing natural scenery: rocks="+rocks.size()+", red="+red+", brown="+brown+", stems="+stems);
        double best=Double.MAX_VALUE;
        for(var cap:caps)for(var b:rocks){double distance=cap.distSqr(b);if(distance<best){best=distance;mushroom=cap;rock=b;}}
        check(mushroom!=null,"No scenery sample");save();
        Thaumcraft.LOGGER.info("DECORATION_NATURAL_OK: untouched new terrain, {} surface rock blocks, {} red caps, {} brown caps, {} stems; samples {} and {}",rocks.size(),red,brown,stems,mushroom,rock);
    }
    @SubscribeEvent public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event){
        if(mode().equals("decoration-client")){event.getEntity().setHealth(event.getEntity().getMaxHealth());if(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)p.setGameMode(GameType.CREATIVE);}
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post event){
        if(!mode().startsWith("decoration")||done||++ticks<100)return;
        var l=event.server().overworld();if(mode().equals("decoration-client")&&l.players().isEmpty())return;
        done=true;
        if(mode().equals("decoration-read")){read(l);Thaumcraft.LOGGER.info("DECORATION_RESTART_OK: natural mushrooms, mossy rock and biome survive cold restart");}
        else natural(l);
        if(mode().equals("decoration-client")){
            var player=l.players().getFirst();player.setGameMode(GameType.CREATIVE);
            var middle=net.minecraft.world.phys.Vec3.atCenterOf(mushroom).add(net.minecraft.world.phys.Vec3.atCenterOf(rock)).scale(.5);
            var view=middle.add(-12,7,15);var delta=middle.subtract(view.add(0,1.62,0));
            float yaw=(float)Math.toDegrees(Math.atan2(-delta.x,delta.z)),pitch=(float)-Math.toDegrees(Math.atan2(delta.y,Math.sqrt(delta.x*delta.x+delta.z*delta.z)));
            player.teleportTo(l,view.x,view.y,view.z,Set.of(),yaw,pitch,true);player.getAbilities().flying=true;player.onUpdateAbilities();player.setNoGravity(true);
            l.getServer().clockManager().setTotalTicks(l.dimensionType().defaultClock().orElseThrow(),6000);ready=true;
            Thaumcraft.LOGGER.info("DECORATION_CLIENT_READY: natural scenery {} and {}",mushroom,rock);
        }else event.server().halt(false);
    }
}
