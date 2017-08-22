package com.gmail.filoghost.chestcommands.internal.icon.command;

import com.gmail.filoghost.chestcommands.ChestCommands;
import com.gmail.filoghost.chestcommands.internal.ExtendedIconMenu;
import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class OpenIconCommand extends IconCommand {

    public OpenIconCommand(String command) {
        super(command);
    }

    @Override
    public boolean execute(final Player player) {
        String parsedCommand = getParsedCommand(player);
        final ExtendedIconMenu menu = ChestCommands.getFileNameToMenuMap().get(parsedCommand.toLowerCase());
        if (menu != null) {

            // Delay the task, since this command is executed in ClickInventoryEvent
            // and opening another inventory in the same moment is not a good idea.
            new BukkitRunnable() {

                @Override
                public void run() {
                    if (player.hasPermission(menu.getPermission())) {
                        menu.open(player);
                    } else {
                        menu.sendNoPermissionMessage(player);
                    }
                }
            }.runTaskLater(ChestCommands.getInstance(), 3);
        } else {
            String errorMessage = ChatColor.RED + "Menu " + parsedCommand + " not found! Please inform the staff.";
            player.sendMessage(errorMessage);
            ChestCommands.getInstance().getLogger().warning(errorMessage);
            return false;
        }
        return true;
    }

}
