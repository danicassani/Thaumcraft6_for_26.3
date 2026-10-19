package thaumcraft.thaumaturgy;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.*;
import net.minecraftforge.registries.*;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import thaumcraft.Thaumcraft;
import thaumcraft.content.*;
/** Original additional ore drop; preserves every existing loot pool and Silk Touch. */
public final class RareEarthLoot extends LootModifier {
    public static final MapCodec<RareEarthLoot> CODEC=RecordCodecBuilder.mapCodec(i->codecStart(i).apply(i,RareEarthLoot::new));
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> TYPES=DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,Thaumcraft.MODID);
    public static void register(BusGroup bus){TYPES.register("rare_earth",()->CODEC);TYPES.register(bus);}
    public RareEarthLoot(LootItemCondition[] c){super(c);}
    public MapCodec<? extends IGlobalLootModifier> codec(){return CODEC;}
    protected ObjectArrayList<ItemStack> doApply(LootTable table,ObjectArrayList<ItemStack> loot,LootContext context){var state=context.getOptional(LootContextParams.BLOCK_STATE);if(state==null)return loot;var tool=context.getOptional(LootContextParams.TOOL);var silk=context.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);if(tool!=null&&tool.getOrDefault(DataComponents.ENCHANTMENTS,ItemEnchantments.EMPTY).getLevel(silk)>0)return loot;
        double chance=state.is(Blocks.DIAMOND_ORE)||state.is(Blocks.DEEPSLATE_DIAMOND_ORE)||state.is(BasicContent.ORE_AMBER.get())?.05:state.is(Blocks.EMERALD_ORE)||state.is(Blocks.DEEPSLATE_EMERALD_ORE)?.075:state.is(Blocks.COAL_ORE)||state.is(Blocks.DEEPSLATE_COAL_ORE)?.001:state.is(Blocks.LAPIS_ORE)||state.is(Blocks.DEEPSLATE_LAPIS_ORE)||state.is(Blocks.REDSTONE_ORE)||state.is(Blocks.DEEPSLATE_REDSTONE_ORE)||state.is(Blocks.NETHER_QUARTZ_ORE)?.01:0;
        if(chance>0&&context.getRandom().nextDouble()<chance)loot.add(new ItemStack(SimpleContent.ITEMS.get("nugget_rareearth").get()));return loot;
    }
}
