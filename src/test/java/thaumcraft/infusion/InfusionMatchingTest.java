package thaumcraft.infusion;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
final class InfusionMatchingTest{
    @Test void overlappingTagsDoNotGreedilyStealAnExactIngredient(){assertArrayEquals(new int[]{1,0},InfusionMatching.assign(new boolean[][]{{true,true},{true,false}}));}
    @Test void quantitiesRequireDistinctPedestals(){assertNull(InfusionMatching.assign(new boolean[][]{{true,false},{true,false}}));}
    @Test void arbitraryOrderAndDuplicateIngredientsMatch(){assertArrayEquals(new int[]{2,1,0},InfusionMatching.assign(new boolean[][]{{false,false,true},{true,true,false},{true,true,false}}));}
    @Test void rectangularOrExtraIngredientInputsAreRejected(){assertNull(InfusionMatching.assign(new boolean[][]{{true,false}}));}
}
