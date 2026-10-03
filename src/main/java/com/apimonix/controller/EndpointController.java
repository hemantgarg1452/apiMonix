package com.apimonix.controller;

import com.apimonix.model.Endpoint;
import com.apimonix.model.PingLog;
import com.apimonix.repository.PingLogRepository;
import com.apimonix.service.EndpointService;
import com.apimonix.service.UptimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<Endpoint>> getEndpoints(){
        UUID userId = UUID.fromString("12e80951-225e-4ccf-b11f-a3b986504783");
        return ResponseEntity.ok(endpointService.getEndpoints(userId));
    }

    @PostMapping
    public ResponseEntity<Endpoint> addEndpoint(@RequestBody AddEndpointRequest request){
        UUID userId = UUID.fromString("12e80951-225e-4ccf-b11f-a3b986504783");
        Endpoint endpoint = endpointService.addEndpoint(userId, request.name(), request.url());
        return ResponseEntity.ok(endpoint);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEndpoint(@PathVariable UUID id){
        UUID userId = UUID.fromString("12e80951-225e-4ccf-b11f-a3b986504783");
        endpointService.deleteEndpoint(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/uptime")
    public ResponseEntity<UptimeService.UptimeSummary> getUptime(@PathVariable UUID id){
        return ResponseEntity.ok(uptimeService.getSummary(id));
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<List<PingLog>> getLogs(@PathVariable UUID id){
        return ResponseEntity.ok(
                pingLogRepository.findTop50ByEndpointIdOrderByCheckedAtDesc(id));
    }

    public record AddEndpointRequest(String name, String url){}
}
