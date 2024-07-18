package lol.zanspace.unloadedactivity.mixin.chunk.randomTicks;


import lol.zanspace.unloadedactivity.ExpectPlatform;
import lol.zanspace.unloadedactivity.OccurrencesAndLeftover;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.mixin.CropBlockInvoker;
import net.minecraft.block.*;

#if MC_VER >= MC_1_20_4
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
#endif
#if MC_VER >= MC_1_19_4
import net.minecraft.registry.tag.BlockTags;
#else
import net.minecraft.tag.BlockTags;
#endif
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Optional;

import static java.lang.Math.min;

@Mixin(StemBlock.class)
public abstract class StemMixin extends PlantBlock {

    #if MC_VER >= MC_1_20_4
    public StemMixin(Settings settings) {
        super(settings);
    }
    #else
    public StemMixin(Settings settings, GourdBlock gourdBlock) {
        super(settings);
        this.gourdBlock = gourdBlock;
    }
    #endif

    @Shadow
    public static IntProperty AGE;

    #if MC_VER >= MC_1_20_4
    @Shadow @Final
    private RegistryKey<Block> gourdBlock;
    @Shadow @Final
    private RegistryKey<Block> attachedStemBlock;
    #else
    @Shadow
    private final GourdBlock gourdBlock;
    #endif

    @Override public int getCurrentAgeUA(BlockState state) {
        return state.get(AGE);
    }

    @Override public int getMaxAgeUA() {
        return 7;
    }

    @Override
    public double getOdds(ServerWorld world, BlockPos pos) {
        #if MC_VER >= MC_1_21
        float f = ExpectPlatform.getAvailableMoisture(world.getBlockState(pos), world, pos);
        #else
        float f = CropBlockInvoker.getAvailableMoisture(this, world, pos);
        #endif
        return 1.0/(double)((int)(25.0F / f) + 1);
    }
    @Override
    public boolean implementsSimulateRandTicks() {return true;}
    @Override public boolean canSimulateRandTicks(BlockState state, ServerWorld world, BlockPos pos) {
        if (!UnloadedActivity.config.growStems) return false;
        if (world.getBaseLightLevel(pos, 0) < 9) return false;
        if (NumOfValidPositions(pos, world) == 0 && this.getCurrentAgeUA(state) == this.getMaxAgeUA()) return false;
        return true;
    }

    @Override
    public void simulateRandTicks(BlockState state, ServerWorld world, BlockPos pos, Random random, long timePassed, int randomTickSpeed) {

        int currentAge = this.getCurrentAgeUA(state);
        int maxAge = this.getMaxAgeUA();
        int ageDifference = maxAge - currentAge;

        int validPositions = NumOfValidPositions(pos, world);

        double randomPickChance = Utils.getRandomPickOdds(randomTickSpeed);

        double randomGrowChance = getOdds(world, pos); //chance to grow for every pick

        double totalOdds = randomPickChance * randomGrowChance;

        OccurrencesAndLeftover oal = Utils.getOccurrencesAndLeftoverTicks(timePassed, totalOdds, ageDifference + min(1, validPositions), random);

        int growthAmount = oal.occurrences;
        long leftover = oal.leftover;

        if (growthAmount != 0) {
            state = state.with(AGE, min(currentAge + growthAmount, maxAge));
            world.setBlockState(pos, state, 2);
        }

        if (currentAge + growthAmount > maxAge && validPositions != 0) { // it surpasses the max age of crop and has space, try to grow fruit

            double chanceForFreeSpace = 0.25 * validPositions;
            int growsFruit = Utils.getOccurrences(leftover, chanceForFreeSpace, 1, random);

            if (growsFruit == 0)
                return;

            List<Direction> directions = Direction.Type.HORIZONTAL.getShuffled(random);

            for (int i = 0; i < directions.size(); i++) {

                Direction direction = directions.get(i);

                if (!isValidPosition(direction, pos, world)) continue;

                BlockPos blockPos = pos.offset(direction);
                #if MC_VER >= MC_1_20_4
                Registry<Block> blockRegistry = world.getRegistryManager().get(RegistryKeys.BLOCK);
                Optional<Block> gourdBlock = blockRegistry.getOrEmpty(this.gourdBlock);
                Optional<Block> attachedStemBlock = blockRegistry.getOrEmpty(this.attachedStemBlock);

                if (gourdBlock.isPresent() && attachedStemBlock.isPresent()) {
                    world.setBlockState(blockPos, gourdBlock.get().getDefaultState());
                    world.setBlockState(pos, attachedStemBlock.get().getDefaultState().with(HorizontalFacingBlock.FACING, direction));
                }
                #else
                world.setBlockState(blockPos, this.gourdBlock.getDefaultState());

                state = this.gourdBlock.getAttachedStem().getDefaultState().with(HorizontalFacingBlock.FACING, direction);
                world.setBlockState(pos, state);
                #endif
                break;
            }
        }
    }

    public int NumOfValidPositions(BlockPos pos, ServerWorld world) {
        return (isValidPosition(Direction.NORTH, pos, world) ? 1 : 0)
             + (isValidPosition(Direction.EAST, pos, world) ? 1 : 0)
             + (isValidPosition(Direction.SOUTH, pos, world) ? 1 : 0)
             + (isValidPosition(Direction.WEST, pos, world) ? 1 : 0);
    }

    public boolean isValidPosition(Direction direction, BlockPos pos, ServerWorld world) {
        BlockPos blockPos = pos.offset(direction);
        BlockState blockState = world.getBlockState(blockPos.down());
        return world.getBlockState(blockPos).isAir() && (blockState.isOf(Blocks.FARMLAND) || blockState.isIn(BlockTags.DIRT));
    }
}
