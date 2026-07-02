package my.help.finance.avito;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Сущность квартиры — объявление с Авито.
 * Используется JPA/Hibernate. Подключите lombok в зависимостях.
 */
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

    /** Уникальный ID объявления на Авито (например, "4358940157") */
    @Column(name = "avito_id", unique = true, nullable = false, length = 64)
    private String avitoId;

    /** Заголовок: "3-к. квартира, 66,7 м², 9/9 эт." */
    @Column(length = 512)
    private String title;

    /** Цена в рублях (числом) */
    private Long price;

    /** Цена строкой: "7 990 000 ₽" */
    @Column(name = "price_raw", length = 64)
    private String priceRaw;

    /** Первая строка адреса: улица и номер дома, например "ул. Старых Производственников, 11" */
    @Column(length = 512)
    private String address;

    /**
     * Вторая строка адреса: ближайшее метро с временем в пути
     * ("Парк Культуры, 16–20 мин.") либо просто название района
     * ("р-н Приокский"), если метро в объявлении не указано.
     */
    @Column(name = "metro", length = 512)
    private String metro;

    /** Широта (координата объявления с карты Авито) */
    private Double latitude;

    /** Долгота (координата объявления с карты Авито) */
    private Double longitude;

    /** Краткое описание из объявления */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Ссылка на объявление */
    @Column(length = 1024)
    private String url;

    /** URL главной фотографии */
    @Column(name = "image_url", length = 1024)
    private String imageUrl;

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