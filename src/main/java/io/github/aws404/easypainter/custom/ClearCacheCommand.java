package io.github.aws404.easypainter.custom;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;

import java.io.File;
import java.util.Map;

public class ClearCacheCommand {

    private static final SimpleCommandExceptionType CONFIRMATION_REQUIRED = new SimpleCommandExceptionType(new TranslatableText("commands.easy_painter.clear.confirmation_required"));
    private static final SimpleCommandExceptionType INVALID_MOTIVE = new SimpleCommandExceptionType(new TranslatableText("commands.easy_painter.clear.invalid_motive"));

    private static boolean hasConfirmed = false;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("easy_painter")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .then(CommandManager.literal("clear")
                        .executes(context -> {
                            prepareForClear(context.getSource().getMinecraftServer());

                            for (Map.Entry<RegistryKey<PaintingMotive>, PaintingMotive> entry : Registry.PAINTING_MOTIVE.getEntries()) {
                                if (entry.getValue() instanceof CustomMotivesLoader.CustomMotive) {
                                    clearCache(((CustomMotivesLoader.CustomMotive) entry.getValue()).state, context.getSource().getMinecraftServer());
                                }
                            }

                            context.getSource().getMinecraftServer().getOverworld().getPersistentStateManager().save();
                            return 1;
                        })
                        .then(CommandManager.argument("motive", StringArgumentType.string())
                                .executes(context -> {
                                    prepareForClear(context.getSource().getMinecraftServer());

                                    String id = StringArgumentType.getString(context, "motive");

                                    PaintingMotive motive = Registry.PAINTING_MOTIVE.get(Identifier.tryParse(id));

                                    if (motive instanceof CustomMotivesLoader.CustomMotive) {
                                        clearCache(((CustomMotivesLoader.CustomMotive) motive).state, context.getSource().getMinecraftServer());
                                        context.getSource().getMinecraftServer().getOverworld().getPersistentStateManager().save();

                                        return 1;
                                    }

                                    throw INVALID_MOTIVE.create();
                                })
                        )
                )
        );
    }

    private static void prepareForClear(MinecraftServer server) throws CommandSyntaxException {
        if (!hasConfirmed) {
            hasConfirmed = true;
            throw CONFIRMATION_REQUIRED.create();
        }

        server.save(false, true, false);
        server.getPlayerManager().saveAllPlayerData();
        server.stop(false);
    }

    private static void clearCache(CustomPaintingState state, MinecraftServer server) {
        for (int[] mapId : state.mapIds) {
            for (int i : mapId) {
                server.getOverworld().getPersistentStateManager().set("map_" + i, new MarkedRemovalState());
            }
        }
        server.getOverworld().getPersistentStateManager().set(state.id.getPath(), new MarkedRemovalState());
    }

    private static class MarkedRemovalState extends PersistentState {

        private MarkedRemovalState() {
            this.markDirty();
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt) {
            return null;
        }

        @Override
        public void save(File file) {
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
