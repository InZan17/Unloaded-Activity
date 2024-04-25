package com.github.inzan17.mixin.block_entities;

import com.github.inzan17.SimulateBlockEntity;
import com.github.inzan17.UnloadedActivity;
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
import net.minecraft.registry.DynamicRegistryManager;
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
    @Shadow @Final private RecipeManager.MatchGetter<Inventory, ? extends AbstractCookingRecipe> matchGetter;

    @Shadow private static int getCookTime(World world, AbstractFurnaceBlockEntity furnace) {
        return 0;
    }
    protected AbstractFurnaceBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Shadow private boolean isBurning() {
        return this.burnTime > 0;
    }
    @Shadow protected DefaultedList<ItemStack> inventory;

    @Shadow @Final private Object2IntOpenHashMap<Identifier> recipesUsed = new Object2IntOpenHashMap();

    @Shadow protected abstract int getFuelTime(ItemStack fuel);

    private static boolean craftRecipe(DynamicRegistryManager registryManager, @Nullable Recipe<?> recipe, DefaultedList<ItemStack> slots, int count, int quantity) {
        ItemStack input = slots.get(0);
        ItemStack recipeOutput = recipe.getOutput(registryManager);
        ItemStack finalOutput = slots.get(2);
        if (finalOutput.isEmpty()) {
            ItemStack recipeClone = recipeOutput.copy();
            recipeClone.increment(quantity-1);
            slots.set(2, recipeClone);
            finalOutput.increment(quantity);
        } else if (finalOutput.isOf(recipeOutput.getItem())) {
            finalOutput.increment(quantity);
        }
        if (input.isOf(Blocks.WET_SPONGE.asItem()) && !slots.get(1).isEmpty() && slots.get(1).isOf(Items.BUCKET)) {
            slots.set(1, new ItemStack(Items.WATER_BUCKET));
        }
        input.decrement(quantity);
        return true;
    }

    public void setLastRecipe(@Nullable Recipe<?> recipe, int quantity) {
        if (recipe != null) {
            Identifier identifier = recipe.getId();
            this.recipesUsed.addTo(identifier, quantity);
        }
    }

    @Override
    public boolean canSimulate() {
        return true;
    }

    public boolean shouldSimulate(World world, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (state == null) return false;
        return UnloadedActivity.instance.config.updateFurnace;
    }

    @Override public void simulateTime(World world, BlockPos pos, BlockState state, BlockEntity blockEntity, long timeDifference)  {

        if (shouldSimulate(world, pos, state, blockEntity)) {
            AbstractFurnaceBlockEntity abstractFurnaceBlockEntity = (AbstractFurnaceBlockEntity) blockEntity;
            boolean oldIsBurning = this.isBurning();
            boolean stateChanged = false;
            ItemStack itemStack = this.inventory.get(0);
            ItemStack fuelStack = this.inventory.get(1);
            ItemStack finishedStack = this.inventory.get(2);
            int inputCount = itemStack.getCount();
            int fuelCount = fuelStack.getCount();
            Recipe recipe = inputCount != 0 ? this.matchGetter.getFirstMatch(abstractFurnaceBlockEntity, world).orElse(null) : null;
            int maxPerStack = abstractFurnaceBlockEntity.getMaxCountPerStack();

            int fuelTime = this.getFuelTime(fuelStack);
            if (fuelTime == 0)
                fuelTime = this.fuelTime;

            if (fuelTime != 0) {
                if (this.cookTimeTotal == 0)
                    this.cookTimeTotal = getCookTime(this.world, abstractFurnaceBlockEntity);

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
                        Item item2 = fuelItem.getRecipeRemainder();
                        this.inventory.set(1, item2 == null ? fuelStack.EMPTY : new ItemStack(item2));
                    }
                }

                if (itemsCrafted > 0) {
                    stateChanged = true;
                    craftRecipe(world.getRegistryManager(), recipe, this.inventory, maxPerStack, itemsCrafted);
                    setLastRecipe(recipe, itemsCrafted);
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
