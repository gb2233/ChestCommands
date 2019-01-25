package com.gmail.filoghost.chestcommands.internal.icon.command;

import com.gmail.filoghost.chestcommands.ChestCommands;
import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ConditionIconCommand extends IconCommand {

    public ConditionIconCommand(String command) {
        super(command);
    }

    @Override
    public boolean execute(Player player) {
        String[] values = getParsedCommand(player).split("=");
        if(values.length != 2) {
            String errorMessage = ChatColor.RED + "Invalid condition! " + command;
            player.sendMessage(errorMessage);
            ChestCommands.getInstance().getLogger().warning(errorMessage);
            return false; // Error
        }
        return values[0].trim().equalsIgnoreCase(values[1].trim());
    }
}
