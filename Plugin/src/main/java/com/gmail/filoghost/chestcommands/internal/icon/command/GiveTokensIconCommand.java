package com.gmail.filoghost.chestcommands.internal.icon.command;

import com.gmail.filoghost.chestcommands.bridge.TokenManagerBridge;
import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.gmail.filoghost.chestcommands.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GiveTokensIconCommand extends IconCommand {

    private long tokensToGive;
    private String errorMessage;

    public GiveTokensIconCommand(String command) {
        super(command);

        if (!Utils.isValidPositiveInteger(command)) {
            errorMessage = ChatColor.RED + "Invalid tokens amount: " + command;
            return;
        }

        tokensToGive = Long.parseLong(command);
    }

    @Override
    public boolean execute(Player player) {
        if (errorMessage != null) {
            player.sendMessage(errorMessage);
            return false;
        }

        if (TokenManagerBridge.hasValidPlugin()) {
            TokenManagerBridge.giveTokens(player, tokensToGive);
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "The plugin TokenManager was not found. Please inform the staff.");
            return false;
        }
    }
}
