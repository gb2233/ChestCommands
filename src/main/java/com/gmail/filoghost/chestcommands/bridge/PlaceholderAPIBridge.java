package com.gmail.filoghost.chestcommands.bridge;

import com.gmail.filoghost.chestcommands.util.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PlaceholderAPIBridge {

    private static boolean enabled;

    public static boolean setup() {
        enabled = Utils.isClassLoaded("me.clip.placeholderapi.PlaceholderAPI");
        return enabled;
    }

    public static boolean hasVariable(String message) {
        if(!enabled) {
            return false;
        }
        return PlaceholderAPI.containsPlaceholders(message);
    }

    public static boolean hasVariable(List<String> content) {
        if(!enabled) {
            return false;
        }
        for(String message : content) {
            if(hasVariable(message)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasVariable(ItemStack item) {
        if(!enabled) {
            return false;
        }
        if (item == null) {
            return false;
        }
        if (!item.hasItemMeta()) {
            return false;
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta.hasDisplayName()) {
            if(hasVariable(itemMeta.getDisplayName())) {
                return true;
            }
        }
        if (itemMeta.hasLore()) {
            if(hasVariable(itemMeta.getLore())) {
                return true;
            }
        }
        return false;
    }

    public static String replace(Player player, String content) {
        if(!enabled) {
            return content;
        }
        return PlaceholderAPI.setPlaceholders(player, content);
    }

    public static List<String> replace(Player player, List<String> content) {
        if(!enabled) {
            return content;
        }
        return PlaceholderAPI.setPlaceholders(player, content);
    }

    public static ItemStack replace(Player player, ItemStack item) {
        if(!enabled) {
            return item;
        }
        if (item == null) {
            return null;
        }
        if (!item.hasItemMeta()) {
            return item;
        }
        ItemStack newItem = item.clone();
        ItemMeta itemMeta = newItem.getItemMeta();
        if (itemMeta.hasDisplayName()) {
            itemMeta.setDisplayName(replace(player, itemMeta.getDisplayName()));
        }
        if (itemMeta.hasLore()) {
            itemMeta.setLore(replace(player, itemMeta.getLore()));
        }
        newItem.setItemMeta(itemMeta);
        return newItem;
    }
}
