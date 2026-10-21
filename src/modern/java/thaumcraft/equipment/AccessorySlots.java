package thaumcraft.equipment;
import java.util.*;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
/** Seven physical, server-owned slots. Unknown data is preserved and disabled. */
public final class AccessorySlots {
 public static final String ID="thaumcraft:accessories";
 public static final List<String> TYPES=List.of("amulet","ring","ring","belt","head","body","charm");
 private static final Map<ServerPlayer,Storage> CACHE=new WeakHashMap<>();
 public record State(int version,List<ItemStack> items){}
 public static final Codec<State> CODEC=RecordCodecBuilder.<State>create(i->i.group(Codec.intRange(1,1).fieldOf("version").forGetter(State::version),ItemStack.OPTIONAL_CODEC.listOf().fieldOf("items").forGetter(State::items)).apply(i,State::new)).validate(s->{if(s.items().size()!=7)return DataResult.error(()->"Expected seven accessory slots");for(int i=0;i<7;i++)if(!accepts(i,s.items().get(i)))return DataResult.error(()->"Invalid accessory slot contents");return DataResult.success(s);});
 private AccessorySlots(){}
 public static String type(ItemStack s){return switch(EquipmentContent.name(s)){case "amulet_vis","amulet_mundane","amulet_fancy"->"amulet";case "ring_cloud","ring_mundane","ring_fancy","ring_apprentice"->"ring";case "girdle_mundane","girdle_fancy"->"belt";case "band_curiosity"->"head";case "charm_verdant","charm_verdant_life","charm_verdant_sustain","charm_voidseer","charm_undying"->"charm";default->"";};}
 public static boolean accepts(int slot,ItemStack s){return slot>=0&&slot<7&&(s.isEmpty()||s.getCount()==1&&s.getItem() instanceof AccessoryItem&&TYPES.get(slot).equals(type(s)));}
 public static Storage storage(ServerPlayer p){var cached=CACHE.get(p);if(cached!=null&&cached.current())return cached;var raw=p.getPersistentData().get(ID);State state;if(raw==null)state=new State(1,Collections.nCopies(7,ItemStack.EMPTY));else{var result=CODEC.parse(p.registryAccess().createSerializationContext(NbtOps.INSTANCE),raw).result();if(result.isEmpty()){CACHE.remove(p);return null;}state=result.get();}var result=new Storage(p,state,raw);CACHE.put(p,result);return result;}
 public static List<ItemStack> equipped(Player p){var out=new ArrayList<ItemStack>();if(p instanceof ServerPlayer real){var slots=storage(real);if(slots!=null)for(int i=0;i<7;i++)if(!slots.getItem(i).isEmpty())out.add(slots.getItem(i));}if(p.getOffhandItem().getItem() instanceof AccessoryItem)out.add(p.getOffhandItem());return out;}
 public static ItemStack find(Player p,String name){return equipped(p).stream().filter(s->EquipmentContent.name(s).equals(name)).findFirst().orElse(ItemStack.EMPTY);}
 public static void flush(ServerPlayer p){var s=storage(p);if(s!=null)s.persist();}
 public static boolean equip(ServerPlayer p,ItemStack held){var s=storage(p);if(s==null||held.isEmpty())return false;int chosen=-1;for(int i=0;i<7;i++)if(accepts(i,held)){if(chosen<0)chosen=i;if(s.getItem(i).isEmpty()){chosen=i;break;}}if(chosen<0)return false;var previous=s.getItem(chosen);s.setItem(chosen,held);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,previous);p.getInventory().setChanged();return true;}
 public static final class Storage extends SimpleContainer {
  private final ServerPlayer player;private Tag snapshot;private boolean ready,valid=true;
  private Storage(ServerPlayer p,State s,Tag raw){super(7);player=p;snapshot=raw==null?null:raw.copy();for(int i=0;i<7;i++)super.setItem(i,s.items().get(i));ready=true;}
  public boolean current(){return valid&&Objects.equals(snapshot,player.getPersistentData().get(ID));}
  public void setItem(int i,ItemStack s){if(!accepts(i,s))throw new IllegalArgumentException("Wrong accessory slot");if(ready&&!current())throw new IllegalStateException("Accessory data changed externally");super.setItem(i,s);}
  public boolean persist(){if(!ready)return true;if(!current()){valid=false;return false;}var list=new ArrayList<ItemStack>();for(int i=0;i<7;i++)list.add(getItem(i).copy());var encoded=CODEC.encodeStart(player.registryAccess().createSerializationContext(NbtOps.INSTANCE),new State(1,list)).result();if(encoded.isEmpty()){valid=false;return false;}var tag=encoded.get();player.getPersistentData().put(ID,tag);snapshot=tag.copy();return true;}
  public void setChanged(){super.setChanged();if(ready)persist();}
 }
 public static void register(){
  net.minecraftforge.event.entity.player.PlayerEvent.Clone.BUS.addListener(e->{if(e.getOriginal() instanceof ServerPlayer old&&e.getEntity() instanceof ServerPlayer next){flush(old);var raw=old.getPersistentData().get(ID);if(raw!=null)next.getPersistentData().put(ID,raw.copy());CACHE.remove(old);CACHE.remove(next);}});
  net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent.BUS.addListener(e->{if(e.getEntity() instanceof ServerPlayer p){flush(p);CACHE.remove(p);}});
  net.minecraftforge.event.server.ServerStoppedEvent.BUS.addListener(e->CACHE.clear());
  net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(e->{if(!(e.getEntity() instanceof ServerPlayer p))return;var s=storage(p);if(s==null||p.level().getGameRules().get(net.minecraft.world.level.gamerules.GameRules.KEEP_INVENTORY))return;var drops=new ArrayList<ItemStack>();for(int i=0;i<7;i++)drops.add(s.getItem(i).copy());for(int i=0;i<7;i++)s.setItem(i,ItemStack.EMPTY);for(var stack:drops)if(!stack.isEmpty()&&!net.minecraft.world.item.enchantment.EnchantmentHelper.has(stack,net.minecraft.world.item.enchantment.EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP))e.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(p.level(),p.getX(),p.getY(),p.getZ(),stack));});
  net.minecraftforge.event.entity.living.LivingDeathEvent.BUS.addListener(e->e.getEntity() instanceof ServerPlayer p&&protect(p,e.getSource()));
 }
 public static boolean protect(ServerPlayer p,net.minecraft.world.damagesource.DamageSource damage){if(damage.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))return false;var s=storage(p);if(s==null)return false;for(int i=0;i<7;i++){var stack=s.getItem(i);if(!EquipmentContent.name(stack).equals("charm_undying"))continue;var protection=stack.get(net.minecraft.core.component.DataComponents.DEATH_PROTECTION);if(protection==null)continue;var used=stack.copy();s.removeItem(i,1);p.setHealth(1);protection.applyEffects(used,p);if(p.connection!=null){p.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(net.minecraft.world.item.Items.TOTEM_OF_UNDYING));net.minecraft.advancements.triggers.CriteriaTriggers.USED_TOTEM.trigger(p,used);}p.level().broadcastEntityEvent(p,(byte)35);return true;}return false;}
}
