package com.github.inzan17.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.CauldronBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CauldronBlock.class)
public interface CauldronBlockInvoker {
    @Accessor("FILL_WITH_RAIN_CHANCE")
    public static float getRainFIllChance() {
        throw new AssertionError();
    }

    @Accessor("FILL_WITH_SNOW_CHANCE")
    public static float getSnowFIllChance() {
        throw new AssertionError();
    }
}
