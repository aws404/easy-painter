package io.github.aws404.easypainter.mixin;

import io.github.aws404.easypainter.SelectionGui;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(DecorationItem.class)
public abstract class DecorationItemMixin {

    @Inject(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/AbstractDecorationEntity;onPlace()V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir, BlockPos blockPos, Direction direction, BlockPos blockPos2, PlayerEntity playerEntity, ItemStack itemStack, World world, AbstractDecorationEntity decorationEntity) {
        if (decorationEntity instanceof PaintingEntity) {
            SelectionGui.createGui((PaintingEntity) decorationEntity, (ServerPlayerEntity) playerEntity).open();
        }
    }
}
