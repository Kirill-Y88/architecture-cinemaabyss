package com.cinemaabyss.proxy.routing;

import com.cinemaabyss.proxy.config.RoutingProperties;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class RouterTest {

    private static RoutingProperties properties() {
        RoutingProperties properties = new RoutingProperties();
        properties.setMonolithUrl("http://monolith:8080");
        properties.setMoviesServiceUrl("http://movies-service:8081");
        properties.setEventsServiceUrl("http://events-service:8082");
        return properties;
    }

    @Test
    void routesNotExtractedDomainsToMonolith() {
        Router router = new Router(properties());

        assertThat(router.resolve("/api/users")).isEqualTo(Target.MONOLITH);
        assertThat(router.resolve("/api/users/1")).isEqualTo(Target.MONOLITH);
        assertThat(router.resolve("/api/payments")).isEqualTo(Target.MONOLITH);
        assertThat(router.resolve("/api/payments/42")).isEqualTo(Target.MONOLITH);
        assertThat(router.resolve("/api/subscriptions")).isEqualTo(Target.MONOLITH);
    }

    @Test
    void routesMoviesToMonolithWhenNothingIsMigrated() {
        RoutingProperties properties = properties();
        properties.setMoviesMigrationPercent(0);

        assertThat(new Router(properties).resolve("/api/movies")).isEqualTo(Target.MONOLITH);
    }

    @Test
    void routesMoviesToMicroserviceWhenFullyMigrated() {
        RoutingProperties properties = properties();
        properties.setMoviesMigrationPercent(100);

        assertThat(new Router(properties).resolve("/api/movies")).isEqualTo(Target.MOVIES_SERVICE);
        assertThat(new Router(properties).resolve("/api/movies/7")).isEqualTo(Target.MOVIES_SERVICE);
    }

    @Test
    void splitsMoviesTrafficAccordingToTheConfiguredPercent() {
        RoutingProperties properties = properties();
        properties.setMoviesMigrationPercent(50);
        Router router = new Router(properties);

        int samples = 2_000;
        long toMicroservice = IntStream.range(0, samples)
                .mapToObj(index -> router.resolve("/api/movies"))
                .filter(target -> target == Target.MOVIES_SERVICE)
                .count();

        assertThat(toMicroservice).isBetween((long) samples * 45 / 100, (long) samples * 55 / 100);
    }

    @Test
    void routesTheWholeMoviesDomainToMicroserviceWhenFeatureFlagIsOff() {
        RoutingProperties properties = properties();
        properties.setGradualMigration(false);
        properties.setMoviesMigrationPercent(0);

        assertThat(new Router(properties).resolve("/api/movies")).isEqualTo(Target.MOVIES_SERVICE);
        assertThat(new Router(properties).resolve("/api/movies/1")).isEqualTo(Target.MOVIES_SERVICE);
    }

    @Test
    void clampsTheMigrationPercentToTheValidRange() {
        RoutingProperties properties = properties();
        properties.setMoviesMigrationPercent(150);

        assertThat(properties.getMoviesMigrationPercent()).isEqualTo(100);
        assertThat(new Router(properties).resolve("/api/movies")).isEqualTo(Target.MOVIES_SERVICE);

        properties.setMoviesMigrationPercent(-10);
        assertThat(properties.getMoviesMigrationPercent()).isZero();
        assertThat(new Router(properties).resolve("/api/movies")).isEqualTo(Target.MONOLITH);
    }

    @Test
    void routesEventsToTheEventsService() {
        Router router = new Router(properties());

        assertThat(router.resolve("/api/events")).isEqualTo(Target.EVENTS_SERVICE);
        assertThat(router.resolve("/api/events/movie")).isEqualTo(Target.EVENTS_SERVICE);
        assertThat(router.resolve("/api/events/health")).isEqualTo(Target.EVENTS_SERVICE);
    }

    @Test
    void doesNotRouteUnrelatedPaths() {
        Router router = new Router(properties());

        assertThat(router.resolve("/health")).isNull();
        assertThat(router.resolve("/")).isNull();
        assertThat(router.resolve("/api/unknown")).isNull();
        assertThat(router.resolve(null)).isNull();
    }

    @Test
    void resolvesUpstreamUrls() {
        Router router = new Router(properties());

        assertThat(router.urlFor(Target.MONOLITH)).isEqualTo("http://monolith:8080");
        assertThat(router.urlFor(Target.MOVIES_SERVICE)).isEqualTo("http://movies-service:8081");
        assertThat(router.urlFor(Target.EVENTS_SERVICE)).isEqualTo("http://events-service:8082");
    }
}
