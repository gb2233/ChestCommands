package com.gmail.filoghost.chestcommands.internal.icon.command;

import co.aikar.taskchain.TaskChain;
import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.udojava.evalex.Expression;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ConditionIconCommand extends IconCommand {

  public ConditionIconCommand(String command) {
    super(command);
  }

  @Override
  public void execute(Player player, TaskChain taskChain) {
    Expression condition = new Expression(getParsedCommand(player));
    if (!condition.isBoolean()) {
      player.sendMessage(ChatColor.RED + "Invalid condition! Please inform the staff");
      return;
    }

    taskChain.sync(() -> {
      if (condition.eval().intValue() != 1) {
        TaskChain.abort();
      }
    });
  }

}
