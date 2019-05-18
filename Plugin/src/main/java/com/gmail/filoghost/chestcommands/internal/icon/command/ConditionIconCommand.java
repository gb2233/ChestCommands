package com.gmail.filoghost.chestcommands.internal.icon.command;

import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.udojava.evalex.Expression;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ConditionIconCommand extends IconCommand {
    public ConditionIconCommand(String command) {
        super(command);
    }

    @Override
    public boolean execute(Player player) {
        Expression condition = new Expression(getParsedCommand(player));
        if (condition.isBoolean()) {
            return condition.eval().intValue() == 1;
        } else {
            player.sendMessage(ChatColor.RED + "Invalid condition! Please inform the staff");
            return false;
        }
    }
}
