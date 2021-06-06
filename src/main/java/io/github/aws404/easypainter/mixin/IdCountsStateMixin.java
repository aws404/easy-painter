package io.github.aws404.easypainter.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.IdCountsState;
import net.minecraft.world.PersistentState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IdCountsState.class)
public abstract class IdCountsStateMixin extends PersistentState {

    @Shadow @Final private Object2IntMap<String> idCounts;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        idCounts.put("painting", 10000);
    }

    /**
     * Returns the next map id to use for paintings
     * @return the next painting map id
     */
    private int getNextPaintingCount() {
        int i = this.idCounts.getInt("painting") + 1;
        this.idCounts.put("painting", i);
        this.markDirty();
        return i;
    }
}
