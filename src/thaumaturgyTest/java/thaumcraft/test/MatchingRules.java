package thaumcraft.test;
import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import thaumcraft.golems.*;
import static thaumcraft.test.ThaumaturgyProbe.check;
public final class MatchingRules {
 static ItemStack tool(int damage,String name){var s=new ItemStack(Items.IRON_PICKAXE);s.setDamageValue(damage);s.set(DataComponents.CUSTOM_NAME,Component.literal(name));return s;}
 static void verify(){var a=tool(1,"Inspector");var b=tool(20,"Inspector");var c=tool(20,"Other");
 check(!SealMatching.matches(a,b,true,true,false,false),"Exact accepted different damage");
 check(SealMatching.matches(a,b,true,false,false,false),"Damage ignore rejected same components");
 check(!SealMatching.matches(a,c,true,false,false,false),"Damage ignore also ignored custom name");
 check(SealMatching.matches(a,c,false,false,false,false),"Relaxed comparison rejected same item");
 check(!SealMatching.matches(a,c,false,true,false,false),"Damage-only comparison ignored wear");
 var oak=new ItemStack(Items.OAK_PLANKS);var birch=new ItemStack(Items.BIRCH_PLANKS);
 check(!SealMatching.matches(oak,birch,true,true,false,false)&&SealMatching.matches(oak,birch,true,true,true,false),"Plank material equivalence failed");
 check(!SealMatching.matches(new ItemStack(Items.IRON_INGOT),new ItemStack(Items.GOLD_INGOT),false,false,true,false),"Generic ingots treated as same material");
 check(!SealMatching.matches(a,new ItemStack(Items.DIAMOND_AXE),false,false,true,false),"Generic tool tags matched");
 check(SealMatching.matches(oak,new ItemStack(Items.DIAMOND),true,true,false,true),"Mod precedence failed");
 check(!SealMatching.matches(oak,new ItemStack(thaumcraft.content.SimpleContent.ITEMS.get("golem_bell").get()),false,false,true,true),"Mod filter crossed namespace");
 check(!SealMatching.matches(ItemStack.EMPTY,oak,false,false,true,true),"Empty stack accepted");
 }
}
