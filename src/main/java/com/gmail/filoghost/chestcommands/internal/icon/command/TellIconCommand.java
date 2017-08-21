package com.gmail.filoghost.chestcommands.internal.icon.command;

import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.gmail.filoghost.chestcommands.util.Utils;
import org.bukkit.entity.Player;

public class TellIconCommand extends IconCommand {

    public TellIconCommand(String command) {
        super(Utils.addColors(command));
    }

    @Override
    public void execute(Player player) {
        player.sendMessage(getParsedCommand(player));
    }

}
