package com.cinemaabyss.proxy.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Slf4j
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "proxy")
public class RoutingProperties {

    @NotBlank
    private String monolithUrl = "http://localhost:8080";

    @NotBlank
    private String moviesServiceUrl = "http://localhost:8081";

    @NotBlank
    private String eventsServiceUrl = "http://localhost:8082";

    private boolean gradualMigration = true;

    private int moviesMigrationPercent = 50;

    private boolean fallbackToMonolith = true;

    private Duration connectTimeout = Duration.ofSeconds(5);

    private Duration requestTimeout = Duration.ofSeconds(10);

    public void setMoviesMigrationPercent(int moviesMigrationPercent) {
        if (moviesMigrationPercent < 0 || moviesMigrationPercent > 100) {
            log.warn("MOVIES_MIGRATION_PERCENT={} is out of range [0, 100], value is clamped",
                    moviesMigrationPercent);
        }
        this.moviesMigrationPercent = Math.max(0, Math.min(100, moviesMigrationPercent));
    }
}
