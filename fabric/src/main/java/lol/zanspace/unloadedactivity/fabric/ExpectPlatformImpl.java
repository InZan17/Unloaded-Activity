package lol.zanspace.unloadedactivity.fabric;

import org.jetbrains.annotations.Nullable;

#if MC_VER >= MC_1_21_1
import lol.zanspace.unloadedactivity.fabric.mixin.CropBlockInvoker;
#endif


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


import lol.zanspace.unloadedactivity.fabric.mixin.AbstractFurnaceBlockEntityInvoker;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class ExpectPlatformImpl {
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
    #if MC_VER >= MC_1_21_1
    public static float getAvailableMoisture(BlockState blockState, BlockView world, BlockPos pos) {
        return CropBlockInvoker.getAvailableMoisture(blockState.getBlock(), world, pos);
    }
    #endif
    public static boolean craftRecipe(
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
        int count,
        AbstractFurnaceBlockEntity furnace
    ) {
        return AbstractFurnaceBlockEntityInvoker.invokeCraftRecipe(
            #if MC_VER >= MC_1_19_4 registryManager, #endif
            recipe,
            #if MC_VER >= MC_1_21_3 input, #endif
            slots,
            count
        );
    }
}
