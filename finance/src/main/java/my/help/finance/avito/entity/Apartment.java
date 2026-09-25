package my.help.finance.avito.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "apartments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "avito_id", unique = true, nullable = false, length = 64)
    private String avitoId;

    @Column(length = 512)
    private String title;

    private Integer rooms;

    @Column(name = "total_area")
    private Double totalArea;

    private Integer floor;

    @Column(name = "total_floors")
    private Integer totalFloors;

    private Long price;

    @Column(name = "price_per_meter")
    private Long pricePerMeter;

    @Column(length = 8)
    private String currency;

    @Column(length = 512)
    private String address;

    @Column(name = "metro_name")
    private String metroName;

    @Column(name = "metro_minutes")
    private Integer metroMinutes;

    private Double latitude;

    private Double longitude;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @Column(name = "building_type")
    private String buildingType;

    @Column(name = "year_built")
    private Integer yearBuilt;

    @Column
    private String renovation;

    // ── инфраструктура бота-обходчика ─────────────────────────────
    @Column(length = 1024)
    private String url;

    @Column(name = "detail_visited")
    private Boolean detailVisited;

    @Column(name = "detail_visited_at")
    private LocalDateTime detailVisitedAt;

    @Column(name = "detail_visit_attempts")
    @Builder.Default
    private Integer detailVisitAttempts = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}