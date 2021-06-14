package io.github.aws404.easypainter;

import fr.catcore.server.translations.api.ServerTranslations;
import io.github.aws404.easypainter.command.EasyPainterCommand;
import io.github.aws404.easypainter.custom.CustomFrameEntity;
import io.github.aws404.easypainter.custom.CustomMotivesManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.tag.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EasyPainter implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger();

	public static final Tag<Block> PAINTING_IGNORED = TagRegistry.block(new Identifier("easy_painter:painting_ignored"));
	public static final Tag<Block> CANNOT_SUPPORT_PAINTING = TagRegistry.block(new Identifier("easy_painter:cannot_support_painting"));
	public static final Tag<EntityType<?>> PAINTING_INTERACT = TagRegistry.entityType(new Identifier("easy_painter:painting_interact"));
	public static final EntityType<CustomFrameEntity> CUSTOM_FRAME_ENTITY = registerEntity("frame_entity", CustomFrameEntity::new);
    public static final PaintingItem PAINTING_ITEM_OVERRIDE = Registry.register(Registry.ITEM, Registry.ITEM.getRawId(Items.PAINTING), "painting", new PaintingItem(new FabricItemSettings().group(ItemGroup.DECORATIONS)));

	public static CustomMotivesManager customMotivesManager;

    @Override
    public void onInitialize() {
        LOGGER.info("Starting Easy Painter (Version {})", FabricLoader.getInstance().getModContainer("easy_painter").orElseThrow(() -> new IllegalStateException("initialising unloaded mod")).getMetadata().getVersion().getFriendlyString());

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> EasyPainterCommand.register(dispatcher));

        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.getRegistryKey() == World.OVERWORLD) {
                EasyPainter.customMotivesManager = new CustomMotivesManager(world.getPersistentStateManager());
                EasyPainter.customMotivesManager.reload(server.getResourceManager());
            }
        });
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

    public static MutableText getPaintingDisplayName(Identifier id) {
        String translationKey = Util.createTranslationKey("painting", id);
        if (ServerTranslations.INSTANCE.getDefaultLanguage().local().contains(translationKey)) {
            return new TranslatableText(translationKey);
        }

        return new LiteralText(StringUtils.capitalize(id.getPath().replace("_", " ")));
    }

    private static double getOffset(int i) {
        return i % 32 == 0 ? 0.5D : 0.0D;
    }

    private static <T extends Entity> EntityType<T> registerEntity(String id, EntityType.EntityFactory<T> factory) {
        return Registry.register(
                Registry.ENTITY_TYPE,
                "easy_painter:" + id,
                FabricEntityTypeBuilder.create(SpawnGroup.MISC, factory).disableSaving().disableSummon().fireImmune().build()
        );
    }

}
