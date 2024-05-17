package org.example;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Program {
    private final int BOUND = 10;
    private final int N_PRODUCERS = 4;
    private final int N_CONSUMERS = Runtime.getRuntime().availableProcessors();
    private final int poisonPill = Integer.MAX_VALUE;
    private final int poisonPillPerProducer = N_CONSUMERS / N_PRODUCERS;
    private final int mod = N_CONSUMERS % N_PRODUCERS;
    private final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(BOUND);

    public void execute() {
        System.out.println("N_CONSUMERS = "+Runtime.getRuntime().availableProcessors());
        for (int i = 1; i < N_PRODUCERS; i++) {
            new Thread(new Producer(queue, poisonPill, poisonPillPerProducer)).start();
        }
        for (int j = 0; j < N_CONSUMERS; j++) {
            new Thread(new Consumer(queue, poisonPill)).start();
        }
        new Thread(new Producer(queue, poisonPill, poisonPillPerProducer + mod)).start();
    }
}