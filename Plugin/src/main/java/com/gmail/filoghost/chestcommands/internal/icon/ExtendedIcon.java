package com.gmail.filoghost.chestcommands.internal.icon;

import com.gmail.filoghost.chestcommands.ChestCommands;
import com.gmail.filoghost.chestcommands.api.Icon;
import com.gmail.filoghost.chestcommands.bridge.EconomyBridge;
import com.gmail.filoghost.chestcommands.bridge.PlaceholderAPIBridge;
import com.gmail.filoghost.chestcommands.internal.ExtendedIconMenu;
import com.gmail.filoghost.chestcommands.internal.MenuInventoryHolder;
import com.gmail.filoghost.chestcommands.internal.RequiredItem;
import com.gmail.filoghost.chestcommands.util.StringUtils;
import com.gmail.filoghost.chestcommands.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.List;

public class ExtendedIcon extends Icon {

    private String permission;
    private String permissionMessage;
    private String viewPermission;

    private boolean permissionNegated;
    private boolean viewPermissionNegated;

    private String moneyPrice;
    private String expLevelsPrice;
    private RequiredItem requiredItem;

    public ExtendedIcon() {
        super();
    }

    public boolean canClickIcon(Player player) {
        if (permission == null) {
            return true;
        }

        if (permissionNegated) {
            return !player.hasPermission(permission);
        } else {
            return player.hasPermission(permission);
        }
    }

    public void setPermission(String permission) {
        if (StringUtils.isNullOrEmpty(permission)) {
            permission = null;
        }

        if (permission != null) {
            if (permission.startsWith("-")) {
                permissionNegated = true;
                permission = permission.substring(1, permission.length()).trim();
            }
        }
        this.permission = permission;
    }

    public void setPermissionMessage(String permissionMessage) {
        this.permissionMessage = permissionMessage;
    }

    public boolean hasViewPermission() {
        return viewPermission != null;
    }

    public boolean canViewIcon(Player player) {
        if (viewPermission == null) {
            return true;
        }

        if (viewPermissionNegated) {
            return !player.hasPermission(viewPermission);
        } else {
            return player.hasPermission(viewPermission);
        }
    }

    public void setViewPermission(String viewPermission) {
        if (StringUtils.isNullOrEmpty(viewPermission)) {
            viewPermission = null;
        }

        if (viewPermission != null) {
            if (viewPermission.startsWith("-")) {
                viewPermissionNegated = true;
                viewPermission = viewPermission.substring(1, viewPermission.length()).trim();
            }
        }
        this.viewPermission = viewPermission;
    }

    public String getMoneyPrice() {
        return moneyPrice;
    }

    public void setMoneyPrice(String moneyPrice) {
        this.moneyPrice = moneyPrice;
    }

    public String getExpLevelsPrice() {
        return expLevelsPrice;
    }

    public void setExpLevelsPrice(String expLevelsPrice) {
        this.expLevelsPrice = expLevelsPrice;
    }

    public RequiredItem getRequiredItem() {
        return requiredItem;
    }

    public void setRequiredItem(RequiredItem requiredItem) {
        this.requiredItem = requiredItem;
    }

    public String calculateName(Player pov) {
        return PlaceholderAPIBridge.replace(pov, super.calculateName(pov));
    }

    public List<String> calculateLore(Player pov) {
        return PlaceholderAPIBridge.replace(pov, super.calculateLore(pov));
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onClick(Player player) {

        // Check all the requirements.

        if (!canClickIcon(player)) {
            if (permissionMessage != null) {
                player.sendMessage(permissionMessage);
            } else {
                player.sendMessage(ChestCommands.getLang().default_no_icon_permission);
            }
            return closeOnClick;
        }

        double price = 0;
        if (moneyPrice != null) {
            if (!EconomyBridge.hasValidEconomy()) {
                player.sendMessage(ChatColor.RED + "This command has a price, but Vault with a compatible economy plugin was not found. For security, the command has been blocked. Please inform the staff.");
                return closeOnClick;
            }

            String parsedPrice = PlaceholderAPIBridge.replace(player, moneyPrice);

            try {
                price = Double.parseDouble(parsedPrice);
            } catch (NumberFormatException e) {
                String errorMessage = ChatColor.RED + "Error while parsing icon click money price! " + parsedPrice + " isn't a valid number!";
                player.sendMessage(errorMessage);
                ChestCommands.getInstance().getLogger().warning(errorMessage);
                return closeOnClick; // Error
            }

            if (price != 0 && !EconomyBridge.hasMoney(player, price)) {
                player.sendMessage(ChestCommands.getLang().no_money.replace("{money}", EconomyBridge.formatMoney(price)));
                return closeOnClick;
            }
        }

        int exp = 0;
        if(expLevelsPrice != null) {
            String parsedExp = PlaceholderAPIBridge.replace(player, expLevelsPrice);

            try {
                exp = Integer.parseInt(parsedExp);
            } catch (NumberFormatException e) {
                String errorMessage = ChatColor.RED + "Error while parsing icon click level price! " + parsedExp + " isn't a valid number!";
                player.sendMessage(errorMessage);
                ChestCommands.getInstance().getLogger().warning(errorMessage);
                return closeOnClick; // Error
            }
        }

        if (exp > 0) {
            if (player.getLevel() < exp) {
                player.sendMessage(ChestCommands.getLang().no_exp.replace("{levels}", Integer.toString(exp)));
                return closeOnClick;
            }
        }

        if (requiredItem != null) {

            if (!requiredItem.hasItem(player)) {
                player.sendMessage(ChestCommands.getLang().no_required_item
                        .replace("{material}", Utils.formatMaterial(requiredItem.getMaterial()))
                        .replace("{id}", Integer.toString(requiredItem.getMaterial().getId()))
                        .replace("{amount}", Integer.toString(requiredItem.getAmount()))
                        .replace("{datavalue}", requiredItem.hasRestrictiveDataValue() ? Short.toString(requiredItem.getDataValue()) : ChestCommands.getLang().any)
                );
                return closeOnClick;
            }
        }

        // Take the money, the points and the required item.

        boolean changedVariables = false; // To update the placeholders.

        if (price > 0) {
            if (!EconomyBridge.takeMoney(player, price)) {
                player.sendMessage(ChatColor.RED + "Error: the transaction couldn't be executed. Please inform the staff.");
                return closeOnClick;
            }
            player.sendMessage(ChestCommands.getLang().money_taken.replace("{money}", EconomyBridge.formatMoney(price)));
            changedVariables = true;
        }

        if (exp > 0) {
            player.setLevel(player.getLevel() - exp);
        }

        if (requiredItem != null) {
            requiredItem.takeItem(player);
        }

        if (changedVariables) {
            InventoryView view = player.getOpenInventory();
            if (view != null) {
                Inventory topInventory = view.getTopInventory();
                if (topInventory.getHolder() instanceof MenuInventoryHolder) {
                    MenuInventoryHolder menuHolder = (MenuInventoryHolder) topInventory.getHolder();

                    if (menuHolder.getIconMenu() instanceof ExtendedIconMenu) {
                        ((ExtendedIconMenu) menuHolder.getIconMenu()).refresh(player, topInventory);
                    }
                }
            }
        }

        return super.onClick(player);
    }


}
