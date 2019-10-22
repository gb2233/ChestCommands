package com.gmail.filoghost.chestcommands.bridge;

import io.github.thatsmusic99.headsplus.HeadsPlus;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class HeadsPlusBridge {

  private static HeadsPlus plugin;

  public static boolean setupPlugin() {
    if (!Bukkit.getServer().getPluginManager().isPluginEnabled("HeadsPlus")) {
      return false;
    }

    Plugin headsPlus = Bukkit.getServer().getPluginManager().getPlugin("HeadsPlus");

    if (headsPlus == null) {
      return false;
    }

    plugin = ((HeadsPlus) headsPlus);

    return true;
  }

  public static boolean hasValidPlugin() {
    return plugin != null;
  }

  public static boolean hasValidID(String input) {
    return hasValidPlugin() && plugin.getHeadsXConfig().isHPXSkull(input) && getItem(input) != null;
  }

  public static ItemStack getItem(String input) {
    if (!hasValidID(input)) {
      return null;
    }
    try {
      return plugin.getHeadsXConfig().getSkull(input);
    } catch (NullPointerException e) {
      return null;
    }
  }
}
