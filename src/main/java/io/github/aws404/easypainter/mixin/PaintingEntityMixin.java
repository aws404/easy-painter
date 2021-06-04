package io.github.aws404.easypainter.mixin;

import io.github.aws404.easypainter.EasyPainter;
import io.github.aws404.easypainter.SelectionGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PaintingEntity.class)
public abstract class PaintingEntityMixin extends AbstractDecorationEntity {

	@Shadow public abstract int getWidthPixels();
	@Shadow public abstract int getHeightPixels();
	@Shadow public PaintingMotive motive;

	@Unique
	private boolean locked = false;

	protected PaintingEntityMixin(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if (!this.locked && player.isSneaking()) {
			new SelectionGui((PaintingEntity) (Object) this, (ServerPlayerEntity) player).open();
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

	@Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
	private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
		this.locked = nbt.getBoolean("locked");
	}

	@Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
	private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
		nbt.putBoolean("locked", this.locked);
	}

}
