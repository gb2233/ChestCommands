package com.gmail.filoghost.chestcommands.bridge;

import com.songoda.epicheads.EpicHeads;
import com.songoda.epicheads.head.HeadManager;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class EpicHeadsBridge {

  private static HeadManager manager;

  public static boolean setupPlugin() {
    if (!Bukkit.getServer().getPluginManager().isPluginEnabled("EpicHeads")) {
      return false;
    }

    manager = ((EpicHeads) Bukkit.getServer().getPluginManager().getPlugin("EpicHeads")).getHeadManager();

    return true;
  }

  public static boolean hasValidPlugin() {
    return manager != null;
  }

  public static boolean hasValidID(String input) {
    return hasValidPlugin() && getItem(input) != null;
  }

  public static ItemStack getItem(String input) {
    try {
      return manager.getHead(input).asItemStack();
    } catch (NullPointerException e) {
      return null;
    }
  }
}
