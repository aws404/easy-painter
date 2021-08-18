package io.github.aws404.easypainter;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class PaintingItem extends DecorationItem {

    public PaintingItem(Settings settings) {
        super(EntityType.PAINTING, settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        MutableText text = (MutableText) super.getName(stack);
        if (stack.getNbt() != null && stack.getNbt().contains("EntityTag")) {
            Identifier current = Identifier.tryParse(stack.getOrCreateSubNbt("EntityTag").getString("Motive"));
            text.append(new TranslatableText("item.easy_painter.painting.set", EasyPainter.getPaintingDisplayName(current).formatted(Formatting.ITALIC)));
        }
        return text.setStyle(text.getStyle().withItalic(text.getStyle().isItalic()));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.isSneaking()) {
            stack.getOrCreateNbt().remove("EntityTag");
        } else if (stack.getNbt() != null && stack.getNbt().contains("EntityTag")) {
            NbtCompound entityTag = stack.getOrCreateSubNbt("EntityTag");
            Identifier current = Identifier.tryParse(entityTag.getString("Motive"));
            int newRaw = Registry.PAINTING_MOTIVE.getRawId(Registry.PAINTING_MOTIVE.get(current)) + 1;
            if (newRaw >= Registry.PAINTING_MOTIVE.getIds().size()) {
                newRaw = 0;
            }

            entityTag.putString("Motive", Registry.PAINTING_MOTIVE.getId(Registry.PAINTING_MOTIVE.get(newRaw)).toString());
        } else {
            NbtCompound entityTag = stack.getOrCreateSubNbt("EntityTag");
            entityTag.putString("Motive", Registry.PAINTING_MOTIVE.getId(PaintingMotive.ALBAN).toString());
        }

        if (!world.isClient) {
            stack.setCustomName(this.getName(stack));
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos blockPos = context.getBlockPos();
        Direction direction = context.getSide();
        BlockPos blockPos2 = blockPos.offset(direction);
        PlayerEntity playerEntity = context.getPlayer();
        ItemStack itemStack = context.getStack();
        if (playerEntity != null && !this.canPlaceOn(playerEntity, direction, itemStack, blockPos2)) {
            return ActionResult.FAIL;
        } else {
            World world = context.getWorld();
            PaintingEntity paintingEntity = new PaintingEntity(world, blockPos2, direction);

            NbtCompound nbtCompound = itemStack.getNbt();
            if (nbtCompound != null) {
                EntityType.loadFromEntityNbt(world, playerEntity, paintingEntity, nbtCompound);
            } else {
                SelectionGui.createGui(paintingEntity, (ServerPlayerEntity) playerEntity).open();
            }

            if (paintingEntity.canStayAttached()) {
                if (!world.isClient) {
                    ((AbstractDecorationEntity)paintingEntity).onPlace();
                    world.emitGameEvent(playerEntity, GameEvent.ENTITY_PLACE, blockPos);
                    world.spawnEntity(paintingEntity);
                }

                itemStack.decrement(1);
                return ActionResult.success(world.isClient);
            } else {
                playerEntity.sendMessage(new TranslatableText("message.easy_painter.painting_cant_fit").formatted(Formatting.RED), true);
                return ActionResult.FAIL;
            }
        }
    }

}
