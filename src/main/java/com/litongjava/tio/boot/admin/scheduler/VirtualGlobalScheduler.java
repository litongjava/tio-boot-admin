package com.litongjava.tio.boot.admin.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class VirtualGlobalScheduler {

  // 1. 虚拟线程执行器：每个任务都会开启一个新的虚拟线程
  public static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

  // 2. 调度器：仅用于计时。线程数固定为 1 或 2 即可，因为它不执行耗时业务
  public static final ScheduledThreadPoolExecutor SCHEDULER_INSTANCE = new ScheduledThreadPoolExecutor(1, r -> {
    Thread t = new Thread(r, "global-scheduler-timer");
    t.setDaemon(true);
    return t;
  });

  /**
   * 使用固定延迟执行：任务结束后等待 delay 时间再执行下一次
   */
  public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay,
      TimeUnit unit) {
    // 包装任务，使其在虚拟线程中运行
    return SCHEDULER_INSTANCE.scheduleWithFixedDelay(() -> {
      VIRTUAL_EXECUTOR.submit(command);
    }, initialDelay, delay, unit);
  }

  /**
   * 使用固定频率执行：每隔 period 时间执行一次
   */
  public static ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period,
      TimeUnit unit) {
    // 包装任务，使其在虚拟线程中运行
    return SCHEDULER_INSTANCE.scheduleAtFixedRate(() -> {
      VIRTUAL_EXECUTOR.submit(command);
    }, initialDelay, period, unit);
  }

  /** 停止所有任务 */
  public static void stop() {
    SCHEDULER_INSTANCE.shutdownNow();
    VIRTUAL_EXECUTOR.shutdownNow();
  }
}