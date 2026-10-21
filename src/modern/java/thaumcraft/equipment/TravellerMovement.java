package thaumcraft.equipment;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.*;
import thaumcraft.api.items.RechargeHelper;
/** Transient attributes restore the player's other movement modifiers on removal. */
public final class TravellerMovement {
 private TravellerMovement(){}
 private static final Identifier ID=Identifier.parse("thaumcraft:traveller_movement");
 public static void tick(ServerPlayer p){var boots=p.getItemBySlot(EquipmentSlot.FEET);boolean worn=boots.getItem() instanceof EquipmentContent.TravellerBoots;boolean active=worn&&RechargeHelper.getCharge(boots)>0&&!p.isShiftKeyDown()&&!p.getAbilities().flying&&p.isAlive()&&!p.isSpectator();set(p,Attributes.MOVEMENT_SPEED,active?(p.isInWater()?.0125:.05):0);set(p,Attributes.STEP_HEIGHT,active?Math.max(0,1-p.getAttribute(Attributes.STEP_HEIGHT).getBaseValue()):0);if(worn&&p.tickCount%20==0){int energy=boots.getOrDefault(EquipmentContent.TRAVEL_ENERGY.get(),0);if(energy>0)boots.set(EquipmentContent.TRAVEL_ENERGY.get(),energy-1);else if(RechargeHelper.consumeCharge(boots,p,1))boots.set(EquipmentContent.TRAVEL_ENERGY.get(),60);}}
 private static void set(ServerPlayer p,net.minecraft.core.Holder<Attribute> kind,double value){var a=p.getAttribute(kind);if(a==null)return;var old=a.getModifier(ID);if(old!=null&&old.amount()==value)return;if(old!=null)a.removeModifier(ID);if(value>0)a.addTransientModifier(new AttributeModifier(ID,value,AttributeModifier.Operation.ADD_VALUE));}
 public static float fallDamage(float damage,boolean boots,boolean cloud){if(boots){damage=Math.max(0,damage/2-1);if(damage<1)damage=0;}if(cloud){damage=Math.max(0,damage/3-2);if(damage<1)damage=0;}return damage;}
 public static void register(){net.minecraftforge.event.entity.living.LivingHurtEvent.BUS.addListener(e->{if(e.getSource().is(net.minecraft.world.damagesource.DamageTypes.FALL)&&e.getEntity() instanceof ServerPlayer p)e.setAmount(fallDamage(e.getAmount(),p.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof EquipmentContent.TravellerBoots,!AccessorySlots.find(p,"ring_cloud").isEmpty()));});}
}
