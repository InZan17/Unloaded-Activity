package lol.zanspace.unloadedactivity.fabric.mixin;

#if MC_VER >= MC_1_19_4
import net.minecraft.registry.DynamicRegistryManager;
#endif
#if MC_VER >= MC_1_21_3
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeEntry;
#elif MC_VER >= MC_1_20_2
import net.minecraft.recipe.RecipeEntry;
#else
import net.minecraft.recipe.Recipe;
#endif
#if MC_VER >= MC_1_21_3
import net.minecraft.recipe.input.SingleStackRecipeInput;
#endif

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityInvoker {
    @Invoker("craftRecipe")
    public static boolean invokeCraftRecipe(
        #if MC_VER >= MC_1_19_4
        DynamicRegistryManager registryManager,
        #endif
        #if MC_VER >= MC_1_21_3
        @Nullable RecipeEntry<? extends AbstractCookingRecipe>
        #elif MC_VER >= MC_1_20_2
        @Nullable RecipeEntry<?>
        #else
        @Nullable Recipe<?>
        #endif
        recipe,
        #if MC_VER >= MC_1_21_3
        SingleStackRecipeInput input,
        #endif
        DefaultedList<ItemStack> slots,
        int count
    ) {
        throw new AssertionError();
    }
}