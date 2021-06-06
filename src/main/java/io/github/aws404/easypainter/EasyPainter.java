package io.github.aws404.easypainter;

import io.github.aws404.easypainter.custom.ClearCacheCommand;
import io.github.aws404.easypainter.custom.CustomFrameEntity;
import io.github.aws404.easypainter.custom.CustomMotivesLoader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EasyPainter implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

	public static Tag<Block> PAINTING_IGNORED = TagRegistry.block(new Identifier("easy_painter:painting_ignored"));
	public static Tag<Block> CANNOT_SUPPORT_PAINTING = TagRegistry.block(new Identifier("easy_painter:cannot_support_painting"));
	public static Tag<EntityType<?>> PAINTING_INTERACT = TagRegistry.entityType(new Identifier("easy_painter:painting_interact"));
	public static EntityType<CustomFrameEntity> CUSTOM_FRAME_ENTITY = registerEntity("frame_entity", CustomFrameEntity::new);

    @Override
    public void onInitialize() {
        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.getRegistryKey() == World.OVERWORLD) {
                CustomMotivesLoader.init(world);
            }
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> ClearCacheCommand.register(dispatcher));
    }

    /**
     * Tests if the painting could fit the supplied motive
     * @param entity the painting entity
     * @param motive the motive to test
     * @return <code>true</code> if the motive will fit
     */
    public static boolean canPaintingAttach(PaintingEntity entity, PaintingMotive motive) {
        Direction facing = entity.getHorizontalFacing();
        Direction rotated = entity.getHorizontalFacing().rotateYCounterclockwise();

        int widthPixels = motive.getWidth();
        int heightPixels = motive.getHeight();
        int widthBlocks = widthPixels / 16;
        int heightBlocks = heightPixels / 16;

        int attachX = (widthBlocks - 1) / -2;
        int attachY = (heightBlocks - 1) / -2;

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = 0; x < widthBlocks; x++) {
            for (int y = 0; y < heightBlocks; y++) {
                mutable.set(entity.getDecorationBlockPos()).move(rotated, x + attachX).move(Direction.UP, y + attachY);
                BlockState inside = entity.world.getBlockState(mutable);

                mutable.move(facing.getOpposite());
                BlockState behind = entity.world.getBlockState(mutable);

                if (!inside.isIn(PAINTING_IGNORED) || behind.isIn(CANNOT_SUPPORT_PAINTING)) {
                    return false;
                }
            }
        }

        return entity.world.getOtherEntities(entity, getBoundingBox(entity, motive, facing, rotated, widthPixels, heightPixels), entity1 -> entity1.getType().isIn(PAINTING_INTERACT)).isEmpty();
    }

    private static Box getBoundingBox(PaintingEntity entity, PaintingMotive motive, Direction facing, Direction rotated, int widthPixels, int heightPixels) {
        if (entity.motive == motive) {
            return entity.getBoundingBox();
        } else {
            double widthOffset = getOffset(widthPixels);
            double startX = (double) entity.getDecorationBlockPos().getX() + 0.5D - facing.getOffsetX() * 0.46875D + widthOffset * rotated.getOffsetX();
            double startY = (double) entity.getDecorationBlockPos().getY() + 0.5D + getOffset(heightPixels);
            double startZ = (double) entity.getDecorationBlockPos().getZ() + 0.5D - facing.getOffsetZ() * 0.46875D + widthOffset * rotated.getOffsetZ();

            double boxX = (facing.getAxis() == Direction.Axis.Z ? widthPixels : 1) / 32.0;
            double boxHeight = heightPixels / 32.0;
            double boxZ = (facing.getAxis() == Direction.Axis.X ? widthPixels : 1) / 32.0;

            return new Box(startX - boxX, startY - boxHeight, startZ - boxZ, startX + boxX, startY + boxHeight, startZ + boxZ);
        }

    }

    private static double getOffset(int i) {
        return i % 32 == 0 ? 0.5D : 0.0D;
    }

    private static <T extends Entity> EntityType<T> registerEntity(String id, EntityType.EntityFactory<T> factory) {
        return Registry.register(
                Registry.ENTITY_TYPE,
                "easy_painter:" + id,
                FabricEntityTypeBuilder.create(SpawnGroup.MISC, factory).disableSaving().disableSummon().build()
        );
    }

}
