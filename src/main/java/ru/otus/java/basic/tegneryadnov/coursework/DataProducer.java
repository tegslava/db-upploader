package ru.otus.java.basic.tegneryadnov.coursework;

import java.sql.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DataProducer {
    private final AppSettings appSettings;
    private final BlockingQueue<String> rowsQueue;
    private final ExecutorService services;
    private boolean headerChecked = false;
    private final String POISON_PILL;
    private final String COLUMN_SEPARATOR;
    private final String SQL;
    private final int THREADS_COUNT;
    private final String WITH_HEADER;

    public DataProducer(BlockingQueue<String> rowsQueue, AppSettings appSettings) {
        this.rowsQueue = rowsQueue;
        this.appSettings = appSettings;
        POISON_PILL = appSettings.getString("poisonPill", "unknownPoisonPill");
        COLUMN_SEPARATOR = appSettings.getString("reportColumnSeparator", ";");
        SQL = appSettings.getString("sql","");
        THREADS_COUNT = appSettings.getInt("threadsCount");
        WITH_HEADER = appSettings.getString("reportWithHeader","Y");
        services = Executors.newFixedThreadPool(THREADS_COUNT);
    }

    private void sendCommandStopConsumer() {
        try {
            rowsQueue.put(POISON_PILL);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getHeader(ResultSetMetaData rsmd) throws SQLException {
        if (rsmd == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            stringBuilder.append(rsmd.getColumnName(i))
                    .append(i < rsmd.getColumnCount() ? COLUMN_SEPARATOR : "");
        }
        return stringBuilder.toString();
    }

    public void execute() {
        try {
            for (int i = 1; i <= THREADS_COUNT; i++) {
                services.execute(() -> {
                    try (Connection connection = DBCPDataSource.getConnection(appSettings);
                         Statement statement = connection.createStatement()) {
                        try (ResultSet resultSet = statement.executeQuery(SQL)) {
                            StringBuilder stringBuilder = new StringBuilder();
                            ResultSetMetaData resultSetMetaData = null;
                            while (resultSet.next()) {
                                stringBuilder.setLength(0);
                                if (resultSetMetaData == null) {
                                    resultSetMetaData = resultSet.getMetaData();
                                }
                                if (!headerChecked) {
                                    processHeader(resultSetMetaData);
                                }
                                processRow(resultSetMetaData, stringBuilder, resultSet);
                            }
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } finally {
            if (services != null) {
                services.shutdown();
                try {
                    services.awaitTermination(30, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    //logger.error("Ошибка закрытия пула потоков " + e);
                    throw new RuntimeException(e);
                } finally {
                    sendCommandStopConsumer();
                }
                //logger.info("Программа завершена");
            }
        }
    }

    private void processRow(ResultSetMetaData rsmd, StringBuilder sb, ResultSet rs) throws SQLException {
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            sb.append(rs.getString(i))
                    .append(i < rsmd.getColumnCount() ? COLUMN_SEPARATOR : "");
        }
        try {
            rowsQueue.put(sb.toString());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void processHeader(ResultSetMetaData rsmd) throws SQLException {
        if (headerChecked) {
            return;
        }
        if (WITH_HEADER.equals("Y")) {
            String header = getHeader(rsmd);
            try {
                rowsQueue.put(header);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        headerChecked = true;
    }
}
