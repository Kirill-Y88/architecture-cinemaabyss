package com.cinemaabyss.proxy.upstream;

import com.cinemaabyss.proxy.config.RoutingProperties;
import com.cinemaabyss.proxy.routing.RouteNotFoundException;
import com.cinemaabyss.proxy.routing.Router;
import com.cinemaabyss.proxy.routing.Target;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpTimeoutException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class ForwardingService {

    public static final String ROUTED_TO_HEADER = "X-Routed-To";
    public static final String MIGRATION_PERCENT_HEADER = "X-Migration-Percent";
    public static final String FALLBACK_HEADER = "X-Fallback";

    private static final Set<String> SKIPPED_RESPONSE_HEADERS = Set.of(
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade",
            "content-length",
            "content-encoding",
            ROUTED_TO_HEADER.toLowerCase(Locale.ROOT),
            MIGRATION_PERCENT_HEADER.toLowerCase(Locale.ROOT),
            FALLBACK_HEADER.toLowerCase(Locale.ROOT));

    private final Router router;
    private final UpstreamClient upstreamClient;
    private final RoutingProperties properties;

    public ResponseEntity<byte[]> forward(HttpServletRequest request, byte[] body) {
        String path = request.getRequestURI();
        Target target = router.resolve(path);
        if (target == null) {
            throw new RouteNotFoundException(path);
        }
        try {
            UpstreamResponse upstream = upstreamClient.exchange(router.urlFor(target), request, body);
            return toResponse(upstream, target, false);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new UpstreamUnavailableException(target, path, ex);
        } catch (IOException ex) {
            if (target == Target.MOVIES_SERVICE && properties.isFallbackToMonolith()) {
                log.warn("movies-service is unavailable ({}), falling back to the monolith for {}",
                        ex.toString(), path);
                return forwardToMonolith(request, body, path, ex);
            }
            throw wrap(target, path, ex);
        }
    }

    private ResponseEntity<byte[]> forwardToMonolith(HttpServletRequest request, byte[] body, String path,
                                                     IOException cause) {
        try {
            UpstreamResponse upstream = upstreamClient.exchange(router.urlFor(Target.MONOLITH), request, body);
            return toResponse(upstream, Target.MONOLITH, true);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new UpstreamUnavailableException(Target.MONOLITH, path, ex);
        } catch (IOException ex) {
            throw wrap(Target.MONOLITH, path, ex);
        }
    }

    private ResponseEntity<byte[]> toResponse(UpstreamResponse upstream, Target target, boolean fallback) {
        HttpHeaders headers = new HttpHeaders();
        for (Map.Entry<String, List<String>> header : upstream.headers().map().entrySet()) {
            if (!SKIPPED_RESPONSE_HEADERS.contains(header.getKey().toLowerCase(Locale.ROOT))) {
                headers.put(header.getKey(), header.getValue());
            }
        }
        headers.set(ROUTED_TO_HEADER, target.getLabel());
        headers.set(MIGRATION_PERCENT_HEADER, String.valueOf(properties.getMoviesMigrationPercent()));
        if (fallback) {
            headers.set(FALLBACK_HEADER, "true");
        }
        log.debug("Forwarded response: status={} routed-to={}", upstream.status(), target.getLabel());
        return ResponseEntity.status(upstream.status()).headers(headers).body(upstream.body());
    }

    private static RuntimeException wrap(Target target, String path, IOException cause) {
        if (cause instanceof HttpTimeoutException) {
            return new UpstreamTimeoutException(target, path, cause);
        }
        return new UpstreamUnavailableException(target, path, cause);
    }
}
