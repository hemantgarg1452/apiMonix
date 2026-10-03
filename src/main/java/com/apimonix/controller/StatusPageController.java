package com.apimonix.controller;

import com.apimonix.model.Endpoint;
import com.apimonix.model.Incident;
import com.apimonix.repository.EndpointRepository;
import com.apimonix.repository.IncidentRepository;
import com.apimonix.service.UptimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/status")
@RequiredArgsConstructor
public class StatusPageController {
    private final EndpointRepository endpointRepository;
    private final IncidentRepository incidentRepository;
    private final UptimeService uptimeService;

    @GetMapping("/{userId}")
    public ResponseEntity<StatusPageResponse> getStatusPage(
            @PathVariable UUID userId){
        List<Endpoint> endpoints =
                endpointRepository.findByUserIdAndActiveTrue(userId);

        if (endpoints.isEmpty()){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No endpoints found");
        }

        List<EndpointStatus> statuses = endpoints.stream()
                .map(ep->{
                    boolean isDown = incidentRepository
                            .existsByEndpointIdAndResolvedAtIsNull(ep.getId());

                    List<Incident> recentIncidents = incidentRepository
                            .findByEndpointIdOrderByStartedAtDesc(ep.getId())
                            .stream()
                            .limit(5)
                            .toList();

                    UptimeService.UptimeSummary uptime = uptimeService.getSummary(ep.getId());

                    return new EndpointStatus(
                            ep.getId(),
                            ep.getName(),
                            ep.getUrl(),
                            isDown ? "DOWN" : "UP",
                            uptime,
                            recentIncidents
                    );
                })
                .toList();

        boolean anyDown = statuses.stream()
                .anyMatch(s->s.status().equals("DOWN"));

        return ResponseEntity.ok(new StatusPageResponse(
                anyDown ? "DEGRADED" : "OPERATIONAL",
                statuses
        ));
    }

    public record StatusPageResponse(
            String overallStatus,
            List<EndpointStatus> endpoints
    ){}

    public record EndpointStatus(
            UUID id,
            String name,
            String url,
            String status,
            UptimeService.UptimeSummary uptime,
            List<Incident> recentIncidents
    ){}
}
