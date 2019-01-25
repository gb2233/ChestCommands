package com.gmail.filoghost.chestcommands.internal.icon.command;

import com.gmail.filoghost.chestcommands.ChestCommands;
import com.gmail.filoghost.chestcommands.bridge.EconomyBridge;
import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.gmail.filoghost.chestcommands.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GiveMoneyIconCommand extends IconCommand {

    public GiveMoneyIconCommand(String command) {
        super(command);
    }

    @Override
    public boolean execute(Player player) {
        String parsedCommand = getParsedCommand(player);
        if (!Utils.isValidPositiveDouble(parsedCommand)) {
            String errorMessage = ChatColor.RED + "Invalid money amount: " + command;
            ChestCommands.getInstance().getLogger().warning(errorMessage);
            player.sendMessage(errorMessage);
            return false;
        }

        double moneyToGive = Double.parseDouble(parsedCommand);

        if (EconomyBridge.hasValidEconomy()) {
            EconomyBridge.giveMoney(player, moneyToGive);
        } else {
            String errorMessage = ChatColor.RED + "Vault with a compatible economy plugin not found. Please inform the staff.";
            player.sendMessage(errorMessage);
            ChestCommands.getInstance().getLogger().warning(errorMessage);
            return false;
        }
        return true;
    }

}
