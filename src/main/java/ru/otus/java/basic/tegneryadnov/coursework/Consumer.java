package ru.otus.java.basic.tegneryadnov.coursework;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;

public class Consumer implements Runnable {
    private final BlockingQueue<String> queue;
    private final String POISON_PILL;
    private final BufferedWriter bufferedWriter;
    private final String CHARSET_NAME;

    public Consumer(BlockingQueue<String> queue, AppSettings appSettings) {
        this.queue = queue;
        POISON_PILL = appSettings.getString("poisonPill", "unknownPoisonPill");
        CHARSET_NAME = appSettings.getString("reportCharSetName", "UTF-8");
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(appSettings.getString("reportFileName", "report.csv"),
                    Charset.forName(CHARSET_NAME)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        try {
            while (true) {
                String row = queue.take();
                if (row.equals(POISON_PILL)) {
                    System.out.println("Поступила команда: закончить чтение из очереди.");
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    return;
                }
                try {
                    bufferedWriter.write(row + "\r\n");
                    bufferedWriter.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        }
    }
}