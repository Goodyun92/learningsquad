package com.example.learningsquad.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class WebMvcConfig {

    // TaskExecutor 설정
    @Bean
    public TaskExecutor taskExecutor() {
        // CPU 코어 수만큼 스레드를 만들어서 사용
        int corePoolSize = Runtime.getRuntime().availableProcessors();

        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(corePoolSize);  // thread-pool에서 사용할 초기(최소) thread의 갯수
        taskExecutor.setMaxPoolSize(corePoolSize*2);   // thread-pool에서 사용할 최대 thread의 갯수
        taskExecutor.setQueueCapacity(300);    // thread-pool에서 사용할 최대 queue의 크기
        taskExecutor.setThreadNamePrefix("ls-");
        taskExecutor.setWaitForTasksToCompleteOnShutdown(false);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.initialize();
        return taskExecutor;
    }

}
