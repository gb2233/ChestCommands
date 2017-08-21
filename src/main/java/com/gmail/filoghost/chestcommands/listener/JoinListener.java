package com.gmail.filoghost.chestcommands.listener;

import com.gmail.filoghost.chestcommands.ChestCommands;
import com.gmail.filoghost.chestcommands.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (ChestCommands.getLastReloadErrors() > 0 && event.getPlayer().hasPermission(Permissions.SEE_ERRORS)) {
            event.getPlayer().sendMessage(ChestCommands.CHAT_PREFIX + ChatColor.RED + "The plugin found " + ChestCommands.getLastReloadErrors() + " error(s) last time it was loaded. You can see them by doing \"/cc reload\" in the console.");
        }
    }

}
