package io.github.aws404.easypainter.mixin;

import net.minecraft.entity.decoration.AbstractDecorationEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractDecorationEntity.class)
public interface AbstractDecorationEntityAccessor {
    @Invoker
    void callUpdateAttachmentPosition();
}
