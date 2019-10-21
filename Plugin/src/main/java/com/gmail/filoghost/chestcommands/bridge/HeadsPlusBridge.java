package com.gmail.filoghost.chestcommands.bridge;

import io.github.thatsmusic99.headsplus.HeadsPlus;
import io.github.thatsmusic99.headsplus.api.HeadsPlusAPI;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class HeadsPlusBridge {

  private static HeadsPlusAPI api;

  public static boolean setupPlugin() {
    if (!Bukkit.getServer().getPluginManager().isPluginEnabled("HeadsPlus")) {
      return false;
    }

    api = ((HeadsPlus) Bukkit.getServer().getPluginManager().getPlugin("HeadsPlus")).getAPI();

    return true;
  }

  public static boolean hasValidPlugin() {
    return api != null;
  }

  public static boolean hasValidID(String input) {
    return hasValidPlugin() && getItem(input) != null;
  }

  public static ItemStack getItem(String input) {
    try {
      return api.getHead(input);
    } catch (NullPointerException e) {
      return null;
    }
  }
}
