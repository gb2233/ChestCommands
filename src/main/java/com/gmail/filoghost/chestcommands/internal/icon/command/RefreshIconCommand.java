package com.gmail.filoghost.chestcommands.internal.icon.command;

import com.gmail.filoghost.chestcommands.ChestCommands;
import com.gmail.filoghost.chestcommands.internal.ExtendedIconMenu;
import com.gmail.filoghost.chestcommands.internal.MenuInventoryHolder;
import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.scheduler.BukkitRunnable;

public class RefreshIconCommand extends IconCommand {

    public RefreshIconCommand(String command) {
        super(command);
    }

    @Override
    public void execute(Player player) {
        int delay;
        try {
            delay = Integer.parseInt(command.trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
        new BukkitRunnable() {

            @Override
            public void run() {
                InventoryView view = player.getOpenInventory();
                if (view == null) {
                    return;
                }
                Inventory topInventory = view.getTopInventory();
                if (topInventory.getHolder() instanceof MenuInventoryHolder) {
                    MenuInventoryHolder menuHolder = (MenuInventoryHolder) topInventory.getHolder();
                    if (menuHolder.getIconMenu() instanceof ExtendedIconMenu) {
                        ExtendedIconMenu extMenu = (ExtendedIconMenu) menuHolder.getIconMenu();
                        extMenu.refresh(player, topInventory);
                        player.updateInventory();
                    }
                }
            }
        }.runTaskLater(ChestCommands.getInstance(), delay);
    }
}
