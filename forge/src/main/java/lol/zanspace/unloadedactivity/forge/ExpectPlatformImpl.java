package lol.zanspace.unloadedactivity.forge;

#if MC_VER >= MC_1_19_4
import net.minecraft.registry.DynamicRegistryManager;
#endif
import lol.zanspace.unloadedactivity.UnloadedActivity;
import net.minecraft.recipe.Recipe;

import lol.zanspace.unloadedactivity.forge.mixin.AbstractFurnaceBlockEntityInvoker;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class ExpectPlatformImpl {
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
    public static boolean craftRecipe(
        #if MC_VER >= MC_1_19_4
        DynamicRegistryManager registryManager,
        #endif
        @Nullable Recipe<?> recipe,
        DefaultedList<ItemStack> slots,
        int count,
        AbstractFurnaceBlockEntity furnace
    )  {
        AbstractFurnaceBlockEntityInvoker furnaceInvoker = (AbstractFurnaceBlockEntityInvoker) furnace;
        return furnaceInvoker.invokeCraftRecipe(#if MC_VER >= MC_1_19_4 registryManager, #endif recipe, slots, count);
    }
}
