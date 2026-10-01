package com.apimonix.repository;

import com.apimonix.model.PingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface PingLogRepository extends JpaRepository<PingLog, Long> {
    List<PingLog> findTop50ByEndpointIdOrderByCheckedAtDesc(UUID endpointId);

    @Query("""
         SELECT COUNT(p) FROM PingLog p
         WHERE p.endpoint.id = :endpointId
         AND p.checkedAt >= :since
     """)
    long countTotalPings(UUID endpointId, Instant since);

    @Query("""
        SELECT COUNT(p) FROM PingLog p
        WHERE p.endpoint.id = :endpointId
        AND p.checkedAt >= :since
        AND p.isUp = true
    """)
    long countUpPings(UUID endpointId, Instant since);

    @Query("""
        SELECT AVG(p.responseMs) FROM PingLog p
        WHERE p.endpoint.id = :endpointId
        AND p.checkedAt >= :since
        AND p.isUp = true
    """)
    Double avgResponseMs(UUID endpointId, Instant since);
}
