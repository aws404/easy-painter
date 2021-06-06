package io.github.aws404.easypainter.mixin;

import io.github.aws404.easypainter.EasyPainter;
import io.github.aws404.easypainter.SelectionGui;
import io.github.aws404.easypainter.custom.CustomFrameEntity;
import io.github.aws404.easypainter.custom.CustomMotivesLoader;
import io.github.aws404.easypainter.custom.CustomPaintingState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityDestroyS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PaintingEntity.class)
public abstract class PaintingEntityMixin extends AbstractDecorationEntity {

	@Shadow public abstract int getWidthPixels();
	@Shadow public abstract int getHeightPixels();
	@Shadow public PaintingMotive motive;

	@Unique
	private boolean locked = false;
	private CustomFrameEntity[] customPaintingFrames = new CustomFrameEntity[0];
	private PaintingMotive cachedMotive;

	protected PaintingEntityMixin(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if (!this.locked && player.isSneaking()) {
			SelectionGui.createGui((PaintingEntity) (Object) this, (ServerPlayerEntity) player).open();
		}
		return super.interact(player, hand);
	}

	@Override
	public boolean canStayAttached() {
		return this.locked || EasyPainter.canPaintingAttach((PaintingEntity) (Object) this, this.motive);
	}

	@Override
	public boolean handleAttack(Entity attacker) {
		if (this.locked) {
			return false;
		}
		return super.handleAttack(attacker);
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (this.locked) {
			return false;
		}
		return super.damage(source, amount);
	}

	@Override
	public void tick() {
		if (this.motive != this.cachedMotive) {
			for (CustomFrameEntity customPaintingFrame : this.customPaintingFrames) {
				customPaintingFrame.remove(RemovalReason.DISCARDED);
			}
			this.customPaintingFrames = new CustomFrameEntity[0];
		}

		if (this.motive instanceof CustomMotivesLoader.CustomMotive && this.motive != this.cachedMotive) {
			CustomPaintingState state = ((CustomMotivesLoader.CustomMotive) this.motive).state;
			this.customPaintingFrames = new CustomFrameEntity[state.blockWidth * state.blockHeight];

			int widthBlocks = motive.getWidth() / 16;
			int heightBlocks = motive.getHeight() / 16;

			int attachX = (widthBlocks - 1) / -2;
			int attachY = (heightBlocks - 1) / -2;

			Direction rotated = this.facing.rotateYCounterclockwise();

			int i = 0;
			for (int x = 0; x < widthBlocks; x++) {
				for (int y = 0; y < heightBlocks; y++) {
					BlockPos pos = new BlockPos.Mutable().set(this.attachmentPos).move(rotated, x + attachX).move(Direction.UP, (heightBlocks - y) + attachY - 1);

					ItemStack stack = ((CustomMotivesLoader.CustomMotive) this.motive).createMapItem(x, y);

					CustomFrameEntity entity = new CustomFrameEntity(this.world, (PaintingEntity) (Object) this, pos, stack);
					this.world.spawnEntity(entity);

					this.customPaintingFrames[i++] = entity;
				}
			}
		}

		this.cachedMotive = this.motive;
		super.tick();
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
	private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
		this.locked = nbt.getBoolean("locked");
	}

	@Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
	private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
		nbt.putBoolean("locked", this.locked);
	}

	@Inject(method = "createSpawnPacket", at = @At("HEAD"), cancellable = true)
	private void createSpawnPacket(CallbackInfoReturnable<Packet<?>> cir) {
		if (this.motive instanceof CustomMotivesLoader.CustomMotive) {
			cir.setReturnValue(new EntityDestroyS2CPacket(this.getId()));
		}
	}

	@Inject(method = "onBreak", at = @At("HEAD"))
	private void onBreak(Entity entity, CallbackInfo ci) {
		for (CustomFrameEntity customPaintingFrame : this.customPaintingFrames) {
			customPaintingFrame.remove(RemovalReason.DISCARDED);
		}
	}
}
