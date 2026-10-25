package thaumcraft.infusion;
import com.mojang.serialization.*;
import java.util.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;
import thaumcraft.*;
import thaumcraft.content.*;
import thaumcraft.persistence.PlayerWarpStore;
import thaumcraft.research.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID)
public final class CuriosityContent{
    public static final List<String> VARIANTS=List.of("arcane","preserved","ancient","eldritch","knowledge","twisted","rites");
    public static final List<String> CATEGORIES=List.of("AUROMANCY","ALCHEMY","GOLEMANCY","ELDRITCH","INFUSION","ARTIFICE","ELDRITCH");
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,Thaumcraft.MODID);
    public static final RegistryObject<DataComponentType<String>> VARIANT=COMPONENTS.register("curio_variant",()->DataComponentType.<String>builder().persistent(Codec.STRING.validate(s->VARIANTS.contains(s)?DataResult.success(s):DataResult.error(()->"Unknown curiosity"))).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    public static final RegistryObject<Item> BRAIN=Registrar.ITEMS.register("brain",()->new Item(properties("brain").food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(4).saturationModifier(.2F).build(),net.minecraft.world.item.component.Consumables.defaultFood().onConsume(new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.HUNGER,600),.8F)).build())));
    public static final RegistryObject<Item> CURIO=Registrar.ITEMS.register("curio",()->new Item(properties("curio").rarity(Rarity.UNCOMMON).component(VARIANT.get(),"arcane").component(DataComponents.ITEM_NAME,Component.translatable("item.thaumcraft.curio.arcane"))){@Override public InteractionResult use(Level level,Player player,InteractionHand hand){if(level.isClientSide())return InteractionResult.SUCCESS;return study(player,player.getItemInHand(hand))?InteractionResult.SUCCESS:InteractionResult.FAIL;}});
    static{BasicContent.display(BRAIN);for(var v:VARIANTS)BasicContent.displayStack(()->curio(v));}
    private static Item.Properties properties(String id){return new Item.Properties().setId(ResourceKey.create(Registries.ITEM,SimpleContent.id(id)));}
    public static ItemStack curio(String variant){if(!VARIANTS.contains(variant))throw new IllegalArgumentException("Unknown curio");var s=new ItemStack(CURIO.get());s.set(VARIANT.get(),variant);s.set(DataComponents.ITEM_NAME,Component.translatable("item.thaumcraft.curio."+variant));return s;}
    public static String variant(ItemStack stack){return stack.getOrDefault(VARIANT.get(),"arcane");}
    public static boolean warp(ServerPlayer p,int permanent,int normal,int temporary){if(thaumcraft.config.ThaumcraftConfig.wussMode.get())return true;var old=PlayerWarpStore.read(p).result();if(old.isEmpty())return false;var w=old.get();return PlayerWarpStore.write(p,thaumcraft.warp.WarpRules.add(w,permanent,normal,temporary)).result().isPresent();}
    public static boolean study(Player p,ItemStack stack){if(!(p instanceof ServerPlayer real)||!stack.is(CURIO.get()))return false;var old=ResearchService.state(p);if(old==null)return false;String variant=variant(stack);int index=VARIANTS.indexOf(variant);if(index<0)return false;var savedWarp=PlayerWarpStore.read(real).result();if(savedWarp.isEmpty())return false;var w=savedWarp.get();if(variant.equals("rites")&&w.permanent()+w.normal()+w.temporary()/2<=20)return false;var r=p.getRandom();var next=old;
        String category=CATEGORIES.get(index);next=next.addPoints("OBSERVATION",category,8+r.nextInt(9)).addPoints("THEORY",category,10+r.nextInt(7));next=next.addPoints("OBSERVATION",ResearchCatalog.CATEGORIES.get(r.nextInt(7)),8+r.nextInt(9)).addPoints("THEORY",ResearchCatalog.CATEGORIES.get(r.nextInt(7)),10+r.nextInt(7));
        if(variant.equals("rites"))next=next.stage("CrimsonRites",ResearchCatalog.BY_KEY.get("CrimsonRites").stages().size()+1);if(!KnowledgeStore.write(p,next))return false;if((variant.equals("eldritch")||variant.equals("rites"))&&!warp(real,variant.equals("rites")&&r.nextBoolean()?1:0,1,5)){KnowledgeStore.write(p,old);return false;}
        if(!p.getAbilities().instabuild)stack.shrink(1);p.sendSystemMessage(Component.translatable("infusion.thaumcraft.knowledge"));return true;
    }
    @SubscribeEvent public static void drops(LivingDropsEvent e){if(!(e.getEntity().level() instanceof net.minecraft.server.level.ServerLevel server))return;
        var dead=e.getEntity();
        if(dead instanceof Zombie zombie&&!(zombie instanceof thaumcraft.eldritch.BrainyZombie brainy&&brainy.kind!=thaumcraft.eldritch.EldritchMob.Kind.INHABITED)&&e.isRecentlyHit()){
            int looting=looting(server,e.getSource().getEntity());
            if(thaumcraft.warp.CultistRules.dropsBrain(zombie.getRandom().nextInt(10),looting))
                e.getDrops().add(new ItemEntity(server,dead.getX(),dead.getEyeY(),dead.getZ(),new ItemStack(BRAIN.get())));
        }
        if(dead instanceof thaumcraft.warp.WarpMonster cultist&&(cultist.kind==thaumcraft.warp.WarpMonster.Kind.KNIGHT||cultist.kind==thaumcraft.warp.WarpMonster.Kind.CLERIC)
                &&e.getSource().getEntity() instanceof ServerPlayer p&&!p.getClass().getName().contains("FakePlayer")){
            boolean knows=ResearchService.knows(p,"!CrimsonCultist@2");
            boolean carrying=p.getInventory().getNonEquipmentItems().stream().anyMatch(s->s.is(CURIO.get())&&"rites".equals(variant(s)));
            int denominator=thaumcraft.warp.CultistRules.curioDenominator(knows,carrying);
            if(thaumcraft.warp.CultistRules.dropsCurio(cultist.getRandom().nextInt(denominator),denominator))
                e.getDrops().add(new ItemEntity(server,dead.getX(),dead.getEyeY(),dead.getZ(),curio("rites")));
        }
    }
    private static int looting(net.minecraft.server.level.ServerLevel server,net.minecraft.world.entity.Entity killer){
        if(!(killer instanceof net.minecraft.world.entity.LivingEntity living))return 0;
        var enchantment=server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(net.minecraft.world.item.enchantment.Enchantments.LOOTING);
        return enchantment.isPresent()?net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentLevel(enchantment.get(),living):0;
    }
    @SubscribeEvent public static void eaten(net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Finish e){if(e.getEntity() instanceof ServerPlayer p&&e.getItem().is(BRAIN.get())&&p.getRandom().nextFloat()<.8F)warp(p,0,0,1+p.getRandom().nextInt(3));}
    public static void register(BusGroup bus){COMPONENTS.register(bus);}
}
