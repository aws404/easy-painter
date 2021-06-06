package io.github.aws404.easypainter.mixin;

import net.minecraft.world.IdCountsState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(IdCountsState.class)
public interface IdCountsStateAccessor {
    @Invoker
    int invokeGetNextPaintingCount();
}
