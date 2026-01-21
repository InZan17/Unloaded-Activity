package lol.zanspace.unloadedactivity.fabric.mixin;

import lol.zanspace.unloadedactivity.UnloadedActivity;
import com.mojang.brigadier.CommandDispatcher;
import lol.zanspace.unloadedactivity.UnloadedActivityCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public abstract class CommandManagerMixin {
    @Shadow
    @Final
    private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;setConsumer(Lcom/mojang/brigadier/ResultConsumer;)V", remap = false), method = "<init>")
    private void addCommands(Commands.CommandSelection commandSelection, CommandBuildContext commandBuildContext, CallbackInfo ci) {
        UnloadedActivityCommand.register(this.dispatcher);
    }
}
