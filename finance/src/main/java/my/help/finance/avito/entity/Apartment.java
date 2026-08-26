package my.help.finance.avito.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

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

    private Boolean studio;

    @Column(name = "total_area")
    private Double totalArea;

    private Integer floor;

    @Column(name = "total_floors")
    private Integer totalFloors;

    private Long price;

    @Column(name = "price_raw", length = 64)
    private String priceRaw;

    @Column(length = 8)
    private String currency;

    @Column(name = "price_per_meter")
    private Long pricePerMeter;

    @Column(length = 512)
    private String address;

    @Column
    private String street;

    @Column(name = "street_link", length = 1024)
    private String streetLink;

    @Column(name = "house_number", length = 64)
    private String houseNumber;

    @Column(name = "house_link", length = 1024)
    private String houseLink;

    @Column(length = 512)
    private String metro;

    @Column(name = "metro_name")
    private String metroName;

    @Column(name = "metro_link", length = 1024)
    private String metroLink;

    @Column(name = "metro_distance_raw", length = 64)
    private String metroDistanceRaw;

    @Column(name = "metro_minutes")
    private Integer metroMinutes;

    @Column
    private String district;

    private Double latitude;

    private Double longitude;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_full", columnDefinition = "TEXT")
    private String descriptionFull;

    @Column(length = 1024)
    private String url;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @Builder.Default
    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    @JsonIgnoreProperties("apartment")
    private Set<ApartmentImage> images = new LinkedHashSet<>();

    @Builder.Default
    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("apartment")
    private Set<ApartmentBadge> badges = new LinkedHashSet<>();

    @Column(name = "is_new")
    private Boolean isNew;

    @Column(name = "is_promoted")
    private Boolean isPromoted;

    @Column(name = "published_date_raw", length = 64)
    private String publishedDateRaw;

    @Column(name = "seller_name")
    private String sellerName;

    @Column(name = "seller_profile_url", length = 1024)
    private String sellerProfileUrl;

    @Column(name = "seller_completed_listings_raw")
    private String sellerCompletedListingsRaw;

    @Column(name = "seller_completed_listings_count")
    private Integer sellerCompletedListingsCount;

    @Column(name = "detail_visited")
    private Boolean detailVisited;

    @Column(name = "detail_visited_at")
    private LocalDateTime detailVisitedAt;

    @Column(name = "detail_file_path", length = 1024)
    private String detailFilePath;

    @Column(name = "detail_visit_attempts")
    @Builder.Default
    private Integer detailVisitAttempts = 0;

    @Column(name = "kitchen_area")
    private Double kitchenArea;

    @Column(name = "living_area")
    private Double livingArea;

    @Column(name = "balcony_or_loggia")
    private String balconyOrLoggia;

    @Column(name = "rooms_type")
    private String roomsType;

    @Column(name = "bathroom_type")
    private String bathroomType;

    @Column(name = "windows_view")
    private String windowsView;

    @Column
    private String renovation;

    @Column(name = "sale_method")
    private String saleMethod;

    @Column(name = "sale_conditions")
    private String saleConditions;

    @Column(name = "ceiling_height")
    private Double ceilingHeight;

    @Column(name = "renovation_cost_estimate")
    private String renovationCostEstimate;

    @Column(name = "building_type")
    private String buildingType;

    @Column(name = "year_built")
    private Integer yearBuilt;

    @Column(name = "passenger_elevator", length = 64)
    private String passengerElevator;

    @Column(name = "freight_elevator", length = 64)
    private String freightElevator;

    @Column(name = "house_utilities")
    private String houseUtilities;

    @Column(name = "yard_features")
    private String yardFeatures;

    @Column
    private String parking;

    @Column(name = "house_rating")
    private Double houseRating;

    @Column(name = "house_reviews_count")
    private Integer houseReviewsCount;

    @Column(name = "house_catalog_url", length = 1024)
    private String houseCatalogUrl;

    @Column(name = "rosreestr_check_title")
    private String rosreestrCheckTitle;

    @Column(name = "rosreestr_checks_json", columnDefinition = "TEXT")
    private String rosreestrChecksJson;

    @Column(name = "rosreestr_owners_count_raw")
    private String rosreestrOwnersCountRaw;

    @Column(name = "rosreestr_last_owner_change_raw")
    private String rosreestrLastOwnerChangeRaw;

    @Column(name = "rosreestr_has_restrictions")
    private Boolean rosreestrHasRestrictions;

    @Column(name = "rosreestr_data_matches")
    private Boolean rosreestrDataMatches;

    @Column(name = "rosreestr_cadastral_number", length = 64)
    private String rosreestrCadastralNumber;

    @Column(name = "full_address", length = 512)
    private String fullAddress;

    @Column(name = "location_section_raw", columnDefinition = "TEXT")
    private String locationSectionRaw;

    @Column(name = "description_full_detail", columnDefinition = "TEXT")
    private String descriptionFullDetail;

    @Column(name = "contact_person_name")
    private String contactPersonName;

    @Column(name = "phone_masked", length = 64)
    private String phoneMasked;

    @Column(name = "total_views")
    private Integer totalViews;

    @Column(name = "today_views")
    private Integer todayViews;

    @Column(name = "detail_published_raw", length = 128)
    private String detailPublishedRaw;

    @Column(name = "quick_features", length = 512)
    private String quickFeatures;

    @Column(name = "detail_params_json", columnDefinition = "TEXT")
    private String detailParamsJson;

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

    public void addImage(ApartmentImage image) {
        image.setApartment(this);
        this.images.add(image);
    }

    public void replaceImages(Iterable<ApartmentImage> newImages) {
        this.images.clear();
        if (newImages != null) {
            for (ApartmentImage img : newImages) {
                addImage(img);
            }
        }
    }

    public void addBadge(ApartmentBadge badge) {
        badge.setApartment(this);
        this.badges.add(badge);
    }

    public void replaceBadges(Iterable<ApartmentBadge> newBadges) {
        this.badges.clear();
        if (newBadges != null) {
            for (ApartmentBadge b : newBadges) {
                addBadge(b);
            }
        }
    }
}