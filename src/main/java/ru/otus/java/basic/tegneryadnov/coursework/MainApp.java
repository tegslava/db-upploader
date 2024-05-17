package ru.otus.java.basic.tegneryadnov.coursework;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class MainApp {
    private static final BlockingQueue<String> rowsQueue = new LinkedBlockingQueue<>(100);
    private static final ExecutorService services = Executors.newFixedThreadPool(Config.BOUND);
    public static void main(String[] args) {
        new Thread(new Consumer(rowsQueue, Config.poisonPill)).start();
        (new DataProducer(rowsQueue, services)).execute();
    }
}
