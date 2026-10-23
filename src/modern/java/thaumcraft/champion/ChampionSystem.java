package thaumcraft.champion;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import thaumcraft.Thaumcraft;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.content.SimpleContent;
import thaumcraft.persistence.PlayerWarpStore;
import thaumcraft.taint.TaintContent;
import thaumcraft.taint.TaintedMob;
import thaumcraft.warp.WarpRules;

/**
 * M16.1 champion mobs and their treasure-bag source.
 *
 * <p>The table mirrors TC6's {@code ChampionModifier}: type ids are the
 * original ids, so persisted champion values stay meaningful.</p>
 */
public final class ChampionSystem {
    private ChampionSystem() {}

    /** Original modifier ids, in the exact TC6 order. */
    private static final String[] NAMES = {
            "bold", "spine", "armor", "mighty", "grim", "warded", "warp",
            "undying", "fiery", "sickly", "venomous", "vampiric", "infested", "tainted"
    };

    private static final Identifier HEALTH = SimpleContent.id("champion_health");
    private static final Identifier DAMAGE = SimpleContent.id("champion_damage");
    private static final Identifier BOLD_SPEED = SimpleContent.id("champion_bold_speed");
    private static final Identifier MIGHTY_DAMAGE = SimpleContent.id("champion_mighty_damage");

