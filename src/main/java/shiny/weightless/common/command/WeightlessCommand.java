package shiny.weightless.common.command;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;
import shiny.weightless.common.component.WeightlessComponent;

public class WeightlessCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, commandRegistryAccess, registrationEnvironment) -> {
            dispatcher.register(Commands.literal("weightless")
                    .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                    .then(Commands.argument("player", EntityArgument.players())
                            .executes(context -> query(context, EntityArgument.getPlayer(context, "player")))
                            .then(Commands.literal("grant")
                                    .executes(context -> execute(EntityArgument.getPlayer(context, "player"), true))
                            )
                            .then(Commands.literal("revoke")
                                    .executes(context -> execute(EntityArgument.getPlayer(context, "player"), false))
                            )
                    )
            );
        });
    }

    private static int query(CommandContext<CommandSourceStack> context, Player player) {
        if (player != null) {
            boolean bl = WeightlessComponent.has(player);
            if (bl) {
                context.getSource().sendSuccess(() -> Component.translatable("commands.weightless.query.has", player.getName()), bl);
                return 1;
            }
            else {
                context.getSource().sendSuccess(() -> Component.translatable("commands.weightless.query.has_not", player.getName()), bl);
            }

        }
        return 0;
    }

    private static int execute(Player player, boolean grant) {
        if (player != null) {
            if (grant) WeightlessComponent.get(player).attain();
            else WeightlessComponent.get(player).reset();
            return 1;
        }
        return 0;
    }
}
