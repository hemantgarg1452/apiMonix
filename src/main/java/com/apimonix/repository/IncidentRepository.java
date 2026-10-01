package com.apimonix.repository;

import com.apimonix.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID> {
    boolean existsByEndpointIdAndResolvedAtIsNull(UUID endpointId);
    List<Incident> findByEndpointIdOrderByStartedAtDesc(UUID endpointId);

    @Modifying
    @Transactional
    @Query("""
           UPDATE Incident i
           SET i.resolvedAt = :resolvedAt
           WHERE i.endpoint.id = :endpointId
           AND i.resolvedAt IS NULL
           """)
    void resolveOpenIncident(UUID endpointId, Instant resolvedAt);
}
