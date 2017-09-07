package com.gmail.filoghost.chestcommands.util;

public class MysqlData {

    private String host, database, username, password, tableName;
    private int port;
    
	public MysqlData(String host, int port, String database, String username, String password, String tableName) {
		this.host = host;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
		this.tableName = tableName;
	}
	public String ToString() {
		return host + database + username + password + tableName;
	}
	public String GetHost() {
		return this.host;
	}
	public int GetPort() {
		return this.port;
	}
	public String GetDatabase() {
		return this.database;
	}
	public String GetUsername() {
		return this.username;
	}
	public String GetPassword() {
		return this.password;
	}
	public String GetTableName() {
		return this.tableName;
	}
}
