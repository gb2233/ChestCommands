package com.gmail.filoghost.chestcommands.task;

import com.gmail.filoghost.chestcommands.api.Icon;
import org.bukkit.entity.Player;

public class ExecuteCommandsTask implements Runnable {

    private Player player;
    private Icon icon;


    public ExecuteCommandsTask(Player player, Icon icon) {
        this.player = player;
        this.icon = icon;
    }


    @Override
    public void run() {
        boolean close = icon.onClick(player);

        if (close) {
            player.closeInventory();
        }
    }


}
