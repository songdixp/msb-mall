package com.msb.mall.search.thread;

import java.util.concurrent.*;

public class ThreadPoolDemo {
    private static ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
            5,
            50,
            10,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("执行了线程开始-->" + Thread.currentThread().getName());
            int i = 100 / 50;
            System.out.println("执行了线程结束了");
            return i;
        }, poolExecutor).thenApplyAsync(res -> {
            System.out.println("执行[2]线程开始-->" + Thread.currentThread().getName());
            res = res * 10;
            return res;
        }, poolExecutor);

        System.out.println("future: "+future.get());
        // Executors.newCachedThreadPool();
        // Executors.newScheduledThreadPool();
        // Executors.newSingleThreadExecutor();
    }
}
