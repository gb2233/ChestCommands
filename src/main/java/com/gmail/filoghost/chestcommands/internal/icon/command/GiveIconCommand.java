package com.gmail.filoghost.chestcommands.internal.icon.command;

import com.gmail.filoghost.chestcommands.ChestCommands;
import com.gmail.filoghost.chestcommands.exception.FormatException;
import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.gmail.filoghost.chestcommands.util.ItemStackReader;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveIconCommand extends IconCommand {

    public GiveIconCommand(String command) {
        super(command);
    }

    @Override
    public boolean execute(Player player) {

        ItemStack itemToGive;
        try {
            ItemStackReader reader = new ItemStackReader(getParsedCommand(player), true);
            itemToGive = reader.createStack();
        } catch (FormatException e) {
            String errorMessage = ChatColor.RED + "Invalid item to give: " + e.getMessage();
            ChestCommands.getInstance().getLogger().warning(errorMessage);
            player.sendMessage(errorMessage);
            return false;
        }

        player.getInventory().addItem(itemToGive.clone());
        return true;
    }

}
