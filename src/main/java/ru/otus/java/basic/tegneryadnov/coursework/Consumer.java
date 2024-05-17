package ru.otus.java.basic.tegneryadnov.coursework;

import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;

public class Consumer implements Runnable {
    private BlockingQueue<String> queue;
    private final String poisonPill;
    private BufferedWriter bufferedWriter;

    public Consumer(BlockingQueue<String> queue, String poisonPill) {
        this.queue = queue;
        this.poisonPill = poisonPill;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(Config.reportFileName, Charset.forName(Config.reporterCharSetName)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        try {
            while (true) {
                String row = queue.take();
                if (row.equals(poisonPill)) {
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