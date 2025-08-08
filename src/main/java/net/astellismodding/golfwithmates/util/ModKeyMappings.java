package net.astellismodding.golfwithmates.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

public class ModKeyMappings {
    public static final Lazy<KeyMapping> Golf_PowerUP = Lazy.of(() -> new KeyMapping(
            "key.golfwithmates.golfpowerup",
            KeyConflictContext.IN_GAME,
            KeyModifier.SHIFT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UP,
            "key.categories.golfwithmates.golf"
    ));
    public static final Lazy<KeyMapping> Golf_PowerDOWN = Lazy.of(() -> new KeyMapping(
            "key.golfwithmates.golfpowerdown",
            KeyConflictContext.IN_GAME,
            KeyModifier.SHIFT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_DOWN,
            "key.categories.golfwithmates.golf"
    ));

}
