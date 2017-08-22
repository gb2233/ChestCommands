package com.gmail.filoghost.chestcommands.internal.icon.command;

import com.gmail.filoghost.chestcommands.ChestCommands;
import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.gmail.filoghost.chestcommands.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundIconCommand extends IconCommand {

    public SoundIconCommand(String command) {
        super(command);
    }

    @Override
    public boolean execute(Player player) {

        float pitch = 1.0f;
        float volume = 1.0f;

        String[] split = getParsedCommand(player).split(",");

        Sound sound = Utils.matchSound(split[0]);
        if (sound == null) {
            String errorMessage = ChatColor.RED + "Invalid sound \"" + split[0].trim() + "\".";
            player.sendMessage(errorMessage);
            ChestCommands.getInstance().getLogger().warning(errorMessage);
            return true; // A missing sound can't cause any damage
        }

        if (split.length > 1) {
            try {
                pitch = Float.parseFloat(split[1].trim());
            } catch (NumberFormatException ignored) {
            }
        }

        if (split.length > 2) {
            try {
                volume = Float.parseFloat(split[2].trim());
            } catch (NumberFormatException ignored) {
            }
        }

        player.playSound(player.getLocation(), sound, volume, pitch);
        return true;
    }

}
