package com.gmail.filoghost.chestcommands.internal.icon.command;

import co.aikar.taskchain.TaskChain;
import com.gmail.filoghost.chestcommands.bridge.TokenManagerBridge;
import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.gmail.filoghost.chestcommands.util.ExpressionUtils;
import com.gmail.filoghost.chestcommands.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GiveTokensIconCommand extends IconCommand {

  private String errorMessage;

  public GiveTokensIconCommand(String command) {
    super(command);
  }

  @Override
  public void execute(Player player, TaskChain taskChain) {
    long tokensToGive = 0;
    String parsed = getParsedCommand(player);
    if (Utils.isValidPositiveInteger(parsed)) {
      tokensToGive = Integer.parseInt(parsed);
    } else if (ExpressionUtils.isValidExpression(parsed)) {
      tokensToGive = ExpressionUtils.getResult(parsed).longValue();
    } else {
      errorMessage = ChatColor.RED + "Invalid tokens amount: " + command;
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

    if (tokensToGive > 0) {
      long finalTokensToGive = tokensToGive;
      taskChain.sync(() -> TokenManagerBridge.giveTokens(player, finalTokensToGive));
    }
  }

}
