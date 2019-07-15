package com.gmail.filoghost.chestcommands.internal.icon.command;

import co.aikar.taskchain.TaskChain;
import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.udojava.evalex.Expression;
import com.udojava.evalex.Expression.ExpressionException;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ConditionIconCommand extends IconCommand {

  public ConditionIconCommand(String command) {
    super(command);
  }

  @Override
  public void execute(Player player, TaskChain taskChain) {
    Expression condition = new Expression(getParsedCommand(player));
    try {
      if (!condition.isBoolean()) {
        player.sendMessage(ChatColor.RED + "Invalid condition! Please inform the staff");
        return;
      }
    } catch (ExpressionException e) {
      player.sendMessage(ChatColor.RED + "Invalid condition! Please inform the staff");
      return;
    }

    if (condition.eval().intValue() != 1) {
      taskChain.sync(TaskChain::abort);
    }
  }

}
