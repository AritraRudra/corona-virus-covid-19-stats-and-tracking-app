package com.covid19;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Covid19InfectedTrackingAppApplication {

    public static void main(final String[] args) {
        SpringApplication.run(Covid19InfectedTrackingAppApplication.class, args);
    }

    // https://howtodoinjava.com/spring-boot2/rest/enableasync-async-controller/
    // https://www.baeldung.com/spring-async
    // https://dzone.com/articles/spring-boot-creating-asynchronous-methods-using-as
    @Bean("tpDbTaskExecutor")
    public TaskExecutor getAsyncExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("Async-");
        return executor;
    }

}
