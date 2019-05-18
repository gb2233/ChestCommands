package com.gmail.filoghost.chestcommands.internal.icon.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.gmail.filoghost.chestcommands.bridge.PlayerPointsBridge;
import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.gmail.filoghost.chestcommands.util.Utils;

public class GivePointsIconCommand extends IconCommand {

    private int pointsToGive;
    private String errorMessage;

    public GivePointsIconCommand(String command) {
        super(command);

        if (!Utils.isValidPositiveInteger(command)) {
            errorMessage = ChatColor.RED + "Invalid points amount: " + command;
            return;
        }

        pointsToGive = Integer.parseInt(command);
    }

    @Override
    public boolean execute(Player player) {
        if (errorMessage != null) {
            player.sendMessage(errorMessage);
            return false;
        }

        if (PlayerPointsBridge.hasValidPlugin()) {
            PlayerPointsBridge.givePoints(player, pointsToGive);
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "The plugin PlayerPoints was not found. Please inform the staff.");
            return false;
        }
    }

}
