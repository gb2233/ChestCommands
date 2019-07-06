package com.gmail.filoghost.chestcommands.internal.icon.command;

import co.aikar.taskchain.TaskChain;
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
    if (!hasVariables) {
      parseToken(command);
    }
  }

  private void parseToken(String command) {
    if (!Utils.isValidPositiveInteger(command)) {
      errorMessage = ChatColor.RED + "Invalid tokens amount: " + command;
      return;
    }
    errorMessage = null;
    tokensToGive = Long.parseLong(command);
  }

  @Override
  public void execute(Player player, TaskChain taskChain) {
    if (hasVariables) {
      parseToken(getParsedCommand(player));
    }
    if (errorMessage != null) {
      player.sendMessage(errorMessage);
      return;
    }
    if (!TokenManagerBridge.hasValidPlugin()) {
      player.sendMessage(
          ChatColor.RED + "The plugin TokenManager was not found. Please inform the staff.");
      return;
    }

    taskChain.sync(() -> TokenManagerBridge.giveTokens(player, tokensToGive));
  }

}
