package com.foliaco.vision_bathroom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);       // Hilos siempre activos
        executor.setMaxPoolSize(5);        // Máximo de hilos bajo carga
        executor.setQueueCapacity(100);    // Cola de tareas en espera
        executor.setThreadNamePrefix("fcm-notification-");
        executor.setWaitForTasksToCompleteOnShutdown(true); // Espera terminar al apagar la app
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        return executor;
    }
}
