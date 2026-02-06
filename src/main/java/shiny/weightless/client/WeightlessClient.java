package shiny.weightless.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import shiny.weightless.common.util.FlyingPlayerTracker;
import shiny.weightless.common.Weightless;
import shiny.weightless.common.component.WeightlessComponent;
import shiny.weightless.common.network.CompareConfigMatchPayload;
import shiny.weightless.common.network.FlyingSoundPayload;

public class WeightlessClient implements ClientModInitializer {

    //Keybinds
    public static KeyMapping.Category MAIN_CATEGORY = KeyMapping.Category.register(Weightless.id("weightless"));
    public static KeyMapping TOGGLE_WEIGHTLESS = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.weightless.toggle", -1, MAIN_CATEGORY));
    public static KeyMapping AUTOPILOT = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.weightless.autopilot", -1, MAIN_CATEGORY));

    //Client-side global key variables  //TODO (Should maybe make this better..?)
    public static boolean wasWeightlessPressed = false;
    public static boolean wasAutopilotPressed = false;
    public static boolean weightlessActive = true; //TODO Make this update to the value of the component when joining a world
    public static boolean autopilotActive = false;

    //Disconnect message for config mismatch
    public static final Component DISCONNECT_MESSAGE = Component.translatable("message.weightless.disconnect");

    //TODO Add speed lines shader
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(FlyingSoundPayload.TYPE, new FlyingSoundPayload.Handler());
        ClientPlayNetworking.registerGlobalReceiver(CompareConfigMatchPayload.TYPE, new CompareConfigMatchPayload.Handler());

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (TOGGLE_WEIGHTLESS.isDown() && !wasWeightlessPressed) {
                weightlessActive = !weightlessActive;
                wasWeightlessPressed = true;
                TOGGLE_WEIGHTLESS.setDown(false);
            }
            else if (wasWeightlessPressed) {
                wasWeightlessPressed = false;
            }

            if (weightlessActive) {
                if (AUTOPILOT.isDown() && !wasAutopilotPressed) {
                    autopilotActive = !autopilotActive;
                    wasAutopilotPressed = true;
                    AUTOPILOT.setDown(false);
                } else if (wasAutopilotPressed) {
                    wasAutopilotPressed = false;
                }
            }
            else if (autopilotActive) {
                autopilotActive = false;
            }
            WeightlessComponent.clientTick(client);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level != null) {
                FlyingPlayerTracker.update(client);
            }
        });
    }
}