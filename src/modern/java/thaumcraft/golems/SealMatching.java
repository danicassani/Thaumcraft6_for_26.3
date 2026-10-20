package thaumcraft.golems;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** Modern equivalents of the original damage, NBT, ore and mod seal filters. */
public final class SealMatching {
    private SealMatching() {}
    public static boolean materialTag(Identifier id) {
        String path=id.getPath();
        if(id.getNamespace().equals("minecraft"))return path.equals("logs")||path.equals("planks")||path.equals("wool")||path.equals("sand");
        if(!id.getNamespace().equals("c"))return false;
        // Material-specific common tags only: generic ingots/tools/enchantability are not equivalents.
        for(String category:new String[]{"ingots/","nuggets/","gems/","dusts/","raw_materials/","ores/","storage_blocks/","plates/","rods/","gears/"})
            if(path.startsWith(category)&&path.length()>category.length())return true;
        return false;
    }
    public static boolean matches(ItemStack a,ItemStack b,boolean components,boolean damage,boolean tags,boolean mod) {
        if(a.isEmpty()||b.isEmpty())return false;
        if(mod)return BuiltInRegistries.ITEM.getKey(a.getItem()).getNamespace().equals(BuiltInRegistries.ITEM.getKey(b.getItem()).getNamespace());
        if(tags&&b.typeHolder().tags().anyMatch(tag->materialTag(tag.location())&&a.is(tag)))return true;
        if(!a.is(b.getItem())||damage&&a.getDamageValue()!=b.getDamageValue())return false;
        if(!components)return true;
        var left=a.copyWithCount(1);var right=b.copyWithCount(1);
        left.remove(DataComponents.DAMAGE);right.remove(DataComponents.DAMAGE);
        return ItemStack.isSameItemSameComponents(left,right);
    }
}
