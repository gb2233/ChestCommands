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

    public static List<String> replace(Player player, List<String> content) {
        if(!enabled) {
            return content;
        }
        return PlaceholderAPI.setPlaceholders(player, content);
    }

    public static String replace(Player player, String content) {
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
        if (newItem.getItemMeta().hasDisplayName()) {
            itemMeta.setDisplayName(PlaceholderAPI.setPlaceholders(player, newItem.getItemMeta().getDisplayName()));
        }
        if (newItem.getItemMeta().hasLore()) {
            itemMeta.setLore(PlaceholderAPI.setPlaceholders(player, newItem.getItemMeta().getLore()));
        }
        newItem.setItemMeta(itemMeta);
        return newItem;
    }
}
