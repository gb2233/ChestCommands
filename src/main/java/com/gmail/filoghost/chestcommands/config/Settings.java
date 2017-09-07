package com.gmail.filoghost.chestcommands.config;

import com.gmail.filoghost.chestcommands.config.yaml.PluginConfig;
import com.gmail.filoghost.chestcommands.config.yaml.SpecialConfig;

public class Settings extends SpecialConfig {

    public boolean use_console_colors = true;
    public String default_color__name = "&f";
    public String default_color__lore = "&7";
    public String multiple_commands_separator = ";";
    public int anti_click_spam_delay = 200;
    public boolean use_only_commands_without_args = true;
	public boolean use_mysql = false;
	public String db__host = "localhost";
	public int db__port = 3306;
	public String db__database = "minecraft";
	public String db__username = "root";
	public String db__password = "1234";
	public String db__table_name = "chestcommands";
	public String menus = " []";
    public String action_on_start = "none";

    public Settings(PluginConfig config) {
        super(config);
        setHeader("ChestCommands configuration file.\nTutorial: http://dev.bukkit.org/bukkit-plugins/chest-commands\n");
    }

}
