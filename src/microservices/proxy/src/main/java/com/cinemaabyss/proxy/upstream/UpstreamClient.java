package com.cinemaabyss.proxy.upstream;

import com.cinemaabyss.proxy.config.RoutingProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;

@Component
public class UpstreamClient {

    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "host",
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade",
            "content-length",
            "accept-encoding",
            "expect",
            "x-routed-to",
            "x-migration-percent",
            "x-fallback");

    private final HttpClient httpClient;
    private final RoutingProperties properties;

    public UpstreamClient(RoutingProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(properties.getConnectTimeout())
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    public UpstreamResponse exchange(String baseUrl, HttpServletRequest request, byte[] body)
            throws IOException, InterruptedException {
        HttpRequest.BodyPublisher bodyPublisher = body != null && body.length > 0
                ? HttpRequest.BodyPublishers.ofByteArray(body)
                : HttpRequest.BodyPublishers.noBody();

        HttpRequest.Builder builder = HttpRequest.newBuilder(targetUri(baseUrl, request))
                .timeout(properties.getRequestTimeout())
                .method(request.getMethod(), bodyPublisher);
        copyRequestHeaders(request, builder);

        HttpResponse<byte[]> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
        return new UpstreamResponse(response.statusCode(), response.headers(), response.body());
    }

    private static URI targetUri(String baseUrl, HttpServletRequest request) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String query = request.getQueryString();
        return URI.create(base + request.getRequestURI() + (query != null ? "?" + query : ""));
    }

    private static void copyRequestHeaders(HttpServletRequest request, HttpRequest.Builder builder) {
        Enumeration<String> names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            if (HOP_BY_HOP_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                continue;
            }
            Enumeration<String> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                builder.header(name, values.nextElement());
            }
        }
    }
}
