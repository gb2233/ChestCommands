package com.gmail.filoghost.chestcommands.util;


import com.gmail.filoghost.chestcommands.ChestCommands;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DBHandler{
    private static HikariDataSource ds;
    private static String EXPORT;
    private static String IMPORT;
    private static String tableName;
    private static ChestCommands plugin;

    static{
        plugin = ChestCommands.getInstance();
        tableName = plugin.getConfig().getString("db.table-name","chestcommands");
        EXPORT = "INSERT INTO `" + tableName + "`(`FILENAME`, `CFGSTRING`) VALUES (?,?) " +
            "ON DUPLICATE KEY UPDATE `CFGSTRING` = VALUES(`CFGSTRING`);";
        IMPORT = "SELECT * FROM `" + tableName + "` WHERE `FILENAME` = ?;";

        HikariConfig config = new HikariConfig();
        config.setPoolName("captchagui");
        config.setJdbcUrl("jdbc:mysql://" +
            plugin.getConfig().getString("db.host","localhost") +
            ":" +
            plugin.getConfig().getInt("db.port",3306) +
            "/" +
            plugin.getConfig().getString("db.database","minecraft") +
            "?useSSL=false"
        );
        config.setUsername(plugin.getConfig().getString("db.username","root"));
        config.setPassword(plugin.getConfig().getString("db.password","root"));
        //config.setDriverClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("alwaysSendSetIsolation", "false");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");

        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        config.setMaximumPoolSize(5);
        config.setMinimumIdle(10);
        config.setMaxLifetime(1800000);
        config.setConnectionTimeout(5000);

        config.setLeakDetectionThreshold(TimeUnit.SECONDS.toMillis(10)); // 10000
        config.setInitializationFailTimeout(-1);

        ds = new HikariDataSource(config);
    }

    public DBHandler() throws SQLException {
        if (!tableExists(tableName)){
            Connection connection = getConnection();
            Statement s = connection.createStatement(); // "`id` INT AUTO_INCREMENT NOT NULL," +
            String statement = "CREATE TABLE `" + tableName + "` ( " +
                "`FILENAME` VARCHAR(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , " +
                "`CFGSTRING` TEXT CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , " +
                "PRIMARY KEY (`FILENAME`));";
            String statement2 = "CREATE INDEX `fname` ON `" + tableName + "` (`FILENAME`);";
            s.execute(statement);
            s.execute(statement2);
            s.close();
            connection.close();
        }

    }

    private static Connection getConnection() throws SQLException {
        Connection connection = ds.getConnection();
        if (connection == null) {
            throw new SQLException("Unable to get a connection from the pool.");
        }
        return connection;
    }

    public static void Export(List<String> menuList) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Connection connection = getConnection();
                File menuFolder = new File(ChestCommands.GetInstance().getDataFolder(), "menu");
                for(String fName : menuList) {
                    String txtConfig = "";
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(new File(menuFolder,fName + ".yml")));
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
                    PreparedStatement ps = connection.prepareStatement(EXPORT);
                    ps.setString(1,fName);
                    ps.setString(2,encodedString);
                    ps.executeUpdate();
                    ps.close();
                }
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

    }

    public static void Import(List<String> menuList) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String decodedConfig = "";
            try {
                Connection connection = getConnection();
                File menuFolder = new File(ChestCommands.GetInstance().getDataFolder(), "menu");
                for(String fName : menuList) {
                    PreparedStatement ps = connection.prepareStatement(IMPORT);
                    ps.setString(1,fName);
                    ResultSet result = ps.executeQuery();
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
                    ps.close();
                    result.close();

                }
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean tableExists(String table) throws SQLException {
        boolean ex = false;
        Connection connection = getConnection();
        ResultSet rs = connection.getMetaData().getTables(null, null, "%", null);
        while (rs.next()) {
            if (rs.getString(3).equalsIgnoreCase(table)) {
                ex = true;
            }
        }
        rs.close();
        connection.close();
        return ex;
    }

    public static void shutdown() {
        if (ds != null && !ds.isClosed()) {
            try {
                ds.getConnection().close();
                ds.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}