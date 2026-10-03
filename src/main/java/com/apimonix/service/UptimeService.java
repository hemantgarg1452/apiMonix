package com.apimonix.service;

import com.apimonix.repository.PingLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UptimeService {

    private final PingLogRepository pingLogRepository;

    public double calculateUptime(UUID endpointId, int days){
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);

        long total = pingLogRepository.countTotalPings(endpointId, since);

        if(total == 0) return 100.0;
        long up = pingLogRepository.countUpPings(endpointId, since);
        double uptime = (double) up/total*100.0;
        return Math.round(uptime*100.0)/100.0;
    }

    public double calculateAvgResponseMs(UUID endpointId, int days){
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
        Double avg = pingLogRepository.avgResponseMs(endpointId, since);

        return avg != null ? Math.round(avg*10.0)/10.0 : 0.0;
    }

    public UptimeSummary getSummary(UUID endpointId){
        return new UptimeSummary(
                calculateUptime(endpointId, 7),
                calculateUptime(endpointId, 30),
                calculateUptime(endpointId, 90),
                calculateAvgResponseMs(endpointId, 30)
        );
    }

    public record UptimeSummary(
            double uptime7d,
            double uptime30d,
            double uptime90d,
            double avgResponseMs
    ){}
}
