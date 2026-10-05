package com.cinemaabyss.proxy.web;

import com.cinemaabyss.proxy.upstream.ForwardingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class ProxyController {

    private final ForwardingService forwardingService;

    @RequestMapping("/**")
    public ResponseEntity<byte[]> forward(HttpServletRequest request) throws IOException {
        byte[] body = request.getInputStream().readAllBytes();
        return forwardingService.forward(request, body);
    }
}
