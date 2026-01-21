package lol.zanspace.unloadedactivity;

#if MC_VER >= MC_1_19_4
import net.minecraft.core.RegistryAccess;
#endif
#if MC_VER >= MC_1_21_3
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
#elif MC_VER >= MC_1_20_2
import net.minecraft.world.item.crafting.RecipeHolder;
#else
import net.minecraft.world.item.crafting.Recipe;
#endif
#if MC_VER >= MC_1_21_3
import net.minecraft.world.item.crafting.SingleRecipeInput;
#endif

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;

import java.nio.file.Path;

public class ExpectPlatform {
    @dev.architectury.injectables.annotations.ExpectPlatform
    public static Path getConfigDirectory() {
        throw new AssertionError();
    }
    #if MC_VER >= MC_1_21_1
    @dev.architectury.injectables.annotations.ExpectPlatform
    public static float getGrowthSpeed(BlockState blockState, BlockGetter blockGetter, BlockPos pos) {
        throw new AssertionError();
    }
    #endif

    @dev.architectury.injectables.annotations.ExpectPlatform
    public static boolean burn(
        #if MC_VER >= MC_1_19_4
        RegistryAccess registryAccess,
        #endif
        #if MC_VER >= MC_1_21_3
        @Nullable RecipeHolder<? extends AbstractCookingRecipe>
        #elif MC_VER >= MC_1_20_2
        @Nullable RecipeHolder<?>
        #else
        @Nullable Recipe<?>
        #endif
        recipe,
        #if MC_VER >= MC_1_21_3
        SingleRecipeInput input,
        #endif
        NonNullList<ItemStack> slots,
        int count,
        AbstractFurnaceBlockEntity furnace
    ) {
        throw new AssertionError("No burn impl on target platform");
    }
}
