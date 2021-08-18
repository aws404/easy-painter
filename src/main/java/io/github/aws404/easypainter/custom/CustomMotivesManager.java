package io.github.aws404.easypainter.custom;

import com.google.gson.Gson;
import io.github.aws404.easypainter.EasyPainter;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.PersistentStateManager;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomMotivesManager {

    private static final Gson GSON = new Gson();

    private final PersistentStateManager stateManager;

    public CustomMotivesManager(PersistentStateManager stateManager) {
        this.stateManager = stateManager;
    }

    public void reload(ResourceManager manager) {
        MotiveCacheState paintingStorage = MotiveCacheState.getOrCreate(this.stateManager);

        Collection<Identifier> paintings =  manager.findResources("painting", s -> true);

        AtomicInteger id = new AtomicInteger();
        paintings.stream().filter(identifier -> identifier.getPath().contains(".json")).forEach(identifier -> {
            MotiveCacheState.Entry motiveCache = paintingStorage.getOrCreateEntry(identifier, GSON, manager, this.stateManager);
            CustomMotivesManager.registerOrReplace(motiveCache.getId(), new CustomMotive(motiveCache));
            id.incrementAndGet();
        });

        this.stateManager.save();

        EasyPainter.LOGGER.info("Loaded {} painting motives! ({} custom motives)", Registry.PAINTING_MOTIVE.getIds().size(), id.get());
    }

    private static void registerOrReplace(Identifier id, CustomMotive motive) {
        if (Registry.PAINTING_MOTIVE.containsId(id)) {
            EasyPainter.LOGGER.info("Replacing painting motive '{}'. Note that removing motives at reload is not supported.", id);
            Registry.register(Registry.PAINTING_MOTIVE, Registry.PAINTING_MOTIVE.getRawId(Registry.PAINTING_MOTIVE.get(id)), id.toString(), motive);
            return;
        }

        Registry.register(Registry.PAINTING_MOTIVE, id, motive);
    }

    public static class CustomMotive extends PaintingMotive {

        public final MotiveCacheState.Entry state;

        public CustomMotive(MotiveCacheState.Entry state) {
            super(state.blockWidth * 16, state.blockHeight * 16);
            this.state = state;
        }

        public ItemStack createMapItem(int x, int y) {
            ItemStack map = new ItemStack(Items.FILLED_MAP);
            map.getOrCreateNbt().putInt("map", state.mapIds[x][y]);
            return map;
        }
    }

}
