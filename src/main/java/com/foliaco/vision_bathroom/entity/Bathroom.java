package com.foliaco.vision_bathroom.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bathrooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bathroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ManyToOne
    @JoinColumn(name = "block_id")
    private Block block;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private BathroomStatus status = BathroomStatus.DISPONIBLE;

    private Integer floor;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Gender {
        MASCULINO,
        FEMENINO,
        UNISEX
    }

    public enum BathroomStatus {
        DISPONIBLE,
        EN_LIMPIEZA,
        EN_MANTENIMIENTO,
        FUERA_DE_SERVICIO;

        public String toDisplayString() {
            return this.name().toLowerCase().replace("_", " ");
        }
    }

}
