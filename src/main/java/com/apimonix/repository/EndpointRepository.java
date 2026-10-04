package com.apimonix.repository;

import com.apimonix.model.Endpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface EndpointRepository extends JpaRepository<Endpoint, UUID> {

    List<Endpoint> findByUserIdAndActiveTrue(UUID userId);

    @Query("""
        SELECT e FROM Endpoint e
        JOIN FETCH e.user u
        WHERE e.active = true
        AND u.plan = :plan
    """)
    List<Endpoint> findActiveByUserPlan(String plan);

    int countByUserIdAndActiveTrue(UUID userId);
}
