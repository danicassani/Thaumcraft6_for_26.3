package thaumcraft.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** A separate recipe type: ordinary crafting tables must never bypass the aura cost. */
public record ArcaneRecipe(CraftingRecipe recipe, int vis, Map<String, Integer> crystals, String research) implements Recipe<CraftingInput> {
    public static final java.util.List<String> PRIMALS = java.util.List.of("aer", "ignis", "aqua", "terra", "ordo", "perditio");
    private static final Codec<String> ASPECT = Codec.STRING.validate(s -> PRIMALS.contains(s)
        ? com.mojang.serialization.DataResult.success(s) : com.mojang.serialization.DataResult.error(() -> "Not a primal aspect: " + s));
    public static final MapCodec<ArcaneRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Recipe.DIRECT_CODEC.validate(recipe -> recipe instanceof CraftingRecipe
            ? com.mojang.serialization.DataResult.success(recipe) : com.mojang.serialization.DataResult.error(() -> "Expected a crafting recipe"))
            .xmap(recipe -> (CraftingRecipe)recipe, recipe -> (Recipe<?>)recipe).fieldOf("recipe").forGetter(ArcaneRecipe::recipe),
        Codec.intRange(0, 32766).fieldOf("vis").forGetter(ArcaneRecipe::vis),
        Codec.unboundedMap(ASPECT, Codec.intRange(1, 64)).optionalFieldOf("crystals", Map.of()).forGetter(ArcaneRecipe::crystals),
        Codec.string(0,192).optionalFieldOf("research", "").forGetter(ArcaneRecipe::research)
    ).apply(i, ArcaneRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ArcaneRecipe> STREAM = ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());
    public ArcaneRecipe { crystals = Map.copyOf(crystals); }
    @Override public boolean matches(CraftingInput input, Level level) { return recipe.matches(input, level); }
    @Override public ItemStack assemble(CraftingInput input) { return recipe.assemble(input); }
    @Override public RecipeSerializer<ArcaneRecipe> getSerializer() { return ArcaneContent.SERIALIZER.get(); }
    @Override public RecipeType<ArcaneRecipe> getType() { return ArcaneContent.RECIPES.get(); }
    @Override public boolean showNotification() { return false; }
    @Override public String group() { return recipe.group(); }
    @Override public PlacementInfo placementInfo() { return recipe.placementInfo(); }
    @Override public RecipeBookCategory recipeBookCategory() { return recipe.recipeBookCategory(); }
}
