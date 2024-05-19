package ru.otus.java.basic.tegneryadnov.coursework;

import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Класс-пул соединений
 */
public class DBCPDataSource {

    private static final BasicDataSource ds = new BasicDataSource();
    private static DBCPDataSource dBCPDataSource;
    private final AppSettings appSettings;

    public static DBCPDataSource getInstance(AppSettings appSettings) {
        if (dBCPDataSource == null) {
            dBCPDataSource = new DBCPDataSource(appSettings);
        }
        return dBCPDataSource;
    }

    private DBCPDataSource(AppSettings appSettings) {
        this.appSettings = appSettings;
        ds.setUrl(appSettings.getString("url", "jdbc:postgresql://localhost:5432/db_tests"));
        ds.setUsername(appSettings.getString("login", "unknownLogin"));
        ds.setPassword(appSettings.getString("password", "unknownPassword"));
        ds.setMinIdle(5);
        ds.setMaxIdle(10);
        ds.setMaxOpenPreparedStatements(appSettings.getInt("threadsCount"));
    }

    /**
     * Метод получения нового соединения к БД
     * @param appSettings настройки программы
     * @return соединение типа java.sql.Connection
     * @throws SQLException
     */
    public static Connection getConnection(AppSettings appSettings) throws SQLException {
        getInstance(appSettings);
        return ds.getConnection();
    }
}