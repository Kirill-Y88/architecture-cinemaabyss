package com.cinemaabyss.proxy.routing;

import com.cinemaabyss.proxy.config.RoutingProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
@Slf4j
public class Router {

    private final RoutingProperties properties;

    public Router(RoutingProperties properties) {
        this.properties = properties;
    }

    public Target resolve(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String normalized = path.startsWith("/") ? path : "/" + path;
        if (matchesDomain(normalized, "/api/movies")) {
            return resolveMoviesTarget();
        }
        if (matchesDomain(normalized, "/api/users")
                || matchesDomain(normalized, "/api/payments")
                || matchesDomain(normalized, "/api/subscriptions")) {
            return Target.MONOLITH;
        }
        if (matchesDomain(normalized, "/api/events")) {
            return Target.EVENTS_SERVICE;
        }
        return null;
    }

    public String urlFor(Target target) {
        return switch (target) {
            case MONOLITH -> properties.getMonolithUrl();
            case MOVIES_SERVICE -> properties.getMoviesServiceUrl();
            case EVENTS_SERVICE -> properties.getEventsServiceUrl();
        };
    }

    private Target resolveMoviesTarget() {
        if (!properties.isGradualMigration()) {
            return Target.MOVIES_SERVICE;
        }
        int percent = properties.getMoviesMigrationPercent();
        if (percent <= 0) {
            return Target.MONOLITH;
        }
        if (percent >= 100) {
            return Target.MOVIES_SERVICE;
        }
        Target target = ThreadLocalRandom.current().nextInt(100) < percent
                ? Target.MOVIES_SERVICE
                : Target.MONOLITH;
        log.debug("Gradual migration: {}% of the movies traffic, request goes to {}", percent, target.getLabel());
        return target;
    }

    private static boolean matchesDomain(String path, String domain) {
        return path.equals(domain) || path.startsWith(domain + "/");
    }
}
