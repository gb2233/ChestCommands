package com.gmail.filoghost.chestcommands.util;


import com.gmail.filoghost.chestcommands.ChestCommands;
import com.google.common.io.Files;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DBHandler{
    private static HikariDataSource ds;
    private static String EXPORT;
    private static String IMPORT;
    private static String LIKE_IMPORT;
    private static String tableName;
    private static ChestCommands plugin;

    static{
        plugin = ChestCommands.getInstance();
        tableName = ChestCommands.getSettings().db__table_name;
        EXPORT = "INSERT INTO `" + tableName + "`(`FILENAME`, `CFGSTRING`) VALUES (?,?) " +
            "ON DUPLICATE KEY UPDATE `CFGSTRING` = VALUES(`CFGSTRING`);";
        IMPORT = "SELECT * FROM `" + tableName + "` WHERE `FILENAME` = ?;";
        LIKE_IMPORT = "SELECT * FROM `" + tableName + "` WHERE `FILENAME` LIKE ?;";

        HikariConfig config = new HikariConfig();
        config.setPoolName("chestcommands");
        config.setJdbcUrl("jdbc:mysql://" +
            ChestCommands.getSettings().db__host +
            ":" +
            ChestCommands.getSettings().db__port +
            "/" +
            ChestCommands.getSettings().db__database +
            "?useSSL=false"
        );
        config.setUsername(ChestCommands.getSettings().db__username);
        config.setPassword(ChestCommands.getSettings().db__password);
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

    @SuppressWarnings("SqlResolve")
    public DBHandler() throws SQLException {
        if (!tableExists(tableName)){
            Connection connection = getConnection();
            Statement s = connection.createStatement(); // "`id` INT AUTO_INCREMENT NOT NULL," +
            String statement = "CREATE TABLE `" + tableName + "` ( " +
                "`FILENAME` VARCHAR(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , " +
                "`CFGSTRING` BLOB NOT NULL , " +
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

    public static void Export(List<String> menuList, CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = getConnection()) {
                File menuFolder = new File(ChestCommands.getInstance().getDataFolder(), "menu");
                for(String fName : menuList) {
                    Set<String> fNames = new HashSet<>();
                    if (fName.startsWith("R/")){
                        String finalFName = fName.substring(2);
                        File[] fList = menuFolder.listFiles((dir, name) -> name.startsWith(finalFName));
                        Arrays.stream(Objects.requireNonNull(fList)).forEach(file -> fNames.add(Files.getNameWithoutExtension(file.getName())));
                    } else{
                        fNames.add(fName);
                    }
                    for (String file : fNames){
                        try (InputStream in = new FileInputStream(new File(menuFolder,file + ".yml"));
                             PreparedStatement ps = connection.prepareStatement(EXPORT)
                        ) {
                            ps.setString(1,file);
                            ps.setBlob(2,in);
                            ps.executeUpdate();
                        } catch (IOException | SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
                ChestCommands.getInstance().doReload(sender);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

    }

    public static void Import(List<String> menuList, CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = getConnection()) {
                File menuFolder = new File(ChestCommands.getInstance().getDataFolder(), "menu");
                for(String fName : menuList) {
                    PreparedStatement ps;
                    if (fName.startsWith("R/")){
                        ps = connection.prepareStatement(LIKE_IMPORT);
                        fName = fName.substring(2) + "%";
                    }else{
                        ps = connection.prepareStatement(IMPORT);
                    }
                    ps.setString(1,fName);
                    ResultSet result = ps.executeQuery();
                    while(result.next()){
                        String file = result.getString("FILENAME");
                        Blob blob = result.getBlob("CFGSTRING");
                        byte[] byteArray = blob.getBytes(1, (int) blob.length());
                        try (FileOutputStream outPutStream = new FileOutputStream(new File(menuFolder, file + ".yml"))) {
                            outPutStream.write(byteArray);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    ps.close();
                    result.close();
                }
                ChestCommands.getInstance().doReload(sender);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean tableExists(String table) throws SQLException {
        boolean ex = false;
        Connection connection = getConnection();
        ResultSet rs = connection.getMetaData().getTables(null, null, tableName, null);
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