    private static final Identifier NONE_MOD = SimpleContent.id("champion_none");
    private static final Identifier TYPE_MOD = SimpleContent.id("champion_type");

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, Thaumcraft.MODID);

    /** Synced, ranged like the original {@code tc.mobmod}: -2 unprocessed, -1 normal, 0..13 champion. */
    public static final RegistryObject<Attribute> CHAMPION_MOD = ATTRIBUTES.register("champion_mod",
            () -> new RangedAttribute("attribute.name.thaumcraft.champion_mod", -2.0D, -2.0D, 100.0D).setSyncable(true));

    public static void register(BusGroup modBus) {
        ATTRIBUTES.register(modBus);
        EntityAttributeModificationEvent.BUS.addListener(ChampionSystem::addToMobs);
        EntityJoinLevelEvent.BUS.addListener(ChampionSystem::onJoin);
        LivingDamageEvent.BUS.addListener(ChampionSystem::onDamage);
        LivingEvent.LivingTickEvent.BUS.addListener(ChampionSystem::onLivingTick);
        LivingDropsEvent.BUS.addListener(ChampionSystem::onDrops);
    }

    private static Holder<Attribute> attribute() {
        return CHAMPION_MOD.getHolder().orElseThrow();
    }

    private static void addToMobs(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            if (Mob.class.isAssignableFrom(type.getBaseClass())) {
                event.add(type, attribute());
            }
        }
    }

    /** Whitelist values from TC6's ConfigEntities: 0, 1, 2 and 3 with bonus 0, 0, 1 and 2. */
    private static int whitelistValue(Mob mob) {
        if (mob instanceof Zombie || mob instanceof Spider || mob instanceof Blaze
                || mob instanceof Enderman || mob instanceof Skeleton || mob instanceof Witch) {
            return 0;
        }
        if (mob instanceof TaintedMob tainted && tainted.kind == TaintedMob.Kind.TENTACLE) {
            return 2;
        }
        return -1;
    }

    private static boolean isWhitelisted(Mob mob) {
        return whitelistValue(mob) >= 0;
    }

    private static double championValue(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(attribute());
        return instance == null ? -2.0D : instance.getValue();
    }

    private static boolean isChampion(LivingEntity entity) {
        return championValue(entity) >= 0.0D;
    }

    /** -1 when not a champion, otherwise 0..13 (13 is the tainted modifier). */
    private static int championType(LivingEntity entity) {
        double value = championValue(entity);
        if (value < 0.0D || value > 13.0D) return -1;
        return (int) Math.round(value);
    }

    private static void markNormal(Mob mob) {
        AttributeInstance instance = mob.getAttribute(attribute());
        if (instance == null || instance.getValue() > -2.0D) return;
        instance.addPermanentModifier(new AttributeModifier(NONE_MOD, 1.0D, AttributeModifier.Operation.ADD_VALUE));
    }

    private static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (mob instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) return;
        if (mob instanceof net.minecraft.world.entity.boss.wither.WitherBoss) return;
        if (championValue(mob) > -2.0D) return;

        // TC6: 1% base, difficulty and whitelist shift the roll; allowChampionMobs only shifts it further.
        boolean allowed = ThaumcraftConfig.allowChampionMobs.get();
        boolean easy = level.getDifficulty() == net.minecraft.world.Difficulty.EASY;
        boolean hard = level.getDifficulty() == net.minecraft.world.Difficulty.HARD;
        boolean netherOrEnd = level.getBiome(mob.blockPosition()).is(net.minecraft.tags.BiomeTags.IS_NETHER)
                || level.getBiome(mob.blockPosition()).is(net.minecraft.tags.BiomeTags.IS_END);
        int roll = ChampionRules.adjustSpawnRoll(mob.getRandom().nextInt(100), easy, hard, allowed, netherOrEnd);
        int bonus = allowed ? ChampionRules.whitelistBonus(whitelistValue(mob)) : 0;

        if (isWhitelisted(mob) && ChampionRules.spawnsChampion(roll, bonus)
                && mob.getAttributeBaseValue(Attributes.MAX_HEALTH) >= 10.0D) {
            int type = mob instanceof Creeper ? 0 : mob.getRandom().nextInt(NAMES.length);
            makeChampion(mob, type, false);
        } else {
            markNormal(mob);
        }
    }

    /** Port of {@code EntityUtils.makeChampion}. */
    private static void makeChampion(Mob mob, int type, boolean persist) {
        AttributeInstance instance = mob.getAttribute(attribute());
        if (instance == null || instance.getValue() > -2.0D) return;

        instance.removeModifier(TYPE_MOD);
        instance.addPermanentModifier(new AttributeModifier(TYPE_MOD, type + 2.0D, AttributeModifier.Operation.ADD_VALUE));

        AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(HEALTH);
            health.addPermanentModifier(new AttributeModifier(HEALTH, 100.0D, AttributeModifier.Operation.ADD_VALUE));
        }
        AttributeInstance damage = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.removeModifier(DAMAGE);
            damage.addPermanentModifier(new AttributeModifier(DAMAGE, 2.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        mob.heal(25.0F);

        if (type == 0) {
            AttributeInstance speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                speed.removeModifier(BOLD_SPEED);
                speed.addPermanentModifier(new AttributeModifier(BOLD_SPEED, 0.3D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        } else if (type == 3) {
            if (damage != null) {
                damage.removeModifier(MIGHTY_DAMAGE);
                damage.addPermanentModifier(new AttributeModifier(MIGHTY_DAMAGE, 2.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        } else if (type == 5) {
            mob.setAbsorptionAmount(mob.getAbsorptionAmount() + (float) ((int) mob.getAttributeBaseValue(Attributes.MAX_HEALTH) / 2));
        }

        Component base = mob.hasCustomName() && mob.getCustomName() != null
                ? mob.getCustomName() : mob.getType().getDescription();
        mob.setCustomName(Component.empty()
                .append(Component.translatable("champion.mod." + NAMES[type]))
                .append(" ")
                .append(base));
        mob.setCustomNameVisible(true);
        if (persist) mob.setPersistenceRequired();
    }

    private static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!(mob.level() instanceof ServerLevel)) return;
        int type = championType(mob);
        if (type == 5) {
            // Warded: one absorption point every 25 ticks, up to half its base health.
            if (mob.hurtTime <= 0 && mob.tickCount % 25 == 0) {
                int limit = (int) mob.getAttributeBaseValue(Attributes.MAX_HEALTH) / 2;
                if (mob.getAbsorptionAmount() < limit) {
                    mob.setAbsorptionAmount(mob.getAbsorptionAmount() + 1.0F);
                }
            }
        } else if (type == 7) {
            // Undying: heal one point every second.
            if (mob.tickCount % 20 == 0) mob.heal(1.0F);
        }
    }

    private static void onDamage(LivingDamageEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        if (event.getAmount() <= 0.0F) return;
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        LivingEntity attacker = source.getEntity() instanceof LivingEntity living ? living : null;

        // Defensive modifiers (type 2) fire when the champion is hurt by a living attacker.
        if (attacker != null && attacker != target) {
            int type = championType(target);
            if (type == 1 && !source.is(DamageTypes.THORNS)) {
                // Spined: 1-3 thorns damage.
                attacker.hurtServer(level, level.damageSources().thorns(target), 1 + target.getRandom().nextInt(3));
                target.playSound(SoundEvents.THORNS_HIT, 0.5F, 1.0F);
            } else if (type == 2 && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) {
                // Armored: 19/25 of the original damage.
                event.setAmount(event.getAmount() * 19.0F / 25.0F);
            } else if (type == 12 && target.getRandom().nextFloat() < 0.4F) {
                // Infested: spawn a taint crawler at the champion's centre.
                TaintedMob crawler = new TaintedMob(TaintContent.MOBS.get(TaintedMob.Kind.CRAWLER).get(), level, TaintedMob.Kind.CRAWLER);
                crawler.snapTo(target.getX(), target.getY() + target.getBbHeight() / 2.0F, target.getZ(),
                        target.getRandom().nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(crawler);
                target.playSound(SoundEvents.SLIME_ATTACK, 0.5F, 1.0F);
            }
        }

        if (attacker == null) return;
        int type = championType(attacker);

        // Offensive modifiers (type 1) fire when the champion deals damage.
        if (type == 4 && attacker.getRandom().nextFloat() < 0.4F) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200));
        } else if (type == 6 && attacker.getRandom().nextFloat() < 0.33F && target instanceof ServerPlayer player) {
            PlayerWarpStore.read(player).result().ifPresent(state ->
                    PlayerWarpStore.write(player, WarpRules.add(state, 0, 0, 1 + attacker.getRandom().nextInt(3))));
        } else if (type == 8 && attacker.getRandom().nextFloat() < 0.4F) {
            target.igniteForSeconds(4.0F);
        } else if (type == 9 && attacker.getRandom().nextFloat() < 0.4F) {
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 500));
        } else if (type == 10 && attacker.getRandom().nextFloat() < 0.4F) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
        } else if (type == 11) {
            attacker.heal(Math.max(2.0F, event.getAmount() / 2.0F));
        }
    }

    /** Port of TC6's LivingDropsEvent champion branch: one bag, 5-7 XP. */
    private static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        if (!event.isRecentlyHit()) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (mob instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) return;
        if (mob instanceof net.minecraft.world.entity.boss.wither.WitherBoss) return;
        if (!isChampion(mob)) return;

        int type = championType(mob);
        if (type < 0 || type == 13) return;

        Entity killer = event.getSource().getEntity();
        if (killer != null && killer.getClass().getName().contains("FakePlayer")) return;

        int looting = 0;
        if (killer instanceof LivingEntity living) {
            var enchantment = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.LOOTING);
            if (enchantment.isPresent()) {
                looting = EnchantmentHelper.getEnchantmentLevel(enchantment.get(), living);
            }
        }
        int tier = ChampionRules.bagTier(level.getRandom().nextInt(9), looting);
        String bag = switch (tier) {
            case 1 -> "loot_bag_uncommon";
            case 2 -> "loot_bag_rare";
            default -> "loot_bag_common";
        };
        event.getDrops().add(new ItemEntity(level, mob.getX(), mob.getY() + mob.getEyeHeight(), mob.getZ(),
                new ItemStack(SimpleContent.ITEMS.get(bag).get())));

        int xp = 5 + level.getRandom().nextInt(3);
        ExperienceOrb.award(level, new Vec3(mob.getX(), mob.getY(), mob.getZ()), xp);
    }
}
