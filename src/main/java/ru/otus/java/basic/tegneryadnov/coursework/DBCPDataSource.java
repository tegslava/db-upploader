package ru.otus.java.basic.tegneryadnov.coursework;

import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBCPDataSource {
    
    private static BasicDataSource ds = new BasicDataSource();

    static {
        ds.setUrl(Config.url);
        ds.setUsername(Config.login);
        ds.setPassword(Config.password);
        ds.setMinIdle(5);
        ds.setMaxIdle(10);
        ds.setMaxOpenPreparedStatements(Config.BOUND);
    }
    
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
    
    private DBCPDataSource(){ }
}