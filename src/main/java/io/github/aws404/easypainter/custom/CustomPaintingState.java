package io.github.aws404.easypainter.custom;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.aws404.easypainter.EasyPainter;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CustomPaintingState extends PersistentState {

    private static final Gson GSON = new Gson();

    public final Identifier id;
    public final int blockWidth;
    public final int blockHeight;
    public final int[][] mapIds;

    public CustomPaintingState(Identifier id, int blockWidth, int blockHeight, int[][] mapIds) {
        this.id = id;
        this.blockWidth = blockWidth;
        this.blockHeight = blockHeight;
        this.mapIds = mapIds;

        this.markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putString("id", id.toString());
        nbt.putInt("blockWidth", blockWidth);
        nbt.putInt("blockHeight", blockHeight);

        NbtList list = new NbtList();
        for (int[] mapId : mapIds) {
            list.add(new NbtIntArray(mapId));
        }

        nbt.put("mapIds", list);
        return nbt;
    }

    public static CustomPaintingState fromNbt(NbtCompound nbt) {
        int[][] mapIds = new int[nbt.getInt("blockWidth")][nbt.getInt("blockHeight")];
        NbtList list = nbt.getList("mapIds", NbtElement.INT_ARRAY_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtIntArray arr = (NbtIntArray) list.get(i);
            for (int i1 = 0; i1 < arr.size(); i1++) {
                mapIds[i][i1] = arr.get(i1).intValue();
            }
        }

        return new CustomPaintingState(Identifier.tryParse(nbt.getString("id")), nbt.getInt("blockWidth"), nbt.getInt("blockHeight"), mapIds);
    }

    /**
     * Loads a custom painting state from the json file
     * @param file the json data file
     * @param world the servers overworld
     * @return the painting state
     */
    public static CustomPaintingState fromFile(File file, ServerWorld world) throws IOException {
        String id = file.getName().substring(0, file.getName().indexOf(".json"));
        EasyPainter.LOGGER.info("Creating new painting motive '{}' for the first time", id);

        JsonObject data = GSON.fromJson(new FileReader(file), JsonObject.class);

        int blockWidth = data.get("blockWidth").getAsInt();
        int blockHeight = data.get("blockHeight").getAsInt();
        String imageName = data.has("image") ? data.get("image").getAsString() : id;

        BufferedImage image = ImageIO.read(new File(CustomMotivesLoader.CONFIG_DIR, imageName + ".png"));

        if (blockWidth / blockHeight != image.getWidth() / image.getHeight()) {
            throw new IllegalArgumentException("The image's height/width ratio is not the same as the supplied block dimensions");
        }

        int[][] mapIds = new int[blockWidth][blockHeight];

        Image resultingImage = image.getScaledInstance(128 * blockWidth, 128 * blockHeight, Image.SCALE_DEFAULT);
        for (int bW = 0; bW < blockWidth; bW++) {
            for (int bH = 0; bH < blockHeight; bH++) {
                BufferedImage outputImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
                outputImage.getGraphics().drawImage(resultingImage, 0, 0, 128, 128, bW * 128, bH * 128, (bW + 1) * 128, (bH + 1) * 128, null);
                mapIds[bW][bH] = ImageRenderer.renderImageToMap(outputImage, world);
            }
        }

        return new CustomPaintingState(Identifier.tryParse(id), blockWidth, blockHeight, mapIds);
    }
}
