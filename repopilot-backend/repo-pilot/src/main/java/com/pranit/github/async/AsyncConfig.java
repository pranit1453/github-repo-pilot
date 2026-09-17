package com.pranit.github.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "syncExecutor")
    public Executor syncExecutor() {
        return Executors.newThreadPerTaskExecutor(Thread.ofVirtual()
                .name("repository-sync-", 0)
                .factory());
    }

    @Bean(name = "indexingExecutor")
    public Executor indexingExecutor() {
        return Executors.newThreadPerTaskExecutor(Thread.ofVirtual()
                .name("indexing-", 0)
                .factory());
    }
}
