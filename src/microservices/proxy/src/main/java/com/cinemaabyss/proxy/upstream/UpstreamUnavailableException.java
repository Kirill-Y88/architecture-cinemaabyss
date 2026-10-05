package com.cinemaabyss.proxy.upstream;

import com.cinemaabyss.proxy.routing.Target;
import lombok.Getter;

@Getter
public class UpstreamUnavailableException extends RuntimeException {

    private final Target target;

    public UpstreamUnavailableException(Target target, String path, Throwable cause) {
        super("Upstream " + target.getLabel() + " is unavailable for " + path + ": " + cause.getMessage(), cause);
        this.target = target;
    }
}
