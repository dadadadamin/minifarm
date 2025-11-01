package kr.ac.hansung.cse.minifarm.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity @Table(name="devices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Device {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true) private String deviceUid;
    @Column(nullable=false) private String name;
    private String location;
    private Boolean isOnline;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="owner_id", nullable=false)
    private User owner;

    private OffsetDateTime createdAt;
    @PrePersist void onCreate(){ this.createdAt = OffsetDateTime.now(); }
}
