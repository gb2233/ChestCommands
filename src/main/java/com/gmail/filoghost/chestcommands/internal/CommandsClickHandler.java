package com.gmail.filoghost.chestcommands.internal;

import com.gmail.filoghost.chestcommands.api.ClickHandler;
import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.gmail.filoghost.chestcommands.internal.icon.command.OpenIconCommand;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandsClickHandler implements ClickHandler {

    private List<IconCommand> commands;
    private boolean closeOnClick;

    public CommandsClickHandler(List<IconCommand> commands, boolean closeOnClick) {
        this.commands = commands;
        this.closeOnClick = closeOnClick;

        if (commands != null && commands.size() > 0) {
            for (IconCommand command : commands) {
                if (command instanceof OpenIconCommand) {
                    // Fix GUI closing if KEEP-OPEN is not set, and a command should open another GUI.
                    this.closeOnClick = false;
                }
            }
        }
    }

    @Override
    public boolean onClick(Player player) {
        if (commands != null && commands.size() > 0) {
            for (IconCommand command : commands) {
                command.execute(player);
            }
        }

        return closeOnClick;
    }


}
