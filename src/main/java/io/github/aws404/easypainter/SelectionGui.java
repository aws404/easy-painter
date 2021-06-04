package io.github.aws404.easypainter;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.aws404.easypainter.mixin.AbstractDecorationEntityAccessor;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityDestroyS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class SelectionGui extends SimpleGui {

    public SelectionGui(PaintingEntity current, ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);

        List<PaintingMotive> motives = Registry.PAINTING_MOTIVE.stream().filter(motive -> EasyPainter.canPaintingAttach(current, motive)).collect(Collectors.toList());

        for (PaintingMotive possibility : motives) {
            GuiElementBuilder builder = new GuiElementBuilder(Items.PAINTING)
                    .setName(new LiteralText(StringUtils.capitalize(Registry.PAINTING_MOTIVE.getId(possibility).getPath().replace("_", " ")))
                            .formatted(Formatting.YELLOW)
                    )
                    .addLoreLine(new LiteralText("")
                            .append(new LiteralText("- ")
                                    .formatted(Formatting.GOLD)
                            )
                            .append(new LiteralText("Size: ")
                                    .formatted(Formatting.YELLOW)
                            )
                            .append(new LiteralText((possibility.getHeight() / 16) + "x" + (possibility.getWidth() / 16))
                                    .formatted(Formatting.WHITE)
                            )

                    )
                    .setCallback((index, type1, action) -> {
                        this.close();
                        this.changePainting(current, possibility);
                    });

            if (possibility == current.motive) {
                builder.addLoreLine(new LiteralText(""));
                builder.addLoreLine(new LiteralText("Currently Selected").formatted(Formatting.GRAY));
                builder.glow();
            }

            this.addSlot(builder);
        }
    }

    private void changePainting(PaintingEntity entity, PaintingMotive motive) {
        entity.motive = motive;
        ((AbstractDecorationEntityAccessor) entity).callUpdateAttachmentPosition();
        entity.getServer().getPlayerManager().sendToAll(new EntityDestroyS2CPacket(entity.getId()));
        entity.getServer().getPlayerManager().sendToAll(entity.createSpawnPacket());
    }

}
