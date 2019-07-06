package com.gmail.filoghost.chestcommands.internal.icon.command;

import co.aikar.taskchain.TaskChain;
import com.gmail.filoghost.chestcommands.bridge.PlayerPointsBridge;
import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.gmail.filoghost.chestcommands.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GivePointsIconCommand extends IconCommand {

  private int pointsToGive;
  private String errorMessage;

  public GivePointsIconCommand(String command) {
    super(command);
    if (!hasVariables) {
      parsePoint(super.command);
    }
  }

  private void parsePoint(String command) {
    if (!Utils.isValidPositiveInteger(command)) {
      errorMessage = ChatColor.RED + "Invalid points amount: " + command;
      return;
    }
    errorMessage = null;
    pointsToGive = Integer.parseInt(command);
  }

  @Override
  public void execute(Player player, TaskChain taskChain) {
    if (hasVariables) {
      parsePoint(getParsedCommand(player));
    }
    if (errorMessage != null) {
      player.sendMessage(errorMessage);
      return;
    }
    if (!PlayerPointsBridge.hasValidPlugin()) {
      player.sendMessage(
          ChatColor.RED + "The plugin PlayerPoints was not found. Please inform the staff.");
      return;
    }

    taskChain.sync(() -> PlayerPointsBridge.givePoints(player, pointsToGive));
  }

}
