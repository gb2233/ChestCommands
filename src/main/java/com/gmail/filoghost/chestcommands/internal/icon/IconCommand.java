package com.gmail.filoghost.chestcommands.internal.icon;

import com.gmail.filoghost.chestcommands.bridge.PlaceholderAPIBridge;
import com.gmail.filoghost.chestcommands.config.AsciiPlaceholders;
import com.gmail.filoghost.chestcommands.internal.Variable;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class IconCommand {

    protected String command;
    private List<Variable> containedVariables;

    public IconCommand(String command) {
        this.command = AsciiPlaceholders.placeholdersToSymbols(command).trim();
        this.containedVariables = new ArrayList<>();

        for (Variable variable : Variable.values()) {
            if (command.contains(variable.getText())) {
                containedVariables.add(variable);
            }
        }
    }

    public String getParsedCommand(Player executor) {
        String commandCopy = command;

        // Process builtin variables
        for (Variable variable : containedVariables) {
            commandCopy = commandCopy.replace(variable.getText(), variable.getReplacement(executor));
        }

        // Process PlaceholderAPI variables
        commandCopy = PlaceholderAPIBridge.replace(executor, command);

        return commandCopy;
    }

    public abstract void execute(Player player);

}
