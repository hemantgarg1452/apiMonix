package com.apimonix.service;

import com.apimonix.model.Endpoint;
import com.apimonix.repository.EndpointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PingScheduler {

    private final EndpointRepository endpointRepository;
    private final PingService pingService;

    @Scheduled(fixedDelay = 300_000)
    public void pingProEndpoints(){
        List<Endpoint> endpoints = endpointRepository.findActiveByUserPlan("PRO");
        endpoints.addAll(endpointRepository.findActiveByUserPlan("TEAM"));

        if(endpoints.isEmpty()){
            log.debug("No PRO/TEAM endpoints to ping");
            return;
        }
        log.info("Starting PRO ping cycle - {} endpoints", endpoints.size());

        for(Endpoint endpoint : endpoints){
            try{
                var result = pingService.ping(endpoint);
                log.debug("Pinged [{}] - isUp={} responseMs={}",
                        endpoint.getUrl(), result.isUp(), result.getResponseMs());
            } catch(Exception e){
                log.error("Unexpected error pinging endpoint [{}]: {}",
                        endpoint.getId(), e.getMessage());
            }
        }
        log.info("PRO ping cycle complete");
    }

    @Scheduled(fixedDelay = 900_000)
    public void pingFreeEndpoints(){
        List<Endpoint> endpoints = endpointRepository.findActiveByUserPlan("FREE");

        if(endpoints.isEmpty()){
            log.debug("No FREE endpoints to ping");
            return;
        }
        log.info("Starting FREE ping cycle - {} endpoints", endpoints.size());

        for(Endpoint endpoint : endpoints){
            try{
                var result = pingService.ping(endpoint);
                log.debug("Pinged [{}] - isUp={} responseMs={}",
                        endpoint.getUrl(), result.isUp(), result.getResponseMs());
            } catch (Exception e){
                log.error("Unexpected error pinging endpoint [{}]: {}",
                        endpoint.getId(), e.getMessage());
            }
        }
        log.info("FREE ping cycle complete");
    }
}
