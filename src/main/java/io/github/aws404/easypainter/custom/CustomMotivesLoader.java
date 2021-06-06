package io.github.aws404.easypainter.custom;

import io.github.aws404.easypainter.EasyPainter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.Registry;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class CustomMotivesLoader {

    public static final File CONFIG_DIR = new File(FabricLoader.getInstance().getConfigDir().toFile(), "easy_painter");
    static {
        CONFIG_DIR.mkdirs();
    }

    private static int customMotives = 0;

    /**
     * Loads all custom painting motives. <br>
     *
     * When loading a custom motive for the first time the maps are created and all the motive data
     * is stored in a persistent state of the overworld.
     * Once this has been saved, the data can be loaded from the persistent state from then on.
     *
     * @param world the servers overworld
     */
    public static void init(ServerWorld world) {
        for (File file : CONFIG_DIR.listFiles((FilenameFilter) new RegexFileFilter(".*.json"))) {
            String name = file.getName().substring(0, file.getName().indexOf(".json"));
            CustomPaintingState state = world.getPersistentStateManager().getOrCreate(CustomPaintingState::fromNbt, () -> {
                try {
                    return CustomPaintingState.fromFile(file, world);
                } catch (IOException e) {
                    EasyPainter.LOGGER.error("Error loading the custom painting '{}'. Error: ", name);
                    e.printStackTrace();
                }
                return null;
            }, name);

            world.getPersistentStateManager().set(name, state);
            Registry.register(Registry.PAINTING_MOTIVE, name, new CustomMotive(state));
            customMotives++;
        }

        world.getPersistentStateManager().save();

        EasyPainter.LOGGER.info("Loaded {} painting motives! ({} custom motives)", Registry.PAINTING_MOTIVE.getIds().size(), customMotives);
    }

    public static class CustomMotive extends PaintingMotive {

        public final CustomPaintingState state;

        public CustomMotive(CustomPaintingState state) {
            super(state.blockWidth * 16, state.blockHeight * 16);
            this.state = state;
        }

        public ItemStack createMapItem(int x, int y) {
            ItemStack map = new ItemStack(Items.FILLED_MAP);
            map.getOrCreateTag().putInt("map", state.mapIds[x][y]);
            return map;
        }
    }

}
