package io.github.aws404.easypainter.custom;

import io.github.aws404.easypainter.mixin.MapStateAccessor;
import net.minecraft.block.MapColor;
import net.minecraft.item.map.MapState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Arrays;
import java.util.Objects;

/**
 * Credit to the Image2Map Mod for the base of this code!
 * @author TheEssem
 * @see <a href="https://github.com/TheEssem/Image2Map/blob/master/src/main/java/space/essem/image2map/renderer/MapRenderer.java">Original Source</a>
 */
public class ImageRenderer {
    private static final double[] shadeCoeffs = {0.71, 0.86, 1.0, 0.53};

    public static int renderImageToMap(BufferedImage image, DitherMode mode, PersistentStateManager stateManager) {
        MapState state = MapStateAccessor.createMapState(0, 0, (byte) 3, false, false, true, World.OVERWORLD);

        Image resizedImage = image.getScaledInstance(128, 128, Image.SCALE_DEFAULT);
        BufferedImage resized = convertToBufferedImage(resizedImage);
        int width = resized.getWidth();
        int height = resized.getHeight();
        int[][] pixels = convertPixelArray(resized);
        MapColor[] mapColors = MapColor.COLORS;
        Color imageColor;
        mapColors = Arrays.stream(mapColors).filter(Objects::nonNull).toArray(MapColor[]::new);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                imageColor = new Color(pixels[j][i], true);
                if (mode.equals(DitherMode.FLOYD))
                    state.colors[i + j * width] = (byte) floydDither(mapColors, pixels, i, j, imageColor);
                else
                    state.colors[i + j * width] = (byte) nearestColor(mapColors, imageColor);
                state.colors[i + j * width] = (byte) nearestColor(mapColors, imageColor);
            }
        }

        int stateId = getNextPaintingId(stateManager);
        stateManager.set("map_" + stateId, state);
        return stateId;
    }

    private static double distance(double[] vectorA, double[] vectorB) {
        return Math.sqrt(Math.pow(vectorA[0] - vectorB[0], 2) + Math.pow(vectorA[1] - vectorB[1], 2)
                + Math.pow(vectorA[2] - vectorB[2], 2));
    }

    private static double[] applyShade(double[] color, int ind) {
        double coeff = shadeCoeffs[ind];
        return new double[] { color[0] * coeff, color[1] * coeff, color[2] * coeff };
    }

    private static int getNextPaintingId(PersistentStateManager stateManager) {
        return MotiveCacheState.getOrCreate(stateManager).getNextMapId();
    }
    private static Color mapColorToRGBColor(MapColor[] colors, int color) {
        Color mcColor = new Color(colors[color >> 2].color);
        double[] mcColorVec = {
                (double) mcColor.getRed(),
                (double) mcColor.getGreen(),
                (double) mcColor.getBlue()
        };
        double coeff = shadeCoeffs[color & 3];
        return new Color((int)(mcColorVec[0] * coeff), (int)(mcColorVec[1] * coeff), (int)(mcColorVec[2] * coeff));
    }

    private static class NegatableColor {
        public final int r;
        public final int g;
        public final int b;
        public NegatableColor(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }
    private static int floydDither(MapColor[] mapColors, int[][] pixels, int x, int y, Color imageColor) {
        // double[] imageVec = { (double) imageColor.getRed() / 255.0, (double) imageColor.getGreen() / 255.0,
        //         (double) imageColor.getBlue() / 255.0 };
        int colorIndex = nearestColor(mapColors, imageColor);
        Color palletedColor = mapColorToRGBColor(mapColors, colorIndex);
        NegatableColor error = new NegatableColor(
                imageColor.getRed() - palletedColor.getRed(),
                imageColor.getGreen() - palletedColor.getGreen(),
                imageColor.getBlue() - palletedColor.getBlue());
        if (pixels[0].length > x + 1) {
            Color pixelColor = new Color(pixels[y][x + 1], true);
            pixels[y][x + 1] = applyError(pixelColor, error, 7.0 / 16.0);
        }
        if (pixels.length > y + 1) {
            if (x > 0) {
                Color pixelColor = new Color(pixels[y + 1][x - 1], true);
                pixels[y + 1][x - 1] = applyError(pixelColor, error, 3.0 / 16.0);
            }
            Color pixelColor = new Color(pixels[y + 1][x], true);
            pixels[y + 1][x] = applyError(pixelColor, error, 5.0 / 16.0);
            if (pixels[0].length > x + 1) {
                pixelColor = new Color(pixels[y + 1][x + 1], true);
                pixels[y + 1][x + 1] = applyError(pixelColor, error, 1.0 / 16.0);
            }
        }


        return colorIndex;
    }

    private static int applyError(Color pixelColor, NegatableColor error, double quantConst) {
        int pR = MathHelper.clamp(pixelColor.getRed() + (int)((double)error.r * quantConst), 0, 255);
        int pG = MathHelper.clamp(pixelColor.getGreen() + (int)((double)error.g * quantConst), 0, 255);
        int pB = MathHelper.clamp(pixelColor.getBlue() + (int)((double)error.b * quantConst), 0, 255);
        return new Color(pR, pG, pB, pixelColor.getAlpha()).getRGB();
    }

    private static int nearestColor(MapColor[] colors, Color imageColor) {
        double[] imageVec = { (double) imageColor.getRed() / 255.0, (double) imageColor.getGreen() / 255.0,
                (double) imageColor.getBlue() / 255.0 };
        int best_color = 0;
        double lowest_distance = 10000;
        for (int k = 0; k < colors.length; k++) {
            Color mcColor = new Color(colors[k].color);
            double[] mcColorVec = { (double) mcColor.getRed() / 255.0, (double) mcColor.getGreen() / 255.0,
                    (double) mcColor.getBlue() / 255.0 };
            for (int shadeInd = 0; shadeInd < shadeCoeffs.length; shadeInd++) {
                double distance = distance(imageVec, applyShade(mcColorVec, shadeInd));
                if (distance < lowest_distance) {
                    lowest_distance = distance;
                    if (k == 0 && imageColor.getAlpha() == 255) {
                        best_color = 119;
                    } else {
                        best_color = k * shadeCoeffs.length + shadeInd;
                    }
                }
            }
        }
        return best_color;
    }

    private static int[][] convertPixelArray(BufferedImage image) {

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();

        int[][] result = new int[height][width];
        final int pixelLength = 4;
        for (int pixel = 0, row = 0, col = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
            int argb = 0;
            argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
            argb += ((int) pixels[pixel + 1] & 0xff); // blue
            argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
            argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
            result[row][col] = argb;
            col++;
            if (col == width) {
                col = 0;
                row++;
            }
        }

        return result;
    }

    private static BufferedImage convertToBufferedImage(Image image) {
        BufferedImage newImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return newImage;
    }

    public enum DitherMode {
        NONE,
        FLOYD;

        public static DitherMode fromString(String string) {
            if (string.equalsIgnoreCase("NONE"))
                return DitherMode.NONE;
            else if (string.equalsIgnoreCase("DITHER") || string.equalsIgnoreCase("FLOYD"))
                return DitherMode.FLOYD;
            throw new IllegalArgumentException("invalid dither mode");
        }
    }
}