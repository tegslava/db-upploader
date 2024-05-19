package ru.otus.java.basic.tegneryadnov.coursework;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MainApp {
    //private static final AppSettings settingsP = AppSettings.getInstance(SettingsType.XML_FILE, "db-upploader.properties.xml");
    //private static final BlockingQueue<String> rowsQueue = new LinkedBlockingQueue<>(settingsP.getInt("queueCapacity"));

    public static void main(String[] args) {
        AppSettings settings = AppSettings.getInstance(SettingsType.XML_FILE, "db-upploader.properties.xml");
        BlockingQueue<String> rowsQueue = new LinkedBlockingQueue<>(settings.getInt("queueCapacity"));
        new Thread(new Consumer(rowsQueue, settings)).start();
        (new DataProducer(rowsQueue, settings)).execute();
    }
}
