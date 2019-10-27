/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.gmail.filoghost.chestcommands.config;

import com.gmail.filoghost.chestcommands.config.yaml.PluginConfig;
import com.gmail.filoghost.chestcommands.config.yaml.SpecialConfig;

import java.util.*;

public class Settings extends SpecialConfig {

	public boolean use_console_colors = true;
	public String default_color__name = "&f";
	public String default_color__lore = "&7";
	public String multiple_commands_separator = ";";
	public boolean update_notifications = false;
	public int anti_click_spam_delay = 200;
	public boolean use_only_commands_without_args = true;
	public boolean use_mysql = false;
	public String db__host = "localhost";
	public int db__port = 3306;
	public String db__database = "minecraft";
	public String db__username = "root";
	public String db__password = "1234";
	public String db__table_name = "chestcommands";
	public List<String> menus = Arrays.asList("exampleMenuTwo","R/beginWithThisName");
	public Map<String,String> name_forward_conversion = new HashMap<>();
	public String action_on_start = "none";

	public Settings(PluginConfig config) {
		super(config);
        name_forward_conversion.put("watch","CLOCK");
        name_forward_conversion.put("ender_portal_frame","END_PORTAL_FRAME");
        name_forward_conversion.put("gold_spade","GOLDEN_SHOVEL");
        name_forward_conversion.put("diamond_spade","DIAMOND_SHOVEL");
        name_forward_conversion.put("book_and_quill","WRITABLE_BOOK");
		setHeader("ChestCommands configuration file.\nTutorial: http://dev.bukkit.org/bukkit-plugins/chest-commands\n");
	}

}
