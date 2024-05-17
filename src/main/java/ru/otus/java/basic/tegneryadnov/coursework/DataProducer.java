package ru.otus.java.basic.tegneryadnov.coursework;

import java.sql.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class DataProducer {
    private BlockingQueue<String> rowsQueue;
    private ExecutorService services;
    private boolean headerChecked = false;

    public DataProducer(BlockingQueue<String> rowsQueue, ExecutorService services) {
        this.rowsQueue = rowsQueue;
        this.services = services;
    }

    private void sendCommandStopConsumer() {
        try {
            rowsQueue.put(Config.poisonPill);
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
                    .append(i < rsmd.getColumnCount() ? Config.columnSeparator : "");
        }
        return stringBuilder.toString();
    }

    public void execute() {
        try {
            for (int i = 1; i <= Config.BOUND; i++) {
                services.execute(() -> {
                    try (Connection connection = DBCPDataSource.getConnection();
                         Statement statement = connection.createStatement()) {
                        try (ResultSet resultSet = statement.executeQuery(Config.sql)) {
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
                    .append(i < rsmd.getColumnCount() ? Config.columnSeparator : "");
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
        if (Config.withHeader) {
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
