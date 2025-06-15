package lol.zanspace.unloadedactivity.neoforge;

#if MC_VER >= MC_1_21_1
import lol.zanspace.unloadedactivity.neoforge.mixin.CropBlockInvoker;
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

import lol.zanspace.unloadedactivity.neoforge.mixin.AbstractFurnaceBlockEntityInvoker;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class ExpectPlatformImpl {
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
    #if MC_VER >= MC_1_21_1
    public static float getAvailableMoisture(BlockState blockState, BlockView world, BlockPos pos) {
        return CropBlockInvoker.getAvailableMoisture(blockState, world, pos);
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
        #if MC_VER >= MC_1_20_6
        return AbstractFurnaceBlockEntityInvoker.invokeCraftRecipe(
            #if MC_VER >= MC_1_19_4 registryManager, #endif
            recipe,
            #if MC_VER >= MC_1_21_3 input, #endif
            slots,
            count
            #if MC_VER <= MC_1_21_1 , furnace #endif
        );
        #else
        AbstractFurnaceBlockEntityInvoker furnaceInvoker = (AbstractFurnaceBlockEntityInvoker) furnace;
        return furnaceInvoker.invokeCraftRecipe(#if MC_VER >= MC_1_19_4 registryManager, #endif recipe, slots, count);
        #endif
    }
}
