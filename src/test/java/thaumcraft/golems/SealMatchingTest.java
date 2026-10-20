package thaumcraft.golems;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SealMatchingTest {
 @Test void onlyMaterialEquivalenceTagsAreAccepted(){
  for(String id:new String[]{"c:ingots/copper","c:ores/iron","c:raw_materials/gold","minecraft:logs","minecraft:planks","minecraft:wool","minecraft:sand"})assertTrue(SealMatching.materialTag(Identifier.parse(id)),id);
  for(String id:new String[]{"c:ingots","c:ingots/","minecraft:enchantable/durability","minecraft:tools","c:tools/melee_weapon","other:ingots/copper","minecraft:logs_that_burn"})assertFalse(SealMatching.materialTag(Identifier.parse(id)),id);
 }
}
