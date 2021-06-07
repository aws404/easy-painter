package io.github.aws404.easypainter.custom;

import io.github.aws404.easypainter.EasyPainter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * This is the extension of ItemFrame used for custom painting motives
 */
public class CustomFrameEntity extends ItemFrameEntity {

    public PaintingEntity painting;
    public CustomMotivesManager.CustomMotive motive;

    public CustomFrameEntity(World world, PaintingEntity painting, BlockPos pos, ItemStack stack) {
        super(EasyPainter.CUSTOM_FRAME_ENTITY, world, pos, painting.getHorizontalFacing());
        this.painting = painting;
        this.motive = (CustomMotivesManager.CustomMotive) painting.motive;
        this.setHeldItemStack(stack);
    }

    public CustomFrameEntity(EntityType<CustomFrameEntity> entityType, World world) {
        super(entityType, world);
    }

    private CustomFrameEntity(World world) {
        super(EasyPainter.CUSTOM_FRAME_ENTITY, world);
    }

    @Override
    public int getWidthPixels() {
        return 16;
    }

    @Override
    public int getHeightPixels() {
        return 16;
    }

    @Override
    public void tick() {
        if (this.painting.isRemoved()) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    public void onBreak(@Nullable Entity entity) {
        this.painting.onBreak(entity);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return this.painting.damage(source, amount);
    }

    @Override
    public void onPlace() {
        this.painting.onPlace();
    }

    @Override
    public boolean handleAttack(Entity attacker) {
        return this.painting.handleAttack(attacker);
    }

    @Override
    public ItemEntity dropStack(ItemStack stack, float yOffset) {
        return null;
    }

    @Override
    public Packet<?> createSpawnPacket() {
        if (this.painting.motive instanceof CustomMotivesManager.CustomMotive) {
            return new EntitySpawnS2CPacket(this, EntityType.ITEM_FRAME, this.facing.getId(), this.getDecorationBlockPos());
        }
        return new EntityDestroyS2CPacket(this.getId());
    }

    @Override
    public boolean canStayAttached() {
        return true;
    }

    @Override
    public void kill() {
        this.painting.kill();
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        return this.painting.interact(player, hand);
    }
}
