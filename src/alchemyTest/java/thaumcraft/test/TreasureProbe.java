package thaumcraft.test;

import java.util.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.content.SimpleContent;
import thaumcraft.rifts.PrimordialPearl;
import thaumcraft.treasure.*;

@Mod.EventBusSubscriber(modid="thaumcraft")
public final class TreasureProbe {
    public static final BlockPos P=new BlockPos(3968,205,8);
    public static volatile boolean ready;
    static int ticks,cancelAt,joins;
    static long due;
    static boolean done;
    static final List<ItemEntity> emitted=new ArrayList<>();
    static String mode() { return System.getProperty("thaumcraft.alchemyProbe",""); }
    static void check(boolean b,String message) { if (!b) throw new AssertionError(message); }
    @SubscribeEvent public static boolean join(EntityJoinLevelEvent event) {
        if (!mode().startsWith("treasure-") || !(event.getEntity() instanceof ItemEntity item)) return false;
        emitted.add(item);
        return cancelAt>0 && ++joins==cancelAt;
    }
    static LootTable table(ServerLevel level,String path) {
        return level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,SimpleContent.id(path)));
    }
    static List<ItemStack> roll(ServerLevel level,String path,long seed) {
        var params=new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(P)).create(LootContextParamSets.CHEST);
        return table(level,"treasure/"+path).getRandomItems(params,seed*0x9E3779B97F4A7C15L);
    }
    static List<ItemEntity> entities(ServerLevel level) {
        // Chunk spatial indexes can lag insertion; the native registration flag is immediate.
        return emitted.stream().filter(entity->entity.level()==level && entity.isAddedToLevel() && !entity.isRemoved()).toList();
    }
    static void clean(ServerLevel level) { entities(level).forEach(ItemEntity::discard);emitted.clear(); }
    static List<ItemStack> rewards(ServerLevel level) {
        ItemStack potion=ItemStack.EMPTY,pearl=ItemStack.EMPTY,book=ItemStack.EMPTY,gearSample=ItemStack.EMPTY;
        int books=0,potions=0,pearls=0;
        for (int tier=0;tier<3;tier++) {
            String label=TreasureContent.TIERS[tier];
            for (int seed=1;seed<=4096;seed++) {
                var drops=roll(level,"base_"+label,seed);
                check(drops.size()==1 && !drops.getFirst().isEmpty(),"Base reward missing");
                var stack=drops.getFirst();
                if (stack.is(Items.GOLD_NUGGET)) check(stack.getCount()==tier+1,"Nugget amount for "+label);
                else check(stack.getCount()==1,"Unexpected weighted quantity");
                if (stack.has(DataComponents.POTION_CONTENTS)) { potions++;potion=stack.copy(); }
                if (stack.getItem() instanceof PrimordialPearl) {
                    int wear=PrimordialPearl.wear(stack);
                    check(tier==0?wear==7:tier==1?wear==6||wear==7:wear==0||wear==3||wear==5,"Pearl wear by rarity");
                    pearls++;if (wear>0) pearl=stack.copy();
                }
                if (stack.is(Items.ENCHANTED_BOOK)) {
                    check(!stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS,net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY).isEmpty(),"Empty enchanted book");
                    books++;book=stack.copy();
                }
            }
            Set<Integer> counts=new HashSet<>();
            for (int seed=1;seed<=128;seed++) {
                int count=roll(level,"bag_"+label,seed).size();check(count>=8 && count<=12,"Bag roll count");counts.add(count);
            }
            check(counts.size()==5,"Bag does not span 8..12 rewards");
        }
        for (String label:List.of("uncommon","rare")) {
            int enchanted=0;
            for (int seed=1;seed<=2048;seed++) {
                var stack=roll(level,"gear_"+label,seed).getFirst();
                check(stack.isDamageableItem() && stack.getDamageValue()>=0 && stack.getDamageValue()<=stack.getMaxDamage()/6,"Equipment wear");
                if (stack.isEnchanted()) enchanted++;
                if (stack.getDamageValue()>0 && stack.isEnchanted()) gearSample=stack.copy();
            }
            check(label.equals("uncommon") ? enchanted>250 && enchanted<800 : enchanted>700 && enchanted<1400,"Gear enchantment chance "+enchanted);
        }
        check(books>0 && potions>0 && pearls>0,"Special rewards never generated");
        Thaumcraft.LOGGER.info("TREASURE_POOLS_OK: original weights/counts, 8..12 rolls, potion components, pearl wear, enchanted books and native equipment; samples books={}, potions={}, pearls={}",books,potions,pearls);
        return List.of(potion,pearl,book,gearSample);
    }
    static void blocks(ServerLevel level) {
        var pick=new ItemStack(Items.DIAMOND_PICKAXE);var silk=pick.copy();
        silk.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH),1);
        for (int tier=0;tier<3;tier++) for (String kind:List.of("crate","urn")) {
            String name="loot_"+kind+"_"+TreasureContent.TIERS[tier];var block=TreasureContent.BLOCKS.get(name).get();
            for (int seed=1;seed<=128;seed++) {
                for (var tool:List.of(pick,silk)) {
                    var params=new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(P))
                        .withParameter(LootContextParams.BLOCK_STATE,block.defaultBlockState()).withParameter(LootContextParams.TOOL,tool).create(LootContextParamSets.BLOCK);
                    var drops=table(level,"blocks/"+name).getRandomItems(params,seed*0x9E3779B97F4A7C15L);
                    if (tool==silk) check(drops.size()==1 && drops.getFirst().is(block.asItem()) && drops.getFirst().getCount()==1,"Silk rerolled container");
                    else check(drops.size()>=tier+1 && drops.size()<=tier+3 && drops.stream().noneMatch(s->s.is(block.asItem())),"Container reward count");
                }
            }
        }
        Thaumcraft.LOGGER.info("TREASURE_BLOCKS_OK: six containers, rarity-sized rewards and exact Silk Touch unopened blocks");
    }
    static void bags(ServerLevel level,ServerPlayer actor) {
        actor.setPos(P.getX()+.5,P.getY(),P.getZ()+.5);clean(level);
        for (String label:TreasureContent.TIERS) {
            var bag=SimpleContent.ITEMS.get("loot_bag_"+label).get();var stack=new ItemStack(bag,2);
            check(stack.getMaxStackSize()==16,"Bag stack limit");actor.setItemInHand(InteractionHand.OFF_HAND,stack);
            cancelAt=2;joins=0;bag.use(level,actor,InteractionHand.OFF_HAND);cancelAt=0;
            check(joins==2 && stack.getCount()==2 && entities(level).isEmpty(),"Cancelled opening lost bag or leaked rewards");
            bag.use(level,actor,InteractionHand.OFF_HAND);
            check(stack.getCount()==1 && entities(level).size()>=8 && entities(level).size()<=12,"Offhand bag payment/output: count="+stack.getCount()+", entities="+entities(level).size()+", join events="+emitted.size());clean(level);
            actor.getAbilities().instabuild=true;bag.use(level,actor,InteractionHand.OFF_HAND);
            check(stack.isEmpty() && entities(level).size()>=8 && entities(level).size()<=12,"Creative opening did not consume original bag");
            int before=entities(level).size();bag.use(level,actor,InteractionHand.OFF_HAND);
            check(entities(level).size()==before,"Empty bag replay generated loot");clean(level);actor.getAbilities().instabuild=false;
        }
        actor.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        Thaumcraft.LOGGER.info("TREASURE_BAGS_OK: three tiers, offhand, one bag per opening including creative, rollback on second spawn cancellation, no empty replay");
    }
    static void verifySaved(ServerLevel level) {
        level.getChunk(P);
        var chest=(ChestBlockEntity)level.getBlockEntity(P.south(3));
        check(chest!=null && chest.getItem(0).getOrDefault(DataComponents.POTION_CONTENTS,net.minecraft.world.item.alchemy.PotionContents.EMPTY).potion().isPresent(),"Lost saved potion");
        check(chest.getItem(1).getItem() instanceof PrimordialPearl && chest.getItem(1).has(DataComponents.CUSTOM_MODEL_DATA) && PrimordialPearl.wear(chest.getItem(1))>0,"Lost pearl wear");
        check(chest.getItem(2).is(Items.ENCHANTED_BOOK) && !chest.getItem(2).getOrDefault(DataComponents.STORED_ENCHANTMENTS,net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY).isEmpty() && chest.getItem(3).getDamageValue()>0 && chest.getItem(3).isEnchanted(),"Lost book/equipment components");
        check(chest.getItem(4).is(SimpleContent.ITEMS.get("loot_bag_rare").get()) && chest.getItem(4).getCount()==3,"Lost unopened bags");
        Thaumcraft.LOGGER.info("TREASURE_RESTART_OK: native potion, pearl wear, enchanted book/equipment and unopened bags survived second JVM");
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post event) {
        if (!mode().startsWith("treasure-") || done || ++ticks<80) return;
        var level=event.server().overworld();
        if (mode().equals("treasure-client") && event.server().getPlayerList().getPlayers().isEmpty()) return;
        if (due==0) {
            for (int x=247;x<=249;x++) for (int z=-1;z<=1;z++) level.setChunkForced(x,z,true);
            due=level.getGameTime()+40;return;
        }
        if (level.getGameTime()<due) return;
        done=true;
        if (mode().equals("treasure-read")) { verifySaved(level);event.server().halt(false);return; }
        var actor=mode().equals("treasure-client") ? event.server().getPlayerList().getPlayers().getFirst() :
            new ServerPlayer(event.server(),level,new GameProfile(UUID.fromString("39cb47bb-cc47-486b-84df-c04a64956d49"),"TreasureProbe"),ClientInformation.createDefault());
        var samples=rewards(level);blocks(level);bags(level,actor);
        for (var pos:BlockPos.betweenClosed(P.offset(-6,-1,-4),P.offset(6,4,11)))
            level.setBlockAndUpdate(pos,pos.getY()==P.getY()-1 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(P.south(3),Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.SOUTH));
        var chest=(ChestBlockEntity)level.getBlockEntity(P.south(3));
        for (int i=0;i<samples.size();i++) chest.setItem(i,samples.get(i));
        chest.setItem(4,new ItemStack(SimpleContent.ITEMS.get("loot_bag_rare").get(),3));
        for (int tier=0;tier<3;tier++) for (String kind:List.of("crate","urn"))
            level.setBlockAndUpdate(P.offset(tier*2-2,0,kind.equals("urn")?-2:0),TreasureContent.BLOCKS.get("loot_"+kind+"_"+TreasureContent.TIERS[tier]).get().defaultBlockState());
        if (!mode().equals("treasure-client")) { event.server().halt(false);return; }
        actor.setGameMode(net.minecraft.world.level.GameType.CREATIVE);actor.getInventory().clearContent();
        actor.getInventory().setItem(0,new ItemStack(SimpleContent.ITEMS.get("loot_bag_rare").get(),2));actor.getInventory().setSelectedSlot(0);
        actor.teleportTo(level,P.getX()+.5,P.getY()+2,P.getZ()+9,Set.of(),180,24,true);
        actor.getAbilities().flying=true;actor.onUpdateAbilities();
        level.getServer().clockManager().setTotalTicks(level.dimensionType().defaultClock().orElseThrow(),6000);
        ready=true;Thaumcraft.LOGGER.info("TREASURE_CLIENT_READY: six original containers, saved native rewards and two rare bags for real use input");
    }
}
