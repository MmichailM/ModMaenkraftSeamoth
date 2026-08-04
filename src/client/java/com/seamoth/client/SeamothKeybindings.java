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

    public static final KeyMapping KEY_FORWARD = new KeyMapping(
            "key.seamoth.forward",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_W,
            SEAMOTH_CATEGORY
    );

    public static final KeyMapping KEY_BACKWARD = new KeyMapping(
            "key.seamoth.backward",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_S,
            SEAMOTH_CATEGORY
    );

    public static final KeyMapping KEY_LEFT = new KeyMapping(
            "key.seamoth.left",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_A,
            SEAMOTH_CATEGORY
    );

    public static final KeyMapping KEY_RIGHT = new KeyMapping(
            "key.seamoth.right",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_D,
            SEAMOTH_CATEGORY
    );
}
