package shiny.weightless.common.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import shiny.weightless.common.component.WeightlessComponent;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class WeightlessCommand {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, commandRegistryAccess, registrationEnvironment) -> {
            dispatcher.register(literal("weightless")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(argument("player", EntityArgumentType.player())
                            .then(argument("operation", StringArgumentType.string())
                                    .suggests((ctx, builder) -> CommandSource.suggestMatching(addOrRevoke(), builder))
                                    .executes(ctx -> {
                                        boolean bl = StringArgumentType.getString(ctx, "operation").equals("grant");
                                        return execute(EntityArgumentType.getPlayer(ctx, "player"), bl);
                                    })
                            )
                    )
            );
        });
    }

    public static int execute(ServerPlayerEntity player, boolean add) {
        if (player != null) {
            if (add) WeightlessComponent.get(player).attain();
            else WeightlessComponent.get(player).reset();
            return 1;
        }
        return 0;
    }

    private static List<String> addOrRevoke() {
        List<String> strings = new ArrayList<>();
        strings.add("grant");
        strings.add("revoke");
        return strings;
    }
}
