package com.gmail.filoghost.chestcommands.internal.icon.command;

import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import org.bukkit.entity.Player;

public class PlayerIconCommand extends IconCommand {

    public PlayerIconCommand(String command) {
        super(command);
    }

    @Override
    public void execute(Player player) {
        player.chat('/' + getParsedCommand(player));
    }

}
