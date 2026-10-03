package com.cinemaabyss.events.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "events")
public class EventsProperties {

    private Topics topics = new Topics();

    private Consumer consumer = new Consumer();

    private Duration sendTimeout = Duration.ofSeconds(10);

    @Getter
    @Setter
    public static class Topics {

        private String movie = "movie-events";

        private String user = "user-events";

        private String payment = "payment-events";
    }

    @Getter
    @Setter
    public static class Consumer {

        private boolean enabled = true;

        private String groupId = "cinemaabyss-events-service";
    }
}
