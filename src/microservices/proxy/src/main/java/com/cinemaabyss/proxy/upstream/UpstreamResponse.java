package com.cinemaabyss.proxy.upstream;

import java.net.http.HttpHeaders;

public record UpstreamResponse(int status, HttpHeaders headers, byte[] body) {
}
