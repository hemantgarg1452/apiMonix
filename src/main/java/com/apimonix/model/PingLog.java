package com.apimonix.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "ping_logs")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id", nullable = false)
    private Endpoint endpoint;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "response_ms")
    private Long responseMs;

    @Column(name = "is_up", nullable = false)
    private boolean isUp;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    @PrePersist
    public void prePersist(){
        this.checkedAt = Instant.now();
    }
}
