package kr.ac.hansung.cse.minifarm.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity @Table(name="users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true) private String email;
    @Column(name="password_hash", nullable=false) private String passwordHash;
    private String nickname;
    private OffsetDateTime createdAt;
    @PrePersist void onCreate(){ this.createdAt = OffsetDateTime.now(); }
}
