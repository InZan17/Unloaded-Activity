package lol.zanspace.unloadedactivity.mixin.block_entities;

import lol.zanspace.unloadedactivity.interfaces.SimulateBlockEntity;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
#if MC_VER >= MC_1_19_4
import net.minecraft.registry.DynamicRegistryManager;
#endif
#if MC_VER >= MC_1_21_1
import net.minecraft.recipe.input.SingleStackRecipeInput;
#endif
#if MC_VER >= MC_1_21_3
import net.minecraft.item.FuelRegistry;
#endif
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static java.lang.Math.*;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin extends LockableContainerBlockEntity implements SidedInventory, RecipeUnlocker, RecipeInputProvider, SimulateBlockEntity {

    @Shadow int burnTime;
    @Shadow int fuelTime;
    @Shadow int cookTime;
    @Shadow int cookTimeTotal;
    #if MC_VER >= MC_1_21_3
    @Shadow @Final private ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> matchGetter;
    #elif MC_VER == MC_1_21_1
    @Shadow @Final private RecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> matchGetter;
    #else
    @Shadow @Final private RecipeManager.MatchGetter<Inventory, ? extends AbstractCookingRecipe> matchGetter;
    #endif

    #if MC_VER >= MC_1_21_3
    @Shadow private static int getCookTime(ServerWorld world, AbstractFurnaceBlockEntity furnace) {
        return 0;
    }
    #else
    @Shadow private static int getCookTime(World world, AbstractFurnaceBlockEntity furnace) {
        return 0;
    }
    #endif
    protected AbstractFurnaceBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Shadow private boolean isBurning() {
        return this.burnTime > 0;
    }
    @Shadow protected DefaultedList<ItemStack> inventory;

    #if MC_VER >= MC_1_21_3
    @Shadow protected abstract int getFuelTime(FuelRegistry fuelRegistry, ItemStack fuel);
    #else
    @Shadow protected abstract int getFuelTime(ItemStack fuel);
    #endif

    @Shadow
    private static boolean craftRecipe(
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
        int maxCount
    ) { return true; }

    @Shadow
    public void setLastRecipe(@Nullable #if MC_VER == MC_1_20_2 RecipeEntry<?> #elif MC_VER > MC_1_20_2 RecipeEntry #else Recipe<?> #endif recipe) {}

    @Override
    public boolean canSimulate() {
        return true;
    }

    public boolean shouldSimulate(World world, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (state == null) return false;
        return UnloadedActivity.config.updateFurnace;
    }

    @Override public void simulateTime(ServerWorld world, BlockPos pos, BlockState state, BlockEntity blockEntity, long timeDifference)  {

        if (shouldSimulate(world, pos, state, blockEntity)) {
            AbstractFurnaceBlockEntity abstractFurnaceBlockEntity = (AbstractFurnaceBlockEntity) blockEntity;
            boolean oldIsBurning = this.isBurning();
            boolean stateChanged = false;
            ItemStack itemStack = this.inventory.get(0);
            ItemStack fuelStack = this.inventory.get(1);
            ItemStack finishedStack = this.inventory.get(2);
            int inputCount = itemStack.getCount();
            int fuelCount = fuelStack.getCount();

            #if MC_VER >= MC_1_21_3
            SingleStackRecipeInput singleStackRecipeInput = new SingleStackRecipeInput(itemStack);
            #endif

            #if MC_VER > MC_1_20_2
            RecipeEntry
            #elif MC_VER == MC_1_20_2
            RecipeEntry<?>
            #else
            Recipe<?>
            #endif
            recipe = inputCount != 0 ?
                #if MC_VER >= MC_1_21_1
                this.matchGetter.getFirstMatch(new SingleStackRecipeInput(itemStack), world).orElse(null)
                #else
                this.matchGetter.getFirstMatch(abstractFurnaceBlockEntity, world).orElse(null)
                #endif
            : null;

            int maxPerStack = abstractFurnaceBlockEntity.getMaxCountPerStack();

            #if MC_VER >= MC_1_21_3
            int fuelTime = this.getFuelTime(world.getFuelRegistry(),fuelStack);
            #else
            int fuelTime = this.getFuelTime(fuelStack);
            #endif
            if (fuelTime == 0)
                fuelTime = this.fuelTime;

            if (fuelTime != 0) {
                if (this.cookTimeTotal == 0)
                    this.cookTimeTotal = getCookTime(world, abstractFurnaceBlockEntity);

                int spacesLeft = maxPerStack - finishedStack.getCount();

                //The amount of time to burn before we catch up to now or until we run out of something.
                int availableBurning = 0;

                if (recipe != null) { //if recipe is null then availableBurning will remain 0
                    availableBurning = (int) min(timeDifference, (long) this.cookTimeTotal * min(inputCount, spacesLeft) - this.cookTime);
                    availableBurning = min(availableBurning, fuelTime * fuelCount + this.burnTime);
                }

                long leftoverTime = timeDifference - availableBurning;

                int fuelsConsumed = (int) ceil((float) max(availableBurning - this.burnTime, 0) / (float) fuelTime);
                this.burnTime = (int) max((this.burnTime - availableBurning + fuelsConsumed * fuelTime) - leftoverTime, 0);

                int itemsCrafted = (availableBurning + this.cookTime) / this.cookTimeTotal;
                this.cookTime = (int) max(((availableBurning + this.cookTime) % this.cookTimeTotal) - leftoverTime * 2, 0);

                if (fuelsConsumed > 0) {
                    stateChanged = true;
                    Item fuelItem = fuelStack.getItem();
                    fuelStack.decrement(fuelsConsumed);
                    if (fuelStack.isEmpty()) {
                        #if MC_VER >= MC_1_21_3
                        this.inventory.set(1, fuelItem.getRecipeRemainder());
                        #else
                        Item remainder = fuelItem.getRecipeRemainder();
                        this.inventory.set(1, remainder == null ? fuelStack.EMPTY : new ItemStack(remainder));
                        #endif
                    }
                }

                if (itemsCrafted > 0) {
                    stateChanged = true;
                    for (int i = 0; i < itemsCrafted; i++) {
                        craftRecipe(#if MC_VER >= MC_1_19_4 world.getRegistryManager(), #endif recipe, #if MC_VER >= MC_1_21_3 singleStackRecipeInput, #endif this.inventory, maxPerStack);
                        setLastRecipe(recipe);
                    }
                }

                if (itemStack.getCount() == 0 || maxPerStack - finishedStack.getCount() == 0)
                    this.cookTime = 0;

                if (oldIsBurning != this.isBurning()) {
                    stateChanged = true;
                    state = state.with(AbstractFurnaceBlock.LIT, this.isBurning());
                    world.setBlockState(pos, state, Block.NOTIFY_ALL);
                }

                if (!this.isBurning())
                    this.fuelTime = 0;

                if (stateChanged) {
                    AbstractFurnaceBlockEntity.markDirty(world, pos, state);
                }
            }
        }

        super.simulateTime(world, pos, state, blockEntity, timeDifference);
    }
}
