package thaumcraft.equipment;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
/** Original priority-one absorption; ordinary armor/toughness remains the native damage stage. */
public final class SpecialArmor {
 public static final TagKey<DamageType> MAGIC=TagKey.create(Registries.DAMAGE_TYPE,Identifier.parse("thaumcraft:is_magic"));
 private SpecialArmor(){}
 public static double ratio(String name,boolean magic,boolean fire,boolean explosion){int points=switch(name){case "fortress_helm"->3;case "fortress_chest"->7;case "fortress_legs"->6;case "void_robe_helm"->4;case "void_robe_chest"->9;case "void_robe_legs"->7;default->0;};if(magic)return points/35.;if(name.startsWith("fortress_")&&(fire||explosion))return points/20.;return 0;}
 public static double capped(double ratio,int remaining,float damage){return damage>0?Math.min(ratio,Math.max(0,remaining)*25./damage):0;}
 public static void absorb(LivingHurtEvent e){var entity=e.getEntity();var source=e.getSource();float damage=e.getAmount();if(entity.level().isClientSide()||damage<=0||!Float.isFinite(damage)||source.is(DamageTypeTags.BYPASSES_INVULNERABILITY))return;boolean magic=source.is(MAGIC),fire=source.is(DamageTypeTags.IS_FIRE),explosion=source.is(DamageTypeTags.IS_EXPLOSION);double total=0;for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS}){var stack=entity.getItemBySlot(slot);total+=capped(ratio(EquipmentContent.name(stack),magic,fire,explosion),stack.getMaxDamage()-stack.getDamageValue()+1,damage);}if(total<=0)return;double scale=total>1?1/total:1;for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS}){var stack=entity.getItemBySlot(slot);double share=capped(ratio(EquipmentContent.name(stack),magic,fire,explosion),stack.getMaxDamage()-stack.getDamageValue()+1,damage)*scale;if(share>0)stack.hurtAndBreak(Math.max(1,(int)(damage*share/25)),entity,slot);}e.setAmount((float)(damage*(1-Math.min(1,total))));}
 public static void register(){LivingHurtEvent.BUS.addListener(SpecialArmor::absorb);}
}
