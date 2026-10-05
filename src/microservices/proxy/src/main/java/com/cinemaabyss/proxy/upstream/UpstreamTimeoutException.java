package com.cinemaabyss.proxy.upstream;

import com.cinemaabyss.proxy.routing.Target;
import lombok.Getter;

@Getter
public class UpstreamTimeoutException extends RuntimeException {

    private final Target target;

    public UpstreamTimeoutException(Target target, String path, Throwable cause) {
        super("Upstream " + target.getLabel() + " timed out for " + path, cause);
        this.target = target;
    }
}
