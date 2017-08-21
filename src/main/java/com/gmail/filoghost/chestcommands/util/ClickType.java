package com.gmail.filoghost.chestcommands.util;

import org.bukkit.event.block.Action;

public enum ClickType {

    LEFT,
    RIGHT,
    BOTH;

    public static ClickType fromOptions(boolean left, boolean right) {
        if (left && right) {
            return BOTH;
        } else if (left) {
            return LEFT;
        } else if (right) {
            return RIGHT;
        } else {
            return null;
        }
    }

    public boolean isValidInteract(Action action) {
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            return this == LEFT || this == BOTH;
        } else
            return (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && (this == RIGHT || this == BOTH);
    }

}
