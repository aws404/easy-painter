package io.github.aws404.easypainter.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.IdCountsState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IdCountsState.class)
public interface IdCountsStateAccessor {
    @Accessor
    Object2IntMap<String> getIdCounts();
}
