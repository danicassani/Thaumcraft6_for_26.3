package thaumcraft.pech;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.item.enchantment.*;
import thaumcraft.content.SimpleContent;

/** The active original ConfigEntities lists, including duplicate entries as weights. */
public final class PechTrades {
    public record Offer(int rank,ItemStack stack){}
    public record Roll(List<ItemStack> outputs,List<ItemStack> pack){}
    private PechTrades(){}
    private static ItemStack item(String name){var value=BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(name));if(value==null||value==Items.AIR)throw new IllegalStateException("Missing pech reward: "+name);return new ItemStack(value);}
    private static void add(List<Offer> out,int rank,String... items){for(var id:items)out.add(new Offer(rank,item(id)));}
    private static ItemStack book(ServerLevel level,net.minecraft.resources.ResourceKey<Enchantment> key){var result=new ItemStack(Items.ENCHANTED_BOOK);var ench=new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);ench.set(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key),1);result.set(DataComponents.STORED_ENCHANTMENTS,ench.toImmutable());return result;}
    private static ItemStack potion(net.minecraft.core.Holder<Potion> potion){var result=new ItemStack(Items.POTION);result.set(DataComponents.POTION_CONTENTS,new PotionContents(potion));return result;}
    public static List<Offer> offers(ServerLevel l,int variant){var out=new ArrayList<Offer>();
        if(variant==0){
            add(out,1,"thaumcraft:cluster_iron","thaumcraft:cluster_gold","thaumcraft:cluster_cinnabar","thaumcraft:cluster_quartz");
            for(var metal:List.of("copper","tin","silver","lead")){var tag=TagKey.create(Registries.ITEM,net.minecraft.resources.Identifier.parse("c:ingots/"+metal));if(metal.equals("copper")||BuiltInRegistries.ITEM.get(tag).map(s->s.size()>0).orElse(false))add(out,1,"thaumcraft:cluster_"+metal);}
            add(out,2,"minecraft:blaze_rod","thaumcraft:sapling_greatwood","minecraft:dragon_breath","minecraft:compass");add(out,3,"minecraft:experience_bottle","minecraft:experience_bottle","minecraft:golden_apple");add(out,4,"thaumcraft:thaumium_pick","thaumcraft:thaumium_axe","thaumcraft:thaumium_hoe","minecraft:spectral_arrow");add(out,5,"minecraft:enchanted_golden_apple","thaumcraft:sapling_silverwood","minecraft:totem_of_undying");out.add(new Offer(5,thaumcraft.infusion.CuriosityContent.curio("knowledge")));
        }else if(variant==1){
            for(var aspect:List.of("aer","terra","ignis","aqua","ordo","perditio"))out.add(new Offer(1,thaumcraft.crystals.CrystalContent.essence(aspect,1)));
            out.add(new Offer(2,potion(Potions.REGENERATION)));out.add(new Offer(2,potion(Potions.STRONG_HEALING)));out.add(new Offer(2,thaumcraft.crystals.CrystalContent.essence("vitium",1)));
            add(out,3,"minecraft:experience_bottle","minecraft:experience_bottle","minecraft:golden_apple");out.add(new Offer(3,thaumcraft.crystals.CrystalContent.essence("auram",1)));
            add(out,4,"thaumcraft:cloth_boots","thaumcraft:cloth_chest","thaumcraft:cloth_legs");add(out,5,"minecraft:enchanted_golden_apple","thaumcraft:pech_wand","thaumcraft:amulet_vis");out.add(new Offer(5,thaumcraft.infusion.CuriosityContent.curio("knowledge")));
        }else if(variant==2){
            for(int i=0;i<15;i++){var dye=DyeColor.byId(15-i);String name=dye==DyeColor.LIGHT_GRAY?"silver":dye==DyeColor.LIGHT_BLUE?"lightblue":dye.getSerializedName();add(out,1,"thaumcraft:candle_"+name);}
            add(out,2,"minecraft:ghast_tear");out.add(new Offer(2,book(l,Enchantments.POWER)));
            add(out,3,"minecraft:experience_bottle","minecraft:experience_bottle","minecraft:golden_apple");
            // ItemsTC.eldritchEye is never registered/assigned in the original TC6 sources.
            out.add(new Offer(4,ItemStack.EMPTY));add(out,4,"minecraft:enchanted_golden_apple");
            add(out,5,"thaumcraft:ring_apprentice");out.add(new Offer(5,book(l,Enchantments.FLAME)));out.add(new Offer(5,book(l,Enchantments.INFINITY)));out.add(new Offer(5,thaumcraft.infusion.CuriosityContent.curio("knowledge")));
        }else throw new IllegalArgumentException("Invalid pech variant");return List.copyOf(out);
    }
    private static boolean insert(List<ItemStack> slots,ItemStack reward){var left=reward.copy();if(left.isEmpty())return true;for(var old:slots)if(!old.isEmpty()&&ItemStack.isSameItemSameComponents(old,left)){int amount=Math.min(left.getCount(),old.getMaxStackSize()-old.getCount());old.grow(amount);left.shrink(amount);if(left.isEmpty())return true;}for(int i=0;i<slots.size();i++)if(slots.get(i).isEmpty()){int amount=Math.min(left.getCount(),left.getMaxStackSize());slots.set(i,left.copyWithCount(amount));left.shrink(amount);if(left.isEmpty())return true;}return false;}
    public static Optional<Roll> roll(ServerLevel level,Pech pech,int initial){var random=pech.getRandom();int value=initial;if(random.nextInt(5)==0)value+=random.nextInt(3);else if(random.nextBoolean())value-=random.nextInt(3);
        var outputs=new ArrayList<ItemStack>(Collections.nCopies(4,ItemStack.EMPTY));var pack=new ArrayList<ItemStack>();for(int i=0;i<9;i++)pack.add(pech.pack.getItem(i).copy());var offers=offers(level,pech.variant());
        while(value>0){int rank=Math.min(5,Math.max((value+1)/2,random.nextInt(value)+1));value-=rank;ItemStack reward;
            var occupied=new ArrayList<Integer>();for(int i=0;i<9;i++)if(!pack.get(i).isEmpty())occupied.add(i);
            if(rank==1&&random.nextBoolean()&&!occupied.isEmpty()){int slot=occupied.get(random.nextInt(occupied.size()));reward=pack.get(slot).copyWithCount(1);pack.get(slot).shrink(1);}
            else{if(rank>=4&&random.nextBoolean())continue;var choices=offers.stream().filter(o->o.rank()==rank).toList();if(choices.isEmpty())throw new IllegalStateException("No pech rank "+rank);reward=choices.get(random.nextInt(choices.size())).stack().copy();}
            if(!insert(outputs,reward))return Optional.empty();
        }
        return Optional.of(new Roll(outputs,pack));
    }
}
