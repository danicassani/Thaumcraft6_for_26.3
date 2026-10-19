package thaumcraft.equipment;
import java.util.*;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.*;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.*;
import net.minecraft.world.effect.*;
import net.minecraftforge.registries.*;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import thaumcraft.*;
import thaumcraft.content.SimpleContent;
import thaumcraft.api.items.*;
import thaumcraft.aura.AuraManager;
import thaumcraft.infusion.InfusionEnchantments;
public final class EquipmentContent {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,Thaumcraft.MODID);
    public static final RegistryObject<DataComponentType<Integer>> RUNIC=COMPONENTS.register("runic_shield",()->DataComponentType.<Integer>builder().persistent(Codec.intRange(0,9)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    private static final Map<String,Integer> DISCOUNTS=new HashMap<>();
    private static final Set<String> REPAIR=new HashSet<>();
    private static ArmorMaterial material(String name,int durability,int boots,int legs,int chest,int head,int enchant,float toughness,String repair){return new ArmorMaterial(durability,Map.of(ArmorType.BOOTS,boots,ArmorType.LEGGINGS,legs,ArmorType.CHESTPLATE,chest,ArmorType.HELMET,head),enchant,SoundEvents.ARMOR_EQUIP_IRON,toughness,0,TagKey.create(Registries.ITEM,SimpleContent.id(repair)),ResourceKey.create(EquipmentAssets.ROOT_ID,SimpleContent.id(name)));}
    private static void armor(String name,ArmorMaterial material,ArmorType type,int discount,boolean repair){SimpleContent.item(name,p->new Item(p.humanoidArmor(material,type).rarity(Rarity.UNCOMMON)));if(discount>0)DISCOUNTS.put(name,discount);if(repair)REPAIR.add(name);}
    private static final EquipmentSlot[] ARMOR={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
    public static String name(ItemStack s){var id=BuiltInRegistries.ITEM.getKey(s.getItem());return id.getNamespace().equals("thaumcraft")?id.getPath():"";}
    public static float visCost(Player p,float base){int discount=0;for(var slot:ARMOR)discount+=DISCOUNTS.getOrDefault(name(p.getItemBySlot(slot)),0);if(name(p.getOffhandItem()).equals("charm_voidseer")&&p instanceof ServerPlayer real){var warp=thaumcraft.persistence.PlayerWarpStore.read(real).result();if(warp.isPresent())discount+=Math.min(25,warp.get().permanent()/4);}
        return base*(1-Math.min(80,discount)/100F);}
    public static int runic(ItemStack s){return s.getOrDefault(RUNIC.get(),0);}
    public static boolean canAugment(ItemStack s){return s.getCount()==1&&(s.has(net.minecraft.core.component.DataComponents.EQUIPPABLE)||s.getItem() instanceof AccessoryItem)&&runic(s)<9;}
    public static ItemStack augment(ItemStack s){if(!canAugment(s))return ItemStack.EMPTY;var out=s.copy();out.set(RUNIC.get(),runic(s)+1);return out;}
    public static int maxShield(Player p){int max=0;for(var slot:ARMOR)max+=runic(p.getItemBySlot(slot));if(p.getOffhandItem().getItem() instanceof AccessoryItem)max+=runic(p.getOffhandItem());return max;}
    public static float shield(Player p){return Math.clamp(p.getPersistentData().getFloatOr("tc_runic_charge",0),0,maxShield(p));}
    public static void register(BusGroup bus){
        var cloth=material("cloth",25,1,2,3,1,25,1,"repairs_cloth");var goggles=material("goggles",25,1,2,3,1,25,1,"repairs_brass");var fortress=material("fortress",40,3,6,7,3,25,3,"repairs_thaumium");var voidArmor=material("void",10,3,6,8,3,10,1,"repairs_void");var voidRobe=material("void_robe",18,4,7,9,4,10,2,"repairs_void");
        SimpleContent.item("goggles",p->new Item(p.humanoidArmor(goggles,ArmorType.HELMET).durability(350)));DISCOUNTS.put("goggles",5);
        for(var entry:Map.of("helm",ArmorType.HELMET,"chest",ArmorType.CHESTPLATE,"legs",ArmorType.LEGGINGS,"boots",ArmorType.BOOTS).entrySet()){
            String suffix=entry.getKey();var type=entry.getValue();armor("void_"+suffix,voidArmor,type,0,true);
            if(type!=ArmorType.BOOTS){armor("fortress_"+suffix,fortress,type,0,false);armor("void_robe_"+suffix,voidRobe,type,5,true);}
            if(type!=ArmorType.HELMET)armor("cloth_"+suffix,cloth,type,type==ArmorType.BOOTS?2:3,false);
        }
        SimpleContent.item("traveller_boots",p->new TravellerBoots(p.humanoidArmor(material("traveller",25,1,2,3,1,25,1,"repairs_leather"),ArmorType.BOOTS)));
        var repair=TagKey.create(Registries.ITEM,SimpleContent.id("repairs_void"));var voidTool=new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,150,8,3,10,repair);var elemental=new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL,1500,9,3,18,TagKey.create(Registries.ITEM,SimpleContent.id("repairs_thaumium")));var primal=new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,500,8,4,20,repair);
        for(String family:List.of("void","elemental"))for(String type:List.of("sword","pick","axe","shovel","hoe")){String name=family+"_"+type;SimpleContent.item(name,p->{var m=family.equals("void")?voidTool:elemental;var properties=switch(type){case "sword"->p.sword(m,3,-2.4F);case "pick"->p.pickaxe(m,1,-2.8F);case "axe"->p.axe(m,5,-3);case "shovel"->p.shovel(m,1.5F,-3);default->p.hoe(m,-2,-1);};if(family.equals("elemental")){var values=switch(type){case "sword"->Map.of("arcing",2);case "pick"->Map.of("refining",1,"sounding",2);case "axe"->Map.of("burrowing",1,"collector",1);case "shovel"->Map.of("destructive",1);default->Map.<String,Integer>of();};properties.component(InfusionEnchantments.ENCHANTMENTS.get(),values);}return family.equals("elemental")&&type.equals("hoe")?new ElementalHoeItem(properties):new Item(properties);});if(family.equals("void"))REPAIR.add(name);}
        SimpleContent.item("primal_crusher",p->new Item(primal.applyToolProperties(p,TagKey.create(Registries.BLOCK,SimpleContent.id("mineable/primal_crusher")),3.5F,-2.8F,0).component(InfusionEnchantments.ENCHANTMENTS.get(),Map.of("destructive",1,"refining",1))));REPAIR.add("primal_crusher");
        for(String accessory:List.of("amulet_vis","charm_verdant","charm_verdant_life","charm_verdant_sustain","charm_voidseer","band_curiosity","ring_cloud"))SimpleContent.item(accessory,AccessoryItem::new);
        SimpleContent.item("charm_undying",p->new AccessoryItem(p.component(net.minecraft.core.component.DataComponents.DEATH_PROTECTION,net.minecraft.world.item.component.DeathProtection.TOTEM_OF_UNDYING)));
        SimpleContent.item("sanity_checker",p->new Item(p.stacksTo(1)){public net.minecraft.world.InteractionResult use(net.minecraft.world.level.Level l,Player player,net.minecraft.world.InteractionHand h){if(player instanceof ServerPlayer real){var state=thaumcraft.persistence.PlayerWarpStore.read(real).result();if(state.isPresent()){var w=state.get();real.sendSystemMessage(Component.translatable("equipment.thaumcraft.warp",w.permanent(),w.normal(),w.temporary()));}}return net.minecraft.world.InteractionResult.SUCCESS;}});
        COMPONENTS.register(bus);CloudJump.register();
        net.minecraftforge.event.entity.player.PlayerXpEvent.XpChange.BUS.addListener(e->{if(e.getEntity() instanceof ServerPlayer p&&name(p.getOffhandItem()).equals("band_curiosity")&&e.getAmount()>0&&p.getRandom().nextInt(3)==0){int points=Math.min(16,Math.max(1,e.getAmount()/2));if(thaumcraft.research.ResearchService.state(p)!=null&&thaumcraft.research.KnowledgeStore.write(p,thaumcraft.research.ResearchService.state(p).addPoints("OBSERVATION","BASICS",points)))e.setAmount(e.getAmount()-Math.min(e.getAmount(),points));}});

        TickEvent.PlayerTickEvent.Post.BUS.addListener(e->{if(!(e.player() instanceof ServerPlayer p))return;if(p.tickCount%20==0){for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(REPAIR.contains(name(s))&&s.isDamaged())s.setDamageValue(s.getDamageValue()-1);}
            var off=p.getOffhandItem();String accessory=name(off);
            if(accessory.equals("amulet_vis")&&p.tickCount%40==0)for(int i=0;i<p.getInventory().getContainerSize();i++){var stack=p.getInventory().getItem(i);if(RechargeHelper.rechargeItem(p.level(),stack,p.blockPosition(),p,1)>0)break;}
            if(accessory.startsWith("charm_verdant")&&p.tickCount%40==0){if((p.hasEffect(MobEffects.POISON)||p.hasEffect(MobEffects.WITHER))&&RechargeHelper.consumeCharge(off,p,20)){p.removeEffect(MobEffects.POISON);p.removeEffect(MobEffects.WITHER);}else if(accessory.endsWith("life")&&p.getHealth()<p.getMaxHealth()&&RechargeHelper.consumeCharge(off,p,2))p.heal(1);else if(accessory.endsWith("sustain")&&p.getFoodData().getFoodLevel()<20&&RechargeHelper.consumeCharge(off,p,2))p.getFoodData().eat(1,.2F);}
            var data=p.getPersistentData();float charge=shield(p);if(p.isAlive()&&charge<maxShield(p)&&p.level().getGameTime()>=data.getLongOr("tc_runic_next",0)&&AuraManager.drainVis(p.level(),p.blockPosition(),1,true)>=1){AuraManager.drainVis(p.level(),p.blockPosition(),1,false);charge=Math.min(maxShield(p),charge+1);data.putLong("tc_runic_next",p.level().getGameTime()+40);}data.putFloat("tc_runic_charge",charge);
            var boots=p.getItemBySlot(EquipmentSlot.FEET);if(boots.getItem() instanceof TravellerBoots&&!p.isShiftKeyDown()&&p.isSprinting()&&RechargeHelper.consumeCharge(boots,p,1)){p.addEffect(new MobEffectInstance(MobEffects.SPEED,25,1,false,false));p.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST,25,1,false,false));}}
        });
        LivingDamageEvent.BUS.addListener(e->{if(!(e.getEntity() instanceof ServerPlayer p)||e.getAmount()<=0||e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))return;float charge=shield(p),used=Math.min(charge,e.getAmount());if(used>0){p.getPersistentData().putFloat("tc_runic_charge",charge-used);p.getPersistentData().putLong("tc_runic_next",p.level().getGameTime()+80);e.setAmount(e.getAmount()-used);p.level().sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,p.getX(),p.getY()+1,p.getZ(),10,.4,.6,.4,.1);p.sendSystemMessage(Component.translatable("equipment.thaumcraft.shield",Math.round(charge-used),maxShield(p)),true);}});
        LivingFallEvent.BUS.addListener(e->{if(e.getEntity() instanceof ServerPlayer p){var boots=p.getItemBySlot(EquipmentSlot.FEET);if(boots.getItem() instanceof TravellerBoots&&e.getDistance()>3&&RechargeHelper.consumeCharge(boots,p,1))e.setDistance(Math.max(0,e.getDistance()-6));}});
        net.minecraftforge.event.entity.player.PlayerEvent.Clone.BUS.addListener(e->{if(!e.isWasDeath()){e.getEntity().getPersistentData().putFloat("tc_runic_charge",shield(e.getOriginal()));e.getEntity().getPersistentData().putLong("tc_runic_next",e.getOriginal().getPersistentData().getLongOr("tc_runic_next",0));}});
        net.minecraftforge.event.entity.player.ItemTooltipEvent.BUS.addListener(e->{if(e.getItemStack().getItem() instanceof AccessoryItem)e.getToolTip().add(Component.translatable("equipment.thaumcraft.accessory"));if(e.getItemStack().getItem() instanceof IRechargable chargeable&&chargeable.getMaxCharge(e.getItemStack(),null)>0)e.getToolTip().add(Component.translatable("equipment.thaumcraft.charge",RechargeHelper.getCharge(e.getItemStack()),chargeable.getMaxCharge(e.getItemStack(),null)));int n=runic(e.getItemStack());if(n>0)e.getToolTip().add(Component.translatable("equipment.thaumcraft.runic",n));int discount=DISCOUNTS.getOrDefault(name(e.getItemStack()),0);if(discount>0)e.getToolTip().add(Component.translatable("equipment.thaumcraft.discount",discount));if(REPAIR.contains(name(e.getItemStack())))e.getToolTip().add(Component.translatable("equipment.thaumcraft.repair"));});
    }
    public static final class TravellerBoots extends Item implements IRechargable {public TravellerBoots(Properties p){super(p);}public int getMaxCharge(ItemStack s,LivingEntity e){return 240;}}
}
