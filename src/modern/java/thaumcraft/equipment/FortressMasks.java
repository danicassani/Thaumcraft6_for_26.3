package thaumcraft.equipment;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.*;
public final class FortressMasks {
 private static final DeferredRegister<DataComponentType<?>> TYPES=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,"thaumcraft");
 public static final RegistryObject<DataComponentType<Integer>> MASK=TYPES.register("fortress_mask",()->DataComponentType.<Integer>builder().persistent(Codec.intRange(0,2)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
 public static final RegistryObject<DataComponentType<Boolean>> GOGGLES=TYPES.register("fortress_goggles",()->DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
 public static boolean upgrade(String key){return key.equals("mask_0")||key.equals("mask_1")||key.equals("mask_2")||key.equals("fortress_goggles");}
 public static boolean accepts(ItemStack s,String key){return upgrade(key)&&s.getCount()==1&&EquipmentContent.name(s).equals("fortress_helm")&&(key.equals("fortress_goggles")?!s.getOrDefault(GOGGLES.get(),false):s.getOrDefault(MASK.get(),-1)!=key.charAt(5)-'0');}
 public static ItemStack apply(ItemStack s,String key){if(!accepts(s,key))return ItemStack.EMPTY;var out=s.copy();if(key.equals("fortress_goggles"))out.set(GOGGLES.get(),true);else out.set(MASK.get(),key.charAt(5)-'0');return out;}
 public static boolean hasGoggles(LivingEntity e){var s=e.getItemBySlot(EquipmentSlot.HEAD);String n=EquipmentContent.name(s);return n.equals("goggles")||n.equals("void_robe_helm")||n.equals("fortress_helm")&&s.getOrDefault(GOGGLES.get(),false);}
 public static int worn(LivingEntity e){var s=e.getItemBySlot(EquipmentSlot.HEAD);return EquipmentContent.name(s).equals("fortress_helm")?s.getOrDefault(MASK.get(),-1):-1;}
 public static void register(net.minecraftforge.eventbus.api.bus.BusGroup bus){TYPES.register(bus);
  net.minecraftforge.event.entity.living.LivingHurtEvent.BUS.addListener(e->{if(e.getEntity().level().isClientSide()||e.getAmount()<=0)return;var source=e.getSource().getEntity();if(source instanceof net.minecraft.server.level.ServerPlayer p&&worn(p)==2&&p.getRandom().nextFloat()<e.getAmount()/12F&&!thaumcraft.thaumaturgy.SpellEngine.protectedTarget(p,e.getEntity()))p.heal(1);if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p&&source instanceof LivingEntity attacker&&attacker!=p&&worn(p)==1&&p.getRandom().nextFloat()<e.getAmount()/10F&&!thaumcraft.thaumaturgy.SpellEngine.protectedTarget(p,attacker))attacker.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER,80));});
  net.minecraftforge.event.entity.player.ItemTooltipEvent.BUS.addListener(e->{var s=e.getItemStack();if(!EquipmentContent.name(s).equals("fortress_helm"))return;int mask=s.getOrDefault(MASK.get(),-1);if(mask>=0)e.getToolTip().add(net.minecraft.network.chat.Component.translatable("equipment.thaumcraft.mask_"+mask));if(s.getOrDefault(GOGGLES.get(),false))e.getToolTip().add(net.minecraft.network.chat.Component.translatable("item.thaumcraft.goggles"));});
 }
}
