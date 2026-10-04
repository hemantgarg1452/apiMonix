package com.apimonix.service;

import com.apimonix.model.Endpoint;
import com.apimonix.model.Incident;
import com.apimonix.model.PingLog;
import com.apimonix.repository.IncidentRepository;
import com.apimonix.repository.PingLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class PingService {
    private final WebClient webClient;
    private final PingLogRepository pingLogRepository;
    private final IncidentRepository incidentRepository;
    private final AlertService alertService;


    public PingLog ping(Endpoint endpoint){
        long startTime = System.currentTimeMillis();
        boolean isUp = false;
        int statusCode = 0;
        String failReason = null;

        try{
            var response = webClient
                    .get()
                    .uri(endpoint.getUrl())
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(10));

            if(response != null){
                statusCode = response.getStatusCode().value();
                isUp = statusCode<400;
            }
        } catch (Exception e){
            failReason = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.warn("Ping failed for endpoint [{}] url =[{}] reason=[{}]",
                    endpoint.getId(), endpoint.getUrl(), failReason);
        }

        long responseMs = System.currentTimeMillis() - startTime;

        PingLog pingLog = PingLog.builder()
                .endpoint(endpoint)
                .statusCode(statusCode == 0 ? null : statusCode)
                .responseMs(responseMs)
                .isUp(isUp)
                .checkedAt(Instant.now())
                .build();
        pingLogRepository.save(pingLog);

        handleIncident(endpoint, isUp, failReason);
        return pingLog;
    }

    private void handleIncident(Endpoint endpoint, boolean isUp, String failReason){
        boolean hasOpenIncident = incidentRepository
                .existsByEndpointIdAndResolvedAtIsNull(endpoint.getId());

        if(!isUp && !hasOpenIncident){
            Incident incident = Incident.builder()
                    .endpoint(endpoint)
                    .startedAt(Instant.now())
                    .cause(failReason!=null ? failReason : "HTTP "+ "error")
                    .build();

            incidentRepository.save(incident);

            log.info("Incident opened for endpoint [{}] url=[{}]",
                    endpoint.getId(), endpoint.getUrl());

            alertService.sendDownAlert(endpoint);
        }

        if(isUp && hasOpenIncident){
            incidentRepository.resolveOpenIncident(endpoint.getId(), Instant.now());

            log.info("Incident resolved for endpoint [{}] url=[{}]",
                    endpoint.getId(), endpoint.getUrl());

            alertService.sendRecoveryAlert(endpoint);
        }
    }
}
