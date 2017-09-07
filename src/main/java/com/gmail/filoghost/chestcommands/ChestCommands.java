package com.gmail.filoghost.chestcommands;

import com.gmail.filoghost.chestcommands.bridge.EconomyBridge;
import com.gmail.filoghost.chestcommands.bridge.PlaceholderAPIBridge;
import com.gmail.filoghost.chestcommands.command.CommandFramework;
import com.gmail.filoghost.chestcommands.command.CommandHandler;
import com.gmail.filoghost.chestcommands.config.AsciiPlaceholders;
import com.gmail.filoghost.chestcommands.config.Lang;
import com.gmail.filoghost.chestcommands.config.Settings;
import com.gmail.filoghost.chestcommands.config.yaml.PluginConfig;
import com.gmail.filoghost.chestcommands.internal.BoundItem;
import com.gmail.filoghost.chestcommands.internal.ExtendedIconMenu;
import com.gmail.filoghost.chestcommands.internal.MenuData;
import com.gmail.filoghost.chestcommands.internal.MenuInventoryHolder;
import com.gmail.filoghost.chestcommands.listener.CommandListener;
import com.gmail.filoghost.chestcommands.listener.InventoryListener;
import com.gmail.filoghost.chestcommands.listener.JoinListener;
import com.gmail.filoghost.chestcommands.listener.SignListener;
import com.gmail.filoghost.chestcommands.nms.AttributeRemover;
import com.gmail.filoghost.chestcommands.serializer.CommandSerializer;
import com.gmail.filoghost.chestcommands.serializer.MenuSerializer;
import com.gmail.filoghost.chestcommands.task.ErrorLoggerTask;
import com.gmail.filoghost.chestcommands.task.RefreshMenusTask;
import com.gmail.filoghost.chestcommands.util.CaseInsensitiveMap;
import com.gmail.filoghost.chestcommands.util.ErrorLogger;
import com.gmail.filoghost.chestcommands.util.MysqlData;
import com.gmail.filoghost.chestcommands.util.Utils;
import com.gmail.filoghost.chestcommands.util.VersionUtils;
import org.bstats.bukkit.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChestCommands extends JavaPlugin {

    public static final String CHAT_PREFIX = ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "ChestCommands" + ChatColor.DARK_GREEN + "] " + ChatColor.GREEN;

    private static ChestCommands instance;
    private static Settings settings;
    private static Lang lang;

    private static Map<String, ExtendedIconMenu> fileNameToMenuMap;
    private static Map<String, ExtendedIconMenu> commandsToMenuMap;

    private static Set<BoundItem> boundItems;

    private static int lastReloadErrors;
    

    private static Connection connection;
    private static MysqlData creds;
    private String decodedConfig;
    private static Configuration config;
    private static List<String> menuList;
    private static File menusFolder;

    public static void closeAllMenus() {
        for (Player player : VersionUtils.getOnlinePlayers()) {
            if (player.getOpenInventory() != null) {
                if (player.getOpenInventory().getTopInventory().getHolder() instanceof MenuInventoryHolder || player.getOpenInventory().getBottomInventory().getHolder() instanceof MenuInventoryHolder) {
                    player.closeInventory();
                }
            }
        }
    }

    public static ChestCommands getInstance() {
        return instance;
    }

    public static Settings getSettings() {
        return settings;
    }

    public static Lang getLang() {
        return lang;
    }

    public static Map<String, ExtendedIconMenu> getFileNameToMenuMap() {
        return fileNameToMenuMap;
    }

    public static Map<String, ExtendedIconMenu> getCommandToMenuMap() {
        return commandsToMenuMap;
    }

    public static Set<BoundItem> getBoundItems() {
        return boundItems;
    }

    public static int getLastReloadErrors() {
        return lastReloadErrors;
    }

    public static void setLastReloadErrors(int lastReloadErrors) {
        ChestCommands.lastReloadErrors = lastReloadErrors;
    }

    @Override
    public void onEnable() {
        if (instance != null) {
            getLogger().warning("Please do not use /reload or plugin reloaders. Do \"/cc reload\" instead.");
            return;
        }

        instance = this;
		config = instance.getConfig();
        fileNameToMenuMap = CaseInsensitiveMap.create();
        commandsToMenuMap = CaseInsensitiveMap.create();
        boundItems = Utils.newHashSet();

        settings = new Settings(new PluginConfig(this, "config.yml"));
        lang = new Lang(new PluginConfig(this, "lang.yml"));

        if (!EconomyBridge.setupEconomy()) {
            getLogger().warning("Vault with a compatible economy plugin was not found! Icons with a PRICE or commands that give money will not work.");
        }

        if (PlaceholderAPIBridge.setup()) {
            getLogger().info("PlaceholderAPI was found! External placeholders can be used.");
        }

        AttributeRemover.setup();

        new MetricsLite(this);

        Bukkit.getPluginManager().registerEvents(new CommandListener(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(), this);
        Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new SignListener(), this);

        CommandFramework.register(this, new CommandHandler("chestcommands"));

        ErrorLogger errorLogger = new ErrorLogger();
        load(errorLogger);

        lastReloadErrors = errorLogger.getSize();
        if (errorLogger.hasErrors()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new ErrorLoggerTask(errorLogger), 10L);
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new RefreshMenusTask(), 2L, 2L);
    }

    @Override
    public void onDisable() {
        closeAllMenus();
		try {
			if(connection.isValid(4))
				connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    public void load(ErrorLogger errorLogger) {
        fileNameToMenuMap.clear();
        commandsToMenuMap.clear();
        boundItems.clear();

        CommandSerializer.checkClassConstructors(errorLogger);

        try {
            settings.load();
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().warning("I/O error while using the configuration. Default values will be used.");
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            getLogger().warning("The config.yml was not a valid YAML, please look at the error above. Default values will be used.");
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().warning("Unhandled error while reading the values for the configuration! Please inform the developer.");
        }

        try {
            lang.load();
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().warning("I/O error while using the language file. Default values will be used.");
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            getLogger().warning("The lang.yml was not a valid YAML, please look at the error above. Default values will be used.");
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().warning("Unhandled error while reading the values for the configuration! Please inform the developer.");
        }

        try {
            AsciiPlaceholders.load(errorLogger);
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().warning("I/O error while reading the placeholders. They will not work.");
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().warning("Unhandled error while reading the placeholders! Please inform the developer.");
        }

        // Load the menus.
        menusFolder = new File(getDataFolder(), "menu");

        if (!menusFolder.isDirectory()) {
            // Create the directory with the default menu.
            menusFolder.mkdirs();
            Utils.saveResourceSafe(this, "menu" + File.separator + "example.yml");
        }

        List<PluginConfig> menusList = loadMenus(menusFolder);
        for (PluginConfig menuConfig : menusList) {
            try {
                menuConfig.load();
            } catch (IOException e) {
                e.printStackTrace();
                errorLogger.addError("I/O error while loading the menu \"" + menuConfig.getFileName() + "\". Is the file in use?");
                continue;
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
                errorLogger.addError("Invalid YAML configuration for the menu \"" + menuConfig.getFileName() + "\". Please look at the error above, or use an online YAML parser (google is your friend).");
                continue;
            }

            MenuData data = MenuSerializer.loadMenuData(menuConfig, errorLogger);
            ExtendedIconMenu iconMenu = MenuSerializer.loadMenu(menuConfig, data.getTitle(), data.getRows(), errorLogger);

            if (fileNameToMenuMap.containsKey(menuConfig.getFileName())) {
                errorLogger.addError("Two menus have the same file name \"" + menuConfig.getFileName() + "\" with different cases. There will be problems opening one of these two menus.");
            }
            fileNameToMenuMap.put(menuConfig.getFileName(), iconMenu);

            if (data.hasCommands()) {
                for (String command : data.getCommands()) {
                    if (!command.isEmpty()) {
                        if (commandsToMenuMap.containsKey(command)) {
                            errorLogger.addError("The menus \"" + commandsToMenuMap.get(command).getFileName() + "\" and \"" + menuConfig.getFileName() + "\" have the same command \"" + command + "\". Only one will be opened.");
                        }
                        commandsToMenuMap.put(command, iconMenu);
                    }
                }
            }

            iconMenu.setRefreshTicks(data.getRefreshTenths());

            if (data.getOpenActions() != null) {
                iconMenu.setOpenActions(data.getOpenActions());
            }

            if (data.hasBoundMaterial() && data.getClickType() != null) {
                BoundItem boundItem = new BoundItem(iconMenu, data.getBoundMaterial(), data.getClickType());
                if (data.hasBoundDataValue()) {
                    boundItem.setRestrictiveData(data.getBoundDataValue());
                }
                boundItems.add(boundItem);
            }
        }
        
		menuList = config.getStringList("menus");

		creds = new MysqlData(config.getString("db.host"), config.getInt("db.port"), config.getString("db.database"), config.getString("db.username"), config.getString("db.password"), config.getString("db.table-name"));
		if(config.getBoolean("use-mysql")) {
			try {     
	            openConnection();
	            if(config.getString("action-on-start").equalsIgnoreCase("import")) {
	            	CommandHandler.Import(menuList);
	            	instance.getLogger().info("Import on startup was successful");
	            }
	            else if(config.getString("action-on-start").equalsIgnoreCase("export")) {
	            	CommandHandler.Export(menuList);
	            	instance.getLogger().info("Export on startup was successful");
	            }
	            
	        } catch (ClassNotFoundException e) {
	            e.printStackTrace();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
			
		}
		
		
		
        // Register the BungeeCord plugin channel.
        if (!Bukkit.getMessenger().isOutgoingChannelRegistered(this, "BungeeCord")) {
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        }
    }

    /**
     * Loads all the configuration files recursively into a list.
     */
    private List<PluginConfig> loadMenus(File file) {
        List<PluginConfig> list = Utils.newArrayList();
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                list.addAll(loadMenus(subFile));
            }
        } else if (file.isFile()) {
            if (file.getName().endsWith(".yml")) {
                list.add(new PluginConfig(this, file));
            }
        }
        return list;
    }
    
	private void openConnection() throws SQLException, ClassNotFoundException {
		if (connection != null && !connection.isClosed()) {
			return;
		} 
		synchronized (this) {
			if (connection != null && !connection.isClosed()) {
				return;
			}
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://" + creds.GetHost() + ":" + creds.GetPort() + "/" + creds.GetDatabase(), creds.GetUsername(), creds.GetPassword());
		}
	
	}	
	public static MysqlData GetMysqlCreds() {
		return creds;
	}
	public static List<String> GetMenuList() {
		return menuList;
	}
	public static Connection GetConnection() {
		return connection;
	}
	public static Configuration GetConfig() {
		return config;
	}
	public static ChestCommands GetInstance() {
		return instance;
	} 

}
