package dev.akvisuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class AKvisuals implements ClientModInitializer {
    public static KeyBinding MENU_KEY;

    @Override
    public void onInitializeClient() {
        Config.load();

        // rebindable in Options > Controls > AKvisuals
        MENU_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.akvisuals.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.create(Identifier.of("akvisuals", "main")) // FILL: if Category API differs on 1.21.11
        ));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (MENU_KEY.wasPressed()) {
                if (mc.currentScreen instanceof AKMenuScreen) {
                    mc.setScreen(null);
                } else if (mc.currentScreen == null) {
                    mc.setScreen(new AKMenuScreen());
                }
            }
            KillAura.tick(mc);
        });

        WorldRenderEvents.AFTER_ENTITIES.register(ChinaHat::render);
    }
}
