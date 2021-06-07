package io.github.aws404.easypainter;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.aws404.easypainter.mixin.AbstractDecorationEntityAccessor;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityDestroyS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SelectionGui extends SimpleGui {

    private final PaintingEntity entity;

    private SelectionGui(PaintingEntity entity, List<PaintingMotive> motives, ServerPlayerEntity player) {
        super(SelectionGui.getHandlerFromItems(motives.size()), player, false);
        this.entity = entity;
        this.setTitle(new TranslatableText("screen.easy_painter.title"));

        for (PaintingMotive possibility : motives) {
            GuiElementBuilder builder = new GuiElementBuilder(Items.PAINTING)
                    .setName(EasyPainter.getPaintingDisplayName(Registry.PAINTING_MOTIVE.getId(possibility))
                            .formatted(Formatting.YELLOW)
                    )
                    .addLoreLine(new LiteralText("")
                            .append(new TranslatableText("screen.easy_painter.bullet").formatted(Formatting.GOLD))
                            .append(new TranslatableText("screen.easy_painter.size",
                                    new TranslatableText("screen.easy_painter.size.data", possibility.getHeight() / 16, possibility.getWidth() / 16).formatted(Formatting.WHITE)
                            ).formatted(Formatting.YELLOW))
                    )
                    .setCallback((index, type1, action) -> {
                        this.close();
                        this.changePainting(possibility);
                    });

            if (possibility == entity.motive) {
                builder.addLoreLine(new LiteralText(""));
                builder.addLoreLine(new TranslatableText("screen.easy_painter.currently_selected").formatted(Formatting.GRAY));
                builder.glow();
            }

            this.addSlot(builder);
        }
    }

    private void changePainting(PaintingMotive motive) {
        this.entity.motive = motive;
        ((AbstractDecorationEntityAccessor) this.entity).callUpdateAttachmentPosition();
        this.entity.getServer().getPlayerManager().sendToAll(new EntityDestroyS2CPacket(this.entity.getId()));
        Packet<?> packet = this.entity.createSpawnPacket();
        if (packet != null) {
            this.entity.getServer().getPlayerManager().sendToAll(packet);
        }
    }

    public static SelectionGui createGui(PaintingEntity entity, ServerPlayerEntity player) {
        List<PaintingMotive> motives = Registry.PAINTING_MOTIVE.stream().filter(motive -> EasyPainter.canPaintingAttach(entity, motive)).sorted(Comparator.comparingInt(o -> o.getHeight() * o.getWidth())).collect(Collectors.toList());
        return new SelectionGui(entity, motives, player);
    }

    private static ScreenHandlerType<?> getHandlerFromItems(int count) {
        return switch ((int) Math.ceil(count / 9.0)) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            default -> ScreenHandlerType.GENERIC_9X6;
        };
    }

}
