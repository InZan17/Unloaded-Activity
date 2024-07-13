package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import net.minecraft.block.*;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static net.minecraft.block.BambooBlock.LEAVES;
import static net.minecraft.block.BambooBlock.AGE;
import static net.minecraft.block.BambooBlock.STAGE;
import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.floor;

#if MC_VER >= MC_1_20_4
@Mixin(BambooShootBlock.class)
#else
@Mixin(BambooSaplingBlock.class)
#endif
public abstract class BambooSaplingMixin extends Block {
    public BambooSaplingMixin(Settings settings) {
        super(settings);
    }

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        return 1d/3d;
    }
    @Override
    public boolean implementsSimulateRandTicks() {return true;}

    @Override public boolean canSimulateRandTicks(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.config.growBamboo) return false;
        if (!world.isAir(pos.up())) return false;
        if (world.getBaseLightLevel(pos.up(), 0) < 9) return false;
        return true;
    }
    @Override public int getMaxHeightUA() {
        return 15;
    }

    public int countAirAbove(BlockView world, BlockPos pos, int maxCount) {
        int i;
        for (i = 0; i < maxCount && world.getBlockState(pos.up(i + 1)).isAir(); ++i) {
        }
        return i;
    }
    @Override
    public void simulateRandTicks(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);
        double totalOdds = getOdds(world, pos) * randomPickChance;

        int maxGrowth = countAirAbove(world,pos, getMaxHeightUA());

        int growthAmount = Utils.getOccurrences(timePassed, totalOdds, maxGrowth, random);

        for(int i=1;i<growthAmount+1;i++) {

            if (state == null)
                return;

            if (!canSimulateRandTicks(state, world, pos))
                return;

            if (i==1) {
                state = Blocks.BAMBOO.getDefaultState().with(BambooBlock.LEAVES, BambooLeaves.SMALL);
                world.setBlockState(pos.up(), state, Block.NOTIFY_ALL);
            } else {
                state = updateLeaves(state,world,pos,random,i);
            }
            pos = pos.up();
        }
    }

    public BlockState updateLeaves(BlockState state, World world, BlockPos pos, Random random, int height) {
        BlockState blockState = world.getBlockState(pos.down());
        BlockPos blockPos = pos.down(2);
        BlockState blockState2 = world.getBlockState(blockPos);
        BambooLeaves bambooLeaves = BambooLeaves.NONE;
        if (height >= 1) {
            if (!blockState.isOf(Blocks.BAMBOO) || blockState.get(LEAVES) == BambooLeaves.NONE) {
                bambooLeaves = BambooLeaves.SMALL;
            } else if (blockState.isOf(Blocks.BAMBOO) && blockState.get(LEAVES) != BambooLeaves.NONE) {
                bambooLeaves = BambooLeaves.LARGE;
                if (blockState2.isOf(Blocks.BAMBOO)) {
                    world.setBlockState(pos.down(), blockState.with(LEAVES, BambooLeaves.SMALL), Block.NOTIFY_ALL);
                    world.setBlockState(blockPos, blockState2.with(LEAVES, BambooLeaves.NONE), Block.NOTIFY_ALL);
                }
            }
        }
        int age = state.get(AGE) == 1 || blockState2.isOf(Blocks.BAMBOO) ? 1 : 0;
        int stage = height >= 11 && random.nextFloat() < 0.25f || height == 15 ? 1 : 0;
        BlockState newBlockState = Blocks.BAMBOO.getDefaultState().with(AGE, age).with(LEAVES, bambooLeaves).with(STAGE, stage);
        world.setBlockState(pos.up(), newBlockState, Block.NOTIFY_ALL);
        return stage == 0 ? newBlockState : null;
    }
}
