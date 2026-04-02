package org.eu.pnxlr.git.crosschat.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Shared MiniMessage serializer utilities.
 */
public final class MiniMessageHelper {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private MiniMessageHelper() {
    }

    /**
     * Deserializes a MiniMessage string into an Adventure component.
     *
     * @param input MiniMessage input
     * @return deserialized component
     */
    public static Component deserialize(String input) {
        return MINI_MESSAGE.deserialize(input);
    }
}
