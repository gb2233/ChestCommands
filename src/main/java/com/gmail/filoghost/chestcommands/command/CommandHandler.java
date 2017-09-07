package com.gmail.filoghost.chestcommands.command;

import com.gmail.filoghost.chestcommands.ChestCommands;
import com.gmail.filoghost.chestcommands.Permissions;
import com.gmail.filoghost.chestcommands.command.CommandFramework.CommandValidate;
import com.gmail.filoghost.chestcommands.internal.ExtendedIconMenu;
import com.gmail.filoghost.chestcommands.task.ErrorLoggerTask;
import com.gmail.filoghost.chestcommands.util.ErrorLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class CommandHandler extends CommandFramework {

    public CommandHandler(String label) {
        super(label);
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            // This info is accessible to anyone. Please don't remove it, remember that Chest Commands is developed for FREE.
            sender.sendMessage(ChestCommands.CHAT_PREFIX);
            sender.sendMessage(ChatColor.GREEN + "Version: " + ChatColor.GRAY + ChestCommands.getInstance().getDescription().getVersion());
            sender.sendMessage(ChatColor.GREEN + "Developer: " + ChatColor.GRAY + "filoghost");
            sender.sendMessage(ChatColor.GREEN + "Commands: " + ChatColor.GRAY + "/" + label + " help");
            return;
        }


        if (args[0].equalsIgnoreCase("help")) {
            CommandValidate.isTrue(sender.hasPermission(Permissions.COMMAND_BASE + "help"), "You don't have permission.");
            sender.sendMessage(ChestCommands.CHAT_PREFIX + " Commands:");
            sender.sendMessage(ChatColor.WHITE + "/" + label + " reload" + ChatColor.GRAY + " - Reloads the plugin.");
            sender.sendMessage(ChatColor.WHITE + "/" + label + " list" + ChatColor.GRAY + " - Lists the loaded menus.");
            sender.sendMessage(ChatColor.WHITE + "/" + label + " open <menu> [player]" + ChatColor.GRAY + " - Opens a menu for a player.");
            sender.sendMessage(ChatColor.WHITE + "/" + label + " migrate <import/export>" + ChatColor.GRAY + " - Syncing with mysql db.");
            return;
        }


        if (args[0].equalsIgnoreCase("reload")) {
            CommandValidate.isTrue(sender.hasPermission(Permissions.COMMAND_BASE + "reload"), "You don't have permission.");

            ChestCommands.closeAllMenus();

            ErrorLogger errorLogger = new ErrorLogger();
            ChestCommands.getInstance().load(errorLogger);

            ChestCommands.setLastReloadErrors(errorLogger.getSize());

            if (!errorLogger.hasErrors()) {
                sender.sendMessage(ChestCommands.CHAT_PREFIX + "Plugin reloaded.");
            } else {
                new ErrorLoggerTask(errorLogger).run();
                sender.sendMessage(ChestCommands.CHAT_PREFIX + ChatColor.RED + "Plugin reloaded with " + errorLogger.getSize() + " error(s).");
                if (!(sender instanceof ConsoleCommandSender)) {
                    sender.sendMessage(ChestCommands.CHAT_PREFIX + ChatColor.RED + "Please check the console.");
                }
            }
            return;
        }


        if (args[0].equalsIgnoreCase("open")) {
            CommandValidate.isTrue(sender.hasPermission(Permissions.COMMAND_BASE + "open"), "You don't have permission.");
            CommandValidate.minLength(args, 2, "Usage: /" + label + " open <menu> [player]");

            Player target;

            if (!(sender instanceof Player)) {
                CommandValidate.minLength(args, 3, "You must specify a player from the console.");
                target = Bukkit.getPlayerExact(args[2]);
            } else {
                if (args.length > 2) {
                    CommandValidate.isTrue(sender.hasPermission(Permissions.COMMAND_BASE + "open.others"), "You don't have permission to open menus for others.");
                    target = Bukkit.getPlayerExact(args[2]);
                } else {
                    target = (Player) sender;
                }

            }

            CommandValidate.notNull(target, "That player is not online.");

            String menuName = args[1].toLowerCase().endsWith(".yml") ? args[1] : args[1] + ".yml";
            ExtendedIconMenu menu = ChestCommands.getFileNameToMenuMap().get(menuName);
            CommandValidate.notNull(menu, "The menu \"" + menuName + "\" was not found.");

            if (!sender.hasPermission(menu.getPermission())) {
                menu.sendNoPermissionMessage(sender);
                return;
            }

            if (sender.getName().equalsIgnoreCase(target.getName())) {
                if (!ChestCommands.getLang().open_menu.isEmpty()) {
                    sender.sendMessage(ChestCommands.getLang().open_menu.replace("{menu}", menuName));
                }
            } else {
                if (!ChestCommands.getLang().open_menu_others.isEmpty()) {
                    sender.sendMessage(ChestCommands.getLang().open_menu_others.replace("{menu}", menuName).replace("{player}", target.getName()));
                }
            }

            menu.open(target);
            return;
        }


        if (args[0].equalsIgnoreCase("list")) {
            CommandValidate.isTrue(sender.hasPermission(Permissions.COMMAND_BASE + "list"), "You don't have permission.");
            sender.sendMessage(ChestCommands.CHAT_PREFIX + " Loaded menus:");
            for (String file : ChestCommands.getFileNameToMenuMap().keySet()) {
                sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.WHITE + file);
            }

            return;
        }
		
		if (args[0].equalsIgnoreCase("migrate")) {
			CommandValidate.dbEnabled("Please enable 'use-mysql' to use this feature");
			CommandValidate.isTrue(sender.hasPermission(Permissions.COMMAND_BASE + "migrate"), "You don't have permission.");
            CommandValidate.minLength(args, 2, "Correct usage: /" + label + "migrate <import/export>");
			if (args[1].equalsIgnoreCase("export")) {
				Export(ChestCommands.GetMenuList());
		        sender.sendMessage(ChatColor.RED + "Export Completed!");
			} 
			else if(args[1].equalsIgnoreCase("import")) {
				Import(ChestCommands.GetMenuList());
		        sender.sendMessage(ChatColor.RED + "Import Completed!");
			}
			return;
		}
		
        sender.sendMessage(ChatColor.RED + "Unknown sub-command \"" + args[0] + "\".");
    }	
	public static void Export(List<String> menuList) {
		try {
	        Statement statement = ChestCommands.GetConnection().createStatement();
	        File menuFolder = new File(ChestCommands.GetInstance().getDataFolder(), "menu");
	        for(String fName : menuList) {
	            String txtConfig = "";
	            try {
	            BufferedReader reader = new BufferedReader(new FileReader (new File(menuFolder,fName + ".yml")));
	            String line = null;
	            StringBuilder stringBuilder = new StringBuilder();
	            String ls = System.getProperty("line.separator");
	                while((line = reader.readLine()) != null) {
	                    stringBuilder.append(line);
	                    stringBuilder.append(ls);
	                }
	            reader.close();
	            txtConfig = stringBuilder.toString();
	            }
	            catch ( IOException e)
	            {
	            }
	            byte[] encodedBytes = Base64.getEncoder().encode(txtConfig.getBytes());
	            String encodedString = new String(encodedBytes, StandardCharsets.UTF_8);
	            String queryText = "INSERT INTO `ChestCommands`(`FILENAME`, `CFGSTRING`) VALUES (\"" + fName + "\",\"" + encodedString + "\") ON DUPLICATE KEY UPDATE `CFGSTRING` = \"" + encodedString + "\";";
	            statement.executeUpdate(queryText);
	        }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
		
	}
	
	public static void Import(List<String> menuList) {
		String decodedConfig = "";
		try {
        Statement statement = ChestCommands.GetConnection().createStatement();
        File menuFolder = new File(ChestCommands.GetInstance().getDataFolder(), "menu");
        for(String fName : menuList) {
            String queryText = "SELECT * FROM `" + ChestCommands.GetMysqlCreds().GetTableName() + "` WHERE `FILENAME` = '" + fName + "';";
            ResultSet result = statement.executeQuery(queryText);
            result.first();
            String base64Config = result.getString("CFGSTRING");
            byte[] decodedBytes = Base64.getDecoder().decode(base64Config.getBytes());
            decodedConfig = new String(decodedBytes, StandardCharsets.UTF_8);
            BufferedWriter writer = null;
            try
            {
                writer = new BufferedWriter( new FileWriter(new File(menuFolder,fName + ".yml")));
                writer.write(decodedConfig);

            }
            catch ( IOException e)
            {
            }
            finally
            {
                try
                {
                    if ( writer != null)
                    writer.close( );
                }
                catch ( IOException e)
                {
                }
            }

        }
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}

}
