package com.zpedroo.mining.data;

import com.zpedroo.mining.Main;

import java.io.File;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;

public class SQLiteConnector {

    private Connection connection;
    private String TABLE_NAME;

    public SQLiteConnector(String TABLE_NAME) {
        this.TABLE_NAME = TABLE_NAME;
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + loadFile().getAbsolutePath();
            connection = DriverManager.getConnection(url);
            checkTable();
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private void checkTable() throws SQLException {
        connection.createStatement().execute("CREATE TABLE IF NOT EXISTS '" + TABLE_NAME + "' (" +
                "'uuid' VARCHAR(255) NOT NULL," +
                "'broken' BIGINT NOT NULL DEFAULT '0'," +
                "'avaible' BIGINT NOT NULL DEFAULT '0'," +
                "'tokens' BIGINT NOT NULL DEFAULT '0'," +
                "PRIMARY KEY (`uuid`));");
    }

    public PlayerData loadPlayer(UUID uuid) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM '" + TABLE_NAME + "' where uuid='" + uuid.toString() + "';");
            if (resultSet.next()) {
                BigInteger broken = new BigInteger(resultSet.getString("broken"));
                BigInteger avaible = new BigInteger(resultSet.getString("avaible"));
                BigInteger tokens = new BigInteger(resultSet.getString("tokens"));
                return new PlayerData(uuid, broken, avaible, tokens);
            }
            return new PlayerData(uuid, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return new PlayerData(uuid, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
        }
    }

    public boolean savePlayer(PlayerData data) {
        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT OR REPLACE INTO '" + TABLE_NAME + "' VALUES" + "('" +
                    data.getUUID().toString() + "', '" +
                    data.getBlocksBroken() + "', '" +
                    data.getBlocksAvaible() + "', '" +
                    data.getTokens() + "'" + ");");
            return true;
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public List<PlayerData> getTopTen() {
        List<PlayerData> topTen = new LinkedList<>();
        try {
            Statement statement = this.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM '" + TABLE_NAME + "' order by broken desc limit 10;");
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                BigInteger broken = new BigInteger(resultSet.getString("broken"));
                BigInteger avaible = new BigInteger(resultSet.getString("avaible"));
                BigInteger tokens = new BigInteger(resultSet.getString("tokens"));
                topTen.add(new PlayerData(uuid, broken, avaible, tokens));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return topTen;
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private File loadFile() {
        return new File(Main.get().getDataFolder(), "/players.db");
    }
}
