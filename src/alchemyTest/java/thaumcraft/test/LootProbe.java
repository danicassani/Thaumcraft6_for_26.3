package thaumcraft.test;

import java.util.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.crystals.CrystalContent;
import thaumcraft.infusion.CuriosityContent;
import thaumcraft.golems.*;
import thaumcraft.taint.TaintContent;

@Mod.EventBusSubscriber(modid="thaumcraft")
public final class LootProbe {
    public static final BlockPos P=new BlockPos(3888,205,8);
    public static volatile boolean ready;
    static int ticks;
    static boolean done;
    static String mode() { return System.getProperty("thaumcraft.alchemyProbe",""); }
    static void check(boolean value,String message) { if (!value) throw new AssertionError(message); }
    static LootTable table(ServerLevel level,String id) {
        return level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse("thaumcraft:"+id)));
    }
    static List<ItemStack> block(ServerLevel level,String name,BlockState state,ItemStack tool,long seed,Float blast) {
        var params=new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(P))
            .withParameter(LootContextParams.BLOCK_STATE,state).withParameter(LootContextParams.TOOL,tool);
        if (blast!=null) params.withParameter(LootContextParams.EXPLOSION_RADIUS,blast);
        return table(level,"blocks/"+name).getRandomItems(params.create(LootContextParamSets.BLOCK),seed*0x9E3779B97F4A7C15L);
    }
    static void one(List<ItemStack> drops,Item item,String label) {
        check(drops.size()==1 && drops.getFirst().is(item) && drops.getFirst().getCount()==1,label+": "+drops);
    }
    static List<ItemStack> tests(ServerLevel level,ServerPlayer actor) {
        var pick=new ItemStack(Items.DIAMOND_PICKAXE);
        var silk=pick.copy();silk.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH),1);
        var fortune=pick.copy();fortune.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE),3);
        var porous=TaintContent.POROUS.get().defaultBlockState();
        var saved=new ArrayList<ItemStack>();
        saved.add(block(level,"stone_porous",porous,silk,47,null).getFirst());
        for (String id:List.of("taint_soil","taint_crust","taint_rock")) {
            var state=TaintContent.BLOCKS.get(id).get().defaultBlockState();
            for (int n=1;n<=64;n++) one(block(level,id,state,silk,n,null),state.getBlock().asItem(),"Silk "+id);
            int successes=0;
            for (int n=1;n<=1024;n++) for (var stack:block(level,id,state,pick,n,null)) {
                check("vitium".equals(stack.get(CrystalContent.ASPECT.get())),"Taint crystal lost its actual aspect");
                check(stack.getCount()==1,"Taint crystal amount");successes++;
                if (saved.size()==1) saved.add(stack.copy());
            }
            check(successes>25 && successes<120,"Taint chance not applied: "+successes);
        }
        for (int n=1;n<=64;n++) one(block(level,"stone_porous",porous,silk,n,null),TaintContent.POROUS.get().asItem(),"Porous silk");
        saved.add(block(level,"taint_crust",TaintContent.BLOCKS.get("taint_crust").get().defaultBlockState(),silk,47,null).getFirst());
        Thaumcraft.LOGGER.info("LOOT_SILK_OK: exact intact blocks with Silk Touch; non-silk taint yields only chance-gated vitium crystals");
        int plain=0,bonus=0,blasted=0;
        ItemStack gravel=ItemStack.EMPTY;
        for (int n=1;n<=4096;n++) {
            for (var stack:block(level,"stone_porous",porous,pick,n,null)) {
                check(stack.getCount()==1 && !stack.is(TaintContent.POROUS.get().asItem()),"Porous non-silk output");
                if (!stack.is(Items.GRAVEL)) plain++;else gravel=stack.copy();
            }
            for (var stack:block(level,"stone_porous",porous,fortune,n,null)) if (!stack.is(Items.GRAVEL)) bonus++;
            if (!block(level,"stone_porous",porous,pick,n,100F).isEmpty()) blasted++;
        }
        check(plain>150 && plain<400 && bonus>850 && bonus<1300 && bonus>plain*2,"Fortune probabilities: "+plain+"/"+bonus);
        check(blasted>0 && blasted<120,"Explosion survival ignored: "+blasted);
        saved.add(gravel);
        Thaumcraft.LOGGER.info("LOOT_FORTUNE_OK: 4096 deterministic draws; normal={}, fortuneIII={}, explosion survivors={}",plain,bonus,blasted);
        var press=GolemContent.BUILDER.get().defaultBlockState();
        one(block(level,"golem_builder",press.setValue(GolemBuilderBlock.ASSEMBLED,false),pick,47,null),press.getBlock().asItem(),"Compact press");
        one(block(level,"golem_builder",press.setValue(GolemBuilderBlock.ASSEMBLED,true),pick,47,null),Items.PISTON,"Assembled press");
        saved.add(new ItemStack(press.getBlock()));saved.add(new ItemStack(Items.PISTON));
        Thaumcraft.LOGGER.info("LOOT_PRESS_OK: compact block and assembled piston are mutually exclusive");
        var base=new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(P));
        var noPlayer=base.withParameter(LootContextParams.THIS_ENTITY,actor).withParameter(LootContextParams.DAMAGE_SOURCE,level.damageSources().generic()).create(LootContextParamSets.ENTITY);
        var player=new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(P))
            .withParameter(LootContextParams.THIS_ENTITY,actor).withParameter(LootContextParams.DAMAGE_SOURCE,level.damageSources().generic())
            .withParameter(LootContextParams.LAST_DAMAGE_PLAYER,actor).create(LootContextParamSets.ENTITY);
        int curios=0,maxCount=0;ItemStack curio=ItemStack.EMPTY;
        for (int n=1;n<=2048;n++) {
            for (var stack:table(level,"entities/taint_seed_prime").getRandomItems(noPlayer,n*0x9E3779B97F4A7C15L)) {
                check("vitium".equals(stack.get(CrystalContent.ASPECT.get())) && stack.getCount()>=1 && stack.getCount()<=3,"Seed components/count modifier");
                maxCount=Math.max(maxCount,stack.getCount());
                if (stack.getCount()==3 && saved.size()==6) saved.add(stack.copy());
            }
            for (var stack:table(level,"entities/cultist_knight").getRandomItems(noPlayer,n*0x9E3779B97F4A7C15L))
                if (!stack.isEmpty()) check(stack.is(Items.GOLD_NUGGET) && stack.getCount()<=2,"Non-player output invalid: "+stack+" variant="+stack.get(CuriosityContent.VARIANT.get()));
            for (var stack:table(level,"entities/cultist_knight").getRandomItems(player,n*0x9E3779B97F4A7C15L))
                if (!stack.isEmpty() && !stack.is(Items.GOLD_NUGGET)) {
                    check("rites".equals(stack.get(CuriosityContent.VARIANT.get())),"Curiosity variant modifier lost");
                    curios++;curio=stack.copy();
                }
        }
        check(maxCount==3 && curios>5 && curios<65,"Entity chance/count: "+curios);
        saved.add(curio);saved.add(pick);saved.add(silk);saved.add(fortune);
        Thaumcraft.LOGGER.info("LOOT_ENTITIES_OK: variable 1..3 vitium drops, player-only rare rites curiosity ({} /2048), count and component modifiers applied",curios);
        return saved;
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post event) {
        if (!mode().startsWith("loot-") || done || ++ticks<80) return;
        var level=event.server().overworld();
        if (mode().equals("loot-client") && event.server().getPlayerList().getPlayers().isEmpty()) return;
        done=true;
        if (mode().equals("loot-read")) {
            level.getChunk(P);
            var chest=(ChestBlockEntity)level.getBlockEntity(P);
            check(chest!=null && chest.getItem(0).is(TaintContent.POROUS.get().asItem()),"Restart lost silk drop");
            check("vitium".equals(chest.getItem(1).get(CrystalContent.ASPECT.get())) && chest.getItem(6).getCount()==3,"Restart lost crystal components/count");
            check("rites".equals(chest.getItem(7).get(CuriosityContent.VARIANT.get())),"Restart lost curiosity variant");
            Thaumcraft.LOGGER.info("LOOT_RESTART_OK: native saved drops retained crystal/curiosity components and counts in second JVM");
            event.server().halt(false);return;
        }
        var actor=mode().equals("loot-client") ? event.server().getPlayerList().getPlayers().getFirst() :
            new ServerPlayer(event.server(),level,new GameProfile(UUID.fromString("75968476-5ac1-450d-bf7b-f15a9547dc07"),"LootProbe"),ClientInformation.createDefault());
        var drops=tests(level,actor);
        for (var pos:BlockPos.betweenClosed(P.offset(-6,-1,-4),P.offset(6,4,8)))
            level.setBlockAndUpdate(pos,pos.getY()==P.getY()-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(P,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.SOUTH));
        var chest=(ChestBlockEntity)level.getBlockEntity(P);
        for (int i=0;i<drops.size();i++) chest.setItem(i,drops.get(i));
        if (!mode().equals("loot-client")) { event.server().halt(false);return; }
        actor.setGameMode(net.minecraft.world.level.GameType.CREATIVE);actor.getInventory().clearContent();
        actor.teleportTo(level,P.getX()+.5,P.getY(),P.getZ()+3.5,Set.of(),180,26,true);
        level.getServer().clockManager().setTotalTicks(level.dimensionType().defaultClock().orElseThrow(),6000);
        ready=true;
        Thaumcraft.LOGGER.info("LOOT_CLIENT_READY: actual native loot samples stored in vanilla chest for real menu interaction");
    }
}
