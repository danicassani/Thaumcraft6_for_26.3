package thaumcraft.test;

import java.util.*;
import java.nio.file.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.content.SimpleContent;
import thaumcraft.treasure.TreasureBlock;
import thaumcraft.trees.*;
import thaumcraft.worldgen.*;

@Mod.EventBusSubscriber(modid="thaumcraft")
public final class StructuresProbe {
    static int ticks;static boolean done;static PrimedTnt primed;
    public static volatile boolean ready;public static volatile boolean outside;public static volatile BlockPos naturalOrigin;
    static String mode() {return System.getProperty("thaumcraft.worldgenProbe","");}
    static void check(boolean b,String why) {if (!b) throw new AssertionError(why);}
    @SubscribeEvent public static void join(EntityJoinLevelEvent event) {if(mode().startsWith("structures")&&event.getEntity() instanceof PrimedTnt tnt) primed=tnt;}
    static void pieces(ServerLevel level) {
        var p=new BlockPos(10248,180,8);long seed=0;
        while (!new MoundPiece(p,seed).trapped()) seed++;
        var piece=new MoundPiece(p,seed);var bb=piece.getBoundingBox();var contexts=StructurePieceSerializationContext.fromLevel(level);
        for (int x=bb.minX()>>4;x<=bb.maxX()>>4;x++) for (int z=bb.minZ()>>4;z<=bb.maxZ()>>4;z++) {
            level.getChunk(x,z);var clip=new BoundingBox(x*16,level.getMinY(),z*16,x*16+15,level.getMaxY()-1,z*16+15);
            piece.postProcess(level,level.structureManager(),level.getChunkSource().getGenerator(),RandomSource.create(50),clip,new ChunkPos(x,z),p);
            piece=new MoundPiece(piece.createTag(contexts));
        }
        check(level.getBlockState(p.offset(9,1,7)).getBlock() instanceof TreasureBlock,"Cache missing across chunk pieces");
        var cache=p.offset(9,1,7);var state=level.getBlockState(cache);
        var drops=Block.getDrops(state,level,cache,null,null,ItemStack.EMPTY);check(!drops.isEmpty(),"Placed cache has no rewards");
        level.destroyBlock(cache,true);
        var copy=new MoundPiece(piece.createTag(contexts));
        for (int x=bb.minX()>>4;x<=bb.maxX()>>4;x++) for (int z=bb.minZ()>>4;z<=bb.maxZ()>>4;z++) copy.postProcess(level,level.structureManager(),level.getChunkSource().getGenerator(),RandomSource.create(50),new BoundingBox(x*16,level.getMinY(),z*16,x*16+15,level.getMaxY()-1,z*16+15),new ChunkPos(x,z),p);
        check(level.getBlockState(cache).isAir(),"Piece replay recreated opened cache");
        check(level.getBlockState(p.offset(10,-1,9)).is(Blocks.TNT),"Trap missing");
        var chest=(ChestBlockEntity)level.getBlockEntity(p.offset(10,1,9));check(chest!=null,"Chest missing");
        var actor=new ServerPlayer(level.getServer(),level,new GameProfile(UUID.fromString("ee42782c-4105-44b6-80b3-270ca8055041"),"MoundProbe"),ClientInformation.createDefault());
        actor.setPos(p.getX()+9.5,p.getY()+1,p.getZ()+9.5);chest.startOpen(actor);
        check(primed!=null&&!level.getBlockState(p.offset(10,-1,9)).is(Blocks.TNT),"Opening trapped chest did not prime native TNT");
        primed.discard();primed=null;chest.stopOpen(actor);
        Thaumcraft.LOGGER.info("STRUCTURES_PIECES_OK: four clipped chunks, piece serialization between chunks, physical cache rewards, no cache replay, trapped chest primes native TNT");
    }
    static void infested(ServerLevel level) {
        var p=new BlockPos(10328,180,8);
        for (int x=-12;x<=12;x++) for(int z=-12;z<=12;z++) {
            level.setBlock(p.offset(x,-1,z),Blocks.DIRT.defaultBlockState(),2);level.setBlock(p.offset(x,-2,z),Blocks.STONE.defaultBlockState(),2);
            for (int y=0;y<40;y++) level.setBlock(p.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);
        }
        check(new SpecialTreeFeature(false).place(level,level.getChunkSource().getGenerator(),RandomSource.create(50),p),"Infested tree base failed");
        check(InfestedGreatwood.place(level,RandomSource.create(50),p),"Infestation failed");
        check(level.getBlockEntity(p.below()) instanceof SpawnerBlockEntity,"Spider spawner absent");
        var spawner=(SpawnerBlockEntity)level.getBlockEntity(p.below());
        check(spawner.getSpawner().getOrCreateDisplayEntity(level,p.below()).getType()==EntityTypes.CAVE_SPIDER,"Wrong spider type");
        check(level.getBlockEntity(p.below(2)) instanceof ChestBlockEntity,"Tree chest absent");
        check(!InfestedGreatwood.place(level,RandomSource.create(50),p),"Infestation overwrites existing inventories");
        Thaumcraft.LOGGER.info("STRUCTURES_INFESTED_OK: cave spider spawner and dungeon chest under greatwood, existing block entities protected");
    }
    static BlockPos natural(ServerLevel level) {
        var holder=level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ResourceKey.create(Registries.STRUCTURE,SimpleContent.id("mound")));
        var generator=level.getChunkSource().getGenerator();
        var located=generator.findNearestMapStructure(level,HolderSet.direct(holder),new BlockPos(-4096,64,-4096),32,false);
        check(located!=null,"Native locate found no natural mound");var cp=new ChunkPos(located.getFirst().getX()>>4,located.getFirst().getZ()>>4);
        var start=level.structureManager().getStartForStructure(holder.value(),level.getChunk(cp.x(),cp.z(),ChunkStatus.STRUCTURE_STARTS));
        check(start!=null&&start.isValid()&&start.getPieces().getFirst() instanceof MoundPiece,"Native saved start missing");
        var piece=(MoundPiece)start.getPieces().getFirst();var bb=piece.getBoundingBox();
        for (int x=bb.minX()>>4;x<=bb.maxX()>>4;x++) for (int z=bb.minZ()>>4;z<=bb.maxZ()>>4;z++) level.getChunk(x,z);
        var p=piece.origin();
        var biome=level.getBiome(p.offset(9,9,9));
        check(!biome.is(net.minecraft.world.level.biome.Biomes.DESERT)&&!biome.is(net.minecraft.world.level.biome.Biomes.MUSHROOM_FIELDS),"Incompatible surface biome");
        for (int z:new int[]{7,11}) check(level.getBlockState(p.offset(9,1,z)).getBlock() instanceof TreasureBlock,"Natural treasure absent");
        for (int i=0;i<2;i++) {
            var q=p.offset(4,5,i==0?4:14);check(level.getBlockEntity(q) instanceof SpawnerBlockEntity,"Natural spawner absent");
            var spawner=(SpawnerBlockEntity)level.getBlockEntity(q);check(spawner.getSpawner().getOrCreateDisplayEntity(level,q).getType()==(i==0?EntityTypes.SKELETON:EntityTypes.ZOMBIE),"Natural spawner wrong type");
        }
        var chest=(ChestBlockEntity)level.getBlockEntity(p.offset(10,1,9));check(chest!=null,"Natural dungeon chest absent");
        boolean loot=false;for(int slot=0;slot<27;slot++) loot|=!chest.getItem(slot).isEmpty();check(loot,"Dungeon loot empty");
        chest.setItem(26,new ItemStack(Items.NAME_TAG));
        try {Files.writeString(Path.of("structures50-proof.txt"),p.getX()+","+p.getY()+","+p.getZ());}catch(Exception e){throw new RuntimeException(e);}
        Thaumcraft.LOGGER.info("STRUCTURES_NATURAL_OK: native /locate and saved structure start at {}, two treasures, skeleton/zombie spawners, native dungeon loot, trap {}",p,piece.trapped());
        Thaumcraft.LOGGER.info("STRUCTURES_BIOME_OK: {}",biome.unwrapKey());
        return p;
    }
    static void read(ServerLevel level) {
        try {
            var parts=Files.readString(Path.of("structures50-proof.txt")).split(",");var p=new BlockPos(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),Integer.parseInt(parts[2]));
            check(level.getBlockState(p.offset(9,1,7)).getBlock() instanceof TreasureBlock&&level.getBlockState(p.offset(9,1,11)).getBlock() instanceof TreasureBlock,"Unopened treasures lost");
            var holder=level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ResourceKey.create(Registries.STRUCTURE,SimpleContent.id("mound")));
            var start=level.structureManager().getStartForStructure(holder.value(),level.getChunk(p.getX()>>4,p.getZ()>>4));
            check(start!=null&&start.getPieces().getFirst() instanceof MoundPiece,"Native piece type lost on restart");
            var piece=(MoundPiece)start.getPieces().getFirst();check(piece.origin().equals(p),"Mound bounding box lost");
            var bb=piece.getBoundingBox();
            for (int x=bb.minX()>>4;x<=bb.maxX()>>4;x++) for (int z=bb.minZ()>>4;z<=bb.maxZ()>>4;z++) piece.postProcess(level,level.structureManager(),level.getChunkSource().getGenerator(),RandomSource.create(50),new BoundingBox(x*16,level.getMinY(),z*16,x*16+15,level.getMaxY()-1,z*16+15),new ChunkPos(x,z),p);
            var chest=(ChestBlockEntity)level.getBlockEntity(p.offset(10,1,9));check(chest!=null&&chest.getItem(26).is(Items.NAME_TAG),"Dungeon inventory lost or regenerated");
            check(level.getBlockEntity(new BlockPos(10328,179,8)) instanceof SpawnerBlockEntity&&level.getBlockEntity(new BlockPos(10328,178,8)) instanceof ChestBlockEntity,"Infested greatwood lost");
            check(level.getBlockEntity(p.offset(4,5,4)) instanceof SpawnerBlockEntity&&level.getBlockEntity(p.offset(4,5,14)) instanceof SpawnerBlockEntity,"Spawners lost");
            Thaumcraft.LOGGER.info("STRUCTURES_RESTART_OK: two unopened treasures, dungeon inventory and spawners survived cold restart");
        }catch(Exception e){throw new RuntimeException(e);}
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post event) {
        if (!mode().startsWith("structures")) return;
        var level=event.server().overworld();
        if (done) {
            if (ready&&!outside&&!level.players().isEmpty()&&level.players().getFirst().getInventory().getSelectedSlot()==0) {
                var p=naturalOrigin;var actor=level.players().getFirst();
                actor.teleportTo(level,p.getX()-10.5,p.getY()+15,p.getZ()+9.5,Set.of(),-90,15,true);
                outside=true;Thaumcraft.LOGGER.info("STRUCTURES_EXTERIOR_READY: inspection camera outside the intact original barred entrance");
            }
            return;
        }
        if (++ticks<100) return;
        if (mode().equals("structures-client")&&level.players().isEmpty()) return;
        done=true;
        if (mode().equals("structures-read")) read(level);
        else {
            pieces(level);infested(level);var p=natural(level);
            if (mode().equals("structures-client")) {
                var actor=level.players().getFirst();actor.setGameMode(net.minecraft.world.level.GameType.CREATIVE);actor.getInventory().setSelectedSlot(8);
                actor.teleportTo(level,p.getX()+7.4,p.getY()+1,p.getZ()+9.5,Set.of(),-90,6,true);
                actor.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION,12000,0,false,false));
                actor.getAbilities().flying=true;actor.onUpdateAbilities();
                level.getServer().clockManager().setTotalTicks(level.dimensionType().defaultClock().orElseThrow(),6000);naturalOrigin=p;ready=true;
                Thaumcraft.LOGGER.info("STRUCTURES_CLIENT_READY: natural treasure chamber, two unopened caches and dungeon chest; night vision only for inspection");
                return;
            }
        }
        event.server().halt(false);
    }
}
