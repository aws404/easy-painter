package io.github.aws404.easypainter.mixin;

import io.github.aws404.easypainter.SelectionGui;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DecorationItem.class)
public abstract class DecorationItemMixin {

    @Shadow @Final private EntityType<? extends AbstractDecorationEntity> entityType;

    @Shadow protected abstract boolean canPlaceOn(PlayerEntity player, Direction side, ItemStack stack, BlockPos pos);

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (this.entityType == EntityType.PAINTING) {
            BlockPos blockPos = context.getBlockPos();
            Direction direction = context.getSide();
            BlockPos blockPos2 = blockPos.offset(direction);
            PlayerEntity playerEntity = context.getPlayer();
            ItemStack itemStack = context.getStack();
            if (playerEntity != null && !this.canPlaceOn(playerEntity, direction, itemStack, blockPos2)) {
                cir.setReturnValue(ActionResult.FAIL);
            } else {
                World world = context.getWorld();
                PaintingEntity painting = new PaintingEntity(world, blockPos2, direction);

                NbtCompound compoundTag = itemStack.getTag();
                if (compoundTag != null) {
                    EntityType.loadFromEntityNbt(world, playerEntity, painting, compoundTag);
                }

                if (painting.canStayAttached()) {
                    painting.onPlace();
                    world.spawnEntity(painting);

                    itemStack.decrement(1);

                    new SelectionGui(painting, (ServerPlayerEntity) playerEntity).open();

                    cir.setReturnValue(ActionResult.success(world.isClient));
                } else {
                    cir.setReturnValue(ActionResult.CONSUME);
                }
            }
        }
    }
}
