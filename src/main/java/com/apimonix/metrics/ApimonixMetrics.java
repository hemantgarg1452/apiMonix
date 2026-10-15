package com.apimonix.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ApimonixMetrics {
    private final Counter totalPings;
    private final Counter failedPings;
    private final Counter incidentsOpened;
    private final Counter alertsSent;
    private final Counter alertsFailed;

    private final Timer pingDuration;

    public ApimonixMetrics(MeterRegistry registry){
        this.totalPings = Counter.builder("apimonix.pings.total")
                .description("Total number of endpoint pings executed")
                .register(registry);

        this.failedPings = Counter.builder("apimonix.pings.failed")
                .description("Total number of failed pings (endpoint down")
                .register(registry);

        this.incidentsOpened = Counter.builder("apimonix.incidents.opened")
                .description("Total incidents opened")
                .register(registry);

        this.alertsSent = Counter.builder("apimonix.alerts.sent")
                .description("Total alert emails sent successfully")
                .register(registry);

        this.alertsFailed = Counter.builder("apimonix.alerts.failed")
                .description("Total alert emails that failed to send")
                .register(registry);

        this.pingDuration = Timer.builder("apimonix.ping.duration")
                .description("Time taken to execute one ping")
                .publishPercentiles(0.5, 0.95, 0.99) // p50, p95, p99
                .register(registry);
    }
}
