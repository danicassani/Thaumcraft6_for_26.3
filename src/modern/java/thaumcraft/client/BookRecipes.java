package thaumcraft.client;
import com.google.gson.*;
import java.util.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import thaumcraft.research.ResearchCatalog;
/** The book renders actual item stacks from generated modern recipes, never an ASCII recipe or a registry ID. */
public final class BookRecipes {
 private static final JsonObject VIEWS=ResearchCatalog.load("recipe_views.json").getAsJsonObject();
 public static JsonObject get(String key){return VIEWS.has(key)?VIEWS.getAsJsonObject(key):null;}
 public static List<String> expand(String key){var view=get(key);if(view==null||!view.has("choices"))return List.of(key);var keys=new ArrayList<String>();for(var choice:view.getAsJsonArray("choices"))keys.add(choice.getAsString());return List.copyOf(keys);}
 public static ItemStack result(JsonObject view){if(!view.has("infusion_enchantment"))return stack(view.get("result"));String name=view.get("infusion_enchantment").getAsString();var k=thaumcraft.infusion.InfusionEnchantments.Kind.of(name);if(k!=null){var item=new ItemStack(k==thaumcraft.infusion.InfusionEnchantments.Kind.ARCING||k==thaumcraft.infusion.InfusionEnchantments.Kind.ESSENCE?Items.DIAMOND_SWORD:Items.DIAMOND_PICKAXE);return thaumcraft.infusion.InfusionEnchantments.apply(item,k);}var input=stack(view.get("input"));if(name.equals("runic"))return thaumcraft.equipment.EquipmentContent.augment(input);if(name.startsWith("mask_")||name.equals("fortress_goggles"))return thaumcraft.equipment.FortressMasks.apply(input,name);return stack(view.get("result"));}
 public static ItemStack stack(JsonElement raw){
  if(raw==null||raw.isJsonNull())return ItemStack.EMPTY;
  if(raw.isJsonArray()){var a=raw.getAsJsonArray();return a.isEmpty()?ItemStack.EMPTY:stack(a.get(0));}
  if(raw.isJsonPrimitive()){String id=raw.getAsString();if(id.startsWith("#")){for(var item:BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM,Identifier.parse(id.substring(1)))))return new ItemStack(item.value());return ItemStack.EMPTY;}return BuiltInRegistries.ITEM.get(Identifier.parse(id)).map(h->new ItemStack(h.value())).orElse(ItemStack.EMPTY);}
  var o=raw.getAsJsonObject();if(o.has("item")){var item=stack(o.get("item"));if(o.has("crystal_aspect")&&item.is(thaumcraft.crystals.CrystalContent.ESSENCE.get()))return thaumcraft.crystals.CrystalContent.essence(o.get("crystal_aspect").getAsString(),1);return item;}if(o.has("tag"))return stack(new JsonPrimitive("#"+o.get("tag").getAsString()));
  if(o.has("id")){if(o.has("components")&&net.minecraft.client.Minecraft.getInstance().level!=null){var decoded=ItemStack.CODEC.parse(net.minecraft.resources.RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE,net.minecraft.client.Minecraft.getInstance().level.registryAccess()),o).result();if(decoded.isPresent())return decoded.get();}var result=stack(o.get("id"));if(!result.isEmpty()){if(o.has("count"))result.setCount(o.get("count").getAsInt());if(o.has("aspect")&&result.is(thaumcraft.alchemy.AlchemyContent.PHIAL.get()))return thaumcraft.alchemy.AlchemyContent.filled(thaumcraft.alchemy.AlchemyContent.PHIAL.get(),o.get("aspect").getAsString(),10);if(o.has("aspect")&&result.is(thaumcraft.crystals.CrystalContent.ESSENCE.get()))return thaumcraft.crystals.CrystalContent.essence(o.get("aspect").getAsString(),result.getCount());if(o.has("components")&&o.getAsJsonObject("components").has("thaumcraft:crystal_aspect"))return thaumcraft.crystals.CrystalContent.essence(o.getAsJsonObject("components").get("thaumcraft:crystal_aspect").getAsString(),result.getCount());}return result;}
  return ItemStack.EMPTY;
 }
 private BookRecipes(){}
}
