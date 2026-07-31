package com.seamoth.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class SeamothKeybindings {
    public static final KeyMapping.Category SEAMOTH_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("seamoth", "main"));

    public static final KeyMapping KEY_UP = new KeyMapping(
            "key.seamoth.up",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_SPACE,
            SEAMOTH_CATEGORY
    );

    public static final KeyMapping KEY_DOWN = new KeyMapping(
            "key.seamoth.down",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            SEAMOTH_CATEGORY
    );
}
