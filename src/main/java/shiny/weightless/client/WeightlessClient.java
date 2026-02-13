package shiny.weightless.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import shiny.weightless.client.util.FlyingPlayerTracker;
import shiny.weightless.common.Weightless;
import shiny.weightless.common.component.WeightlessComponent;
import shiny.weightless.common.network.CompareConfigMatchPayload;
import shiny.weightless.common.network.FlyingSoundPayload;

import java.util.UUID;

public class WeightlessClient implements ClientModInitializer {

    //Me!
    public static final UUID SHINY_UUID = UUID.fromString("a9bcfe9b-bb80-463d-848e-11e0b03f2b6e");

    //Keybinds
    public static final KeyMapping.Category MAIN_CATEGORY = KeyMapping.Category.register(Weightless.id("weightless"));
    public static final KeyMapping TOGGLE_WEIGHTLESS = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.weightless.toggle", -1, MAIN_CATEGORY));
    public static final KeyMapping AUTOPILOT = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.weightless.autopilot", -1, MAIN_CATEGORY));

    //Variables used for keybind toggling (Vanilla ToggleKeyMapping did not work as well)
    private static boolean wasWeightlessPressed = false;
    private static boolean wasAutopilotPressed = false;

    //Disconnect message for config mismatch
    public static final Component DISCONNECT_MESSAGE = Component.translatable("message.weightless.disconnect");

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(FlyingSoundPayload.TYPE, new FlyingSoundPayload.Handler());
        ClientPlayNetworking.registerGlobalReceiver(CompareConfigMatchPayload.TYPE, new CompareConfigMatchPayload.Handler());

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (TOGGLE_WEIGHTLESS.isDown() && !wasWeightlessPressed) {
                WeightlessComponent.clientToggled = !WeightlessComponent.clientToggled;
                wasWeightlessPressed = true;
                TOGGLE_WEIGHTLESS.setDown(false);
            }
            else if (wasWeightlessPressed) {
                wasWeightlessPressed = false;
            }

            if (WeightlessComponent.clientToggled) {
                if (AUTOPILOT.isDown() && !wasAutopilotPressed) {
                    WeightlessComponent.clientAutopilot = !WeightlessComponent.clientAutopilot;
                    wasAutopilotPressed = true;
                    AUTOPILOT.setDown(false);
                } else if (wasAutopilotPressed) {
                    wasAutopilotPressed = false;
                }
            }
            else if (WeightlessComponent.clientAutopilot) {
                WeightlessComponent.clientAutopilot = false;
            }
        });
        WeightlessComponent.init();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level != null) {
                FlyingPlayerTracker.update(client);
            }
        });
    }
}