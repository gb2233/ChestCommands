package com.gmail.filoghost.chestcommands.internal.icon.command;

import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ConsoleIconCommand extends IconCommand {

    public ConsoleIconCommand(String command) {
        super(command);
    }

    @Override
    public void execute(Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), getParsedCommand(player));
    }

}
