package dev.simpilot

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/** Serial, off-main work. Idle threads are released; accepted work is never delayed or dropped. */
internal object BackgroundExecutors {
    fun serial(
        name: String,
        daemon: Boolean = false,
        idleMillis: Long = 30_000,
    ): ThreadPoolExecutor =
        ThreadPoolExecutor(
                1,
                1,
                idleMillis,
                TimeUnit.MILLISECONDS,
                LinkedBlockingQueue<Runnable>(),
                { task -> Thread(task, name).apply { isDaemon = daemon } },
            )
            .apply { allowCoreThreadTimeOut(true) }
}
