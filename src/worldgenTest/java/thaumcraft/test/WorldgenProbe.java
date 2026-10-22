package thaumcraft.test;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.content.SimpleContent;
import thaumcraft.trees.*;
import thaumcraft.worldgen.MagicalForest;

@Mod.EventBusSubscriber(modid="thaumcraft")
public final class WorldgenProbe {
    static boolean done;static int ticks;public static volatile boolean ready;public static volatile BlockPos naturalSample;
    static String mode() {return System.getProperty("thaumcraft.worldgenProbe","");}
    static void check(boolean ok,String message) {if (!ok) throw new AssertionError(message);}
    static final BlockPos TEST=new BlockPos(6408,190,8);
    static void trees(ServerLevel level) {
        for (int x=TEST.getX()-16;x<=TEST.getX()+40;x++) for (int z=-16;z<=24;z++) {
            level.setBlock(new BlockPos(x,189,z),Blocks.DIRT.defaultBlockState(),2);
            for (int y=190;y<235;y++) level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),2);
        }
        for (boolean silver:new boolean[]{false,true}) {
            var p=silver?TEST.east(24):TEST;
            var sapling=TreeContent.BLOCKS.get("sapling_"+(silver?"silverwood":"greatwood")).get();
            var state=sapling.defaultBlockState().setValue(SaplingBlock.STAGE,1);level.setBlock(p,state,3);
            ((SaplingBlock)sapling).advanceTree(level,p,state,RandomSource.create(49));
            check(level.getBlockState(p).is(SimpleContent.BLOCKS.get(silver?"log_silverwood":"log_greatwood").get()),"Sapling failed to grow");
            int logs=0,leaves=0,horizontal=0,max=0;
            for (var q:BlockPos.betweenClosed(p.offset(-12,-1,-12),p.offset(12,40,12))) {
                var s=level.getBlockState(q);
                if (s.is(SimpleContent.BLOCKS.get(silver?"log_silverwood":"log_greatwood").get())) {logs++;max=Math.max(max,q.getY()-p.getY());if (s.getValue(RotatedPillarBlock.AXIS)!=Direction.Axis.Y) horizontal++;}
                if (s.is(TreeContent.BLOCKS.get(silver?"leaves_silverwood":"leaves_greatwood").get())) {leaves++;check(s.getValue(LeavesBlock.DISTANCE)<7,"Unsupported generated foliage");}
            }
            check(logs>35&&leaves>100&&horizontal>0,"Missing branches/canopy");
            check(silver?max>=8:max>=12,"Original height missing");
        }
        var obstacle=TEST.south(48);level.setBlock(obstacle.below(),Blocks.DIRT.defaultBlockState(),2);
        level.setBlock(obstacle.above(3),Blocks.CHEST.defaultBlockState(),3);
        check(!new SpecialTreeFeature(true).place(level,level.getChunkSource().getGenerator(),RandomSource.create(49),obstacle),"Overwrote solid obstruction");
        check(level.getBlockState(obstacle).isAir()&&level.getBlockState(obstacle.above(3)).is(Blocks.CHEST),"Failed growth made partial writes");
        var ceiling=new BlockPos(TEST.getX(),level.getMaxY()-2,TEST.getZ());level.setBlock(ceiling.below(),Blocks.DIRT.defaultBlockState(),2);
        check(!new SpecialTreeFeature(true).place(level,level.getChunkSource().getGenerator(),RandomSource.create(49),ceiling),"Height boundary accepted");
        Thaumcraft.LOGGER.info("WORLDGEN_TREES_OK: both native saplings, tall branched crowns, rotated logs, supported leaves, obstruction atomicity and height boundary");
    }
    static BlockPos locate(ServerLevel level) {
        var source=level.getChunkSource().getGenerator().getBiomeSource();
        check(source.possibleBiomes().stream().anyMatch(b->b.is(MagicalForest.KEY)),"Forest absent from possible biomes");
        var resolver=source.createUncachedResolver(level.getChunkSource().randomState());
        for (int radius=0;radius<=8192;radius+=64) for (int x=-radius;x<=radius;x+=64) for (int z:new int[]{-radius,radius}) {
            if (!resolver.getNoiseBiome(QuartPos.fromBlock(x),18,QuartPos.fromBlock(z)).is(MagicalForest.KEY)) continue;
            boolean interior=true;
            for (int dx:new int[]{-32,32}) for (int dz:new int[]{-32,32}) interior&=resolver.getNoiseBiome(QuartPos.fromBlock(x+dx),18,QuartPos.fromBlock(z+dz)).is(MagicalForest.KEY);
            if (interior) return new BlockPos(x,72,z);
        }
        throw new AssertionError("Could not locate natural magical forest");
    }
    static void natural(ServerLevel level) {
        var p=locate(level);int cx=p.getX()>>4,cz=p.getZ()>>4;
        int trees=0,magic=0,ambient=0;BlockPos sample=null;
        for (int dx=-2;dx<=2;dx++) for (int dz=-2;dz<=2;dz++) {
            var chunk=level.getChunk(cx+dx,cz+dz);
            for (var q:BlockPos.betweenClosed(new BlockPos((cx+dx)*16,50,(cz+dz)*16),new BlockPos((cx+dx)*16+15,150,(cz+dz)*16+15))) {
                var s=chunk.getBlockState(q);
                if (s.is(SimpleContent.BLOCKS.get("log_greatwood").get())||s.is(SimpleContent.BLOCKS.get("log_silverwood").get())) {magic++;if(sample==null&&level.getBiome(q).is(MagicalForest.KEY))sample=q.immutable();}
                if (s.is(Blocks.OAK_LOG)) trees++;
                if (s.is(thaumcraft.devices.DeviceContent.GRASS.get())) ambient++;
            }
        }
        check(sample!=null&&trees>20&&magic>20&&ambient>0,"Natural decoration missing: "+trees+","+magic+","+ambient);
        int y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,p.getX(),p.getZ());
        check(level.getBiome(p.atY(y)).is(MagicalForest.KEY),"Generated biome differs from resolver");
        var tag=new net.minecraft.nbt.CompoundTag();tag.putInt("x",sample.getX());tag.putInt("y",sample.getY());tag.putInt("z",sample.getZ());
        try {java.nio.file.Files.writeString(java.nio.file.Path.of("worldgen49-proof.txt"),sample.getX()+","+sample.getY()+","+sample.getZ());}catch(Exception e){throw new RuntimeException(e);}
        Thaumcraft.LOGGER.info("WORLDGEN_NATURAL_OK: forest {}, oak logs {}, magical logs {}, ambient grass {}; saved sample {}",p,trees,magic,ambient,sample);
        if (mode().equals("client")) {
            var player=level.players().getFirst();
            player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
            naturalSample=sample;
            int cameraX=sample.getX()+32,cameraZ=sample.getZ()+48;
            int cameraY=Math.max(sample.getY()+32,level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,cameraX,cameraZ)+16);
            player.teleportTo(level,cameraX+.5,cameraY,cameraZ+.5,Set.of(),146,27,true);
            player.getAbilities().flying=true;player.onUpdateAbilities();level.getServer().clockManager().setTotalTicks(level.dimensionType().defaultClock().orElseThrow(),6000);ready=true;
            Thaumcraft.LOGGER.info("WORLDGEN_CLIENT_READY: natural magical forest and native tree models");
        }
    }
    @SubscribeEvent public static void tick(TickEvent.LevelTickEvent.Post event) {
        if (mode().isEmpty()||mode().startsWith("structures")||done||!(event.level() instanceof ServerLevel level)||level.dimension()!=net.minecraft.world.level.Level.OVERWORLD) return;
        if (mode().equals("client")&&level.players().isEmpty()) return;
        if (++ticks<100) return;
        done=true;
        if (mode().startsWith("read")) {
            if (mode().equals("read-disabled")) check(level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes().stream().noneMatch(b->b.is(MagicalForest.KEY)),"Disabled biome still generated");
            try {
                var parts=java.nio.file.Files.readString(java.nio.file.Path.of("worldgen49-proof.txt")).split(",");
                var p=new BlockPos(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),Integer.parseInt(parts[2]));
                var s=level.getBlockState(p);
                check(s.is(SimpleContent.BLOCKS.get("log_greatwood").get())||s.is(SimpleContent.BLOCKS.get("log_silverwood").get()),"Natural tree lost on restart");
                check(level.getBiome(p).is(MagicalForest.KEY),"Saved biome lost on restart");
                check(level.getBlockState(TEST).is(SimpleContent.BLOCKS.get("log_greatwood").get()),"Grown tree lost on restart");
                Thaumcraft.LOGGER.info("WORLDGEN_RESTART_OK: natural biome, natural tree and grown tree survived second JVM");
            }catch(Exception e){throw new RuntimeException(e);}
        } else {trees(level);natural(level);}
        if (!mode().equals("client")) level.getServer().halt(false);
    }
}
