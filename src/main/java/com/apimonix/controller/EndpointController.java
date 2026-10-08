package com.apimonix.controller;

import com.apimonix.model.Endpoint;
import com.apimonix.config.AuthUser;
import com.apimonix.model.PingLog;
import com.apimonix.repository.PingLogRepository;
import com.apimonix.service.EndpointService;
import com.apimonix.service.UptimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/endpoints")
@RequiredArgsConstructor
public class EndpointController {
    private final EndpointService endpointService;
    private final UptimeService uptimeService;
    private final PingLogRepository pingLogRepository;

    @GetMapping
    public ResponseEntity<List<Endpoint>> getEndpoints(@AuthenticationPrincipal AuthUser authUser){
        return ResponseEntity.ok(endpointService.getEndpoints(authUser.getId()));
    }

    @PostMapping
    public ResponseEntity<Endpoint> addEndpoint(
            @RequestBody AddEndpointRequest request,
            @AuthenticationPrincipal AuthUser authUser){
        Endpoint endpoint = endpointService.addEndpoint(
                authUser.getId(),
                authUser.getPlan(),
                request.name(),
                request.url());
        return ResponseEntity.ok(endpoint);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEndpoint(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthUser authUser){
        endpointService.deleteEndpoint(id, authUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/uptime")
    public ResponseEntity<UptimeService.UptimeSummary> getUptime(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthUser authUser){
        return ResponseEntity.ok(uptimeService.getSummary(id));
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<List<PingLog>> getLogs(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthUser authUser){
        return ResponseEntity.ok(
                pingLogRepository.findTop50ByEndpointIdOrderByCheckedAtDesc(id));
    }

    public record AddEndpointRequest(String name, String url){}
}
