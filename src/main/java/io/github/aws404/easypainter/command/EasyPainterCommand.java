package io.github.aws404.easypainter.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.aws404.easypainter.custom.MotiveCacheState;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentStateManager;

public class EasyPainterCommand {

    private static final SimpleCommandExceptionType NOT_PREPARED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("command.easy_painter.clearcache.not_prepared"));

    private static boolean prepared = false;

    public static void register(CommandDispatcher<ServerCommandSource> source) {
        source.register(CommandManager.literal("easy_painter")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(3))
                .then(CommandManager.literal("clearcache")
                    .executes(context -> {
                        EasyPainterCommand.checkPrepared();

                        context.getSource().getMinecraftServer().save(false, true, true);
                        context.getSource().getMinecraftServer().getPlayerManager().saveAllPlayerData();
                        context.getSource().getMinecraftServer().stop(false);

                        PersistentStateManager manager = context.getSource().getMinecraftServer().getOverworld().getPersistentStateManager();
                        MotiveCacheState cache = MotiveCacheState.getOrCreate(manager);

                        for (Identifier key : cache.getKeys()) {
                            cache.removeEntry(manager, key);
                        }

                        manager.save();

                        return 1;
                    })
                )
        );
    }

    private static void checkPrepared() throws CommandSyntaxException {
        if (!prepared) {
            prepared = true;
            throw NOT_PREPARED_EXCEPTION.create();
        }
    }
}
