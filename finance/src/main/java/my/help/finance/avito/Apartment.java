package my.help.finance.avito;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Сущность квартиры — объявление с Авито.
 * Используется JPA/Hibernate. Подключите lombok в зависимостях.
 *
 * Хранит максимум того, что реально присутствует в HTML карточки на
 * странице поиска (не карточка объявления, а именно строка в выдаче):
 * структурные параметры из заголовка, адрес по частям, метро с временем
 * в пути, все фото (не только первое), бейджи объявления и продавца,
 * данные продавца, статус "новое"/"продвигается" и дату публикации.
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

    /** Заголовок целиком: "3-к. квартира, 66,7 м², 9/9 эт." */
    @Column(length = 512)
    private String title;

    // ------------------------------------------------------------------
    // Структурные параметры, извлечённые из заголовка
    // ------------------------------------------------------------------

    /** Количество комнат. Null, если студия (см. {@link #studio}) или не распознано. */
    private Integer rooms;

    /** true, если это студия ("Студия, 25 м², 3/9 эт.") */
    private Boolean studio;

    /** Площадь в м², например 66.7 */
    @Column(name = "total_area")
    private Double totalArea;

    /** Этаж квартиры */
    private Integer floor;

    /** Этажность дома */
    @Column(name = "total_floors")
    private Integer totalFloors;

    // ------------------------------------------------------------------
    // Цена
    // ------------------------------------------------------------------

    /** Цена в рублях (числом), взята из itemprop="price" */
    private Long price;

    /** Цена строкой, как отображается: "7 990 000 ₽" */
    @Column(name = "price_raw", length = 64)
    private String priceRaw;

    /** Валюта из itemprop="priceCurrency", обычно "RUB" */
    @Column(length = 8)
    private String currency;

    /** Цена за квадратный метр в рублях, если Авито её показывает */
    @Column(name = "price_per_meter")
    private Long pricePerMeter;

    // ------------------------------------------------------------------
    // Адрес
    // ------------------------------------------------------------------

    /** Первая строка адреса целиком (обратная совместимость): "ул. Бекетова, 4" */
    @Column(length = 512)
    private String address;

    /** Название улицы отдельно: "ул. Бекетова" */
    @Column(length = 255)
    private String street;

    /** Ссылка на страницу улицы */
    @Column(name = "street_link", length = 1024)
    private String streetLink;

    /** Номер дома отдельно: "4" */
    @Column(name = "house_number", length = 64)
    private String houseNumber;

    /** Ссылка на карточку дома (там координаты, планировки соседей и т.д.) */
    @Column(name = "house_link", length = 1024)
    private String houseLink;

    /**
     * Вторая строка адреса целиком (обратная совместимость): либо метро
     * с временем в пути ("Горьковская, от 31 мин."), либо название района
     * ("р-н Приокский"), если метро в объявлении не указано.
     */
    @Column(length = 512)
    private String metro;

    /** Название станции метро отдельно, например "Горьковская" */
    @Column(name = "metro_name", length = 255)
    private String metroName;

    /** Ссылка на фильтр по этой станции метро */
    @Column(name = "metro_link", length = 1024)
    private String metroLink;

    /** Время до метро как строка: "от 31 мин.", "21–30 мин.", "до 5 мин." */
    @Column(name = "metro_distance_raw", length = 64)
    private String metroDistanceRaw;

    /** Время до метро в минутах (верхняя граница диапазона), если удалось распарсить */
    @Column(name = "metro_minutes")
    private Integer metroMinutes;

    /** Название района, если метро не указано: "р-н Приокский" */
    @Column(length = 255)
    private String district;

    /** Широта (координата объявления с карты Авито) */
    private Double latitude;

    /** Долгота (координата объявления с карты Авито) */
    private Double longitude;

    // ------------------------------------------------------------------
    // Описание
    // ------------------------------------------------------------------

    /** Краткое описание из meta itemprop="description" (Авито обрезает его) */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Полный текст описания, как он показан в самой карточке */
    @Column(name = "description_full", columnDefinition = "TEXT")
    private String descriptionFull;

    // ------------------------------------------------------------------
    // Ссылка и фото
    // ------------------------------------------------------------------

    /** Ссылка на объявление */
    @Column(length = 1024)
    private String url;

    /** URL главной (первой) фотографии — обратная совместимость */
    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    /** Все фотографии объявления, в порядке показа в слайдере */
    @Builder.Default
    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    @JsonIgnoreProperties("apartment")
    private Set<ApartmentImage> images = new LinkedHashSet<>();

    // ------------------------------------------------------------------
    // Бейджи (объявления и продавца)
    // ------------------------------------------------------------------

    /**
     * Бейджи самого объявления и продавца: "Собственник",
     * "Проверено в Росреестре", "Кирпичный дом", "Свободная продажа",
     * "Документы проверены", "Новое объявление" и т.д.
     * Тип бейджа — см. {@link ApartmentBadge.BadgeType}.
     */
    @Builder.Default
    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("apartment")
    private Set<ApartmentBadge> badges = new LinkedHashSet<>();

    // ------------------------------------------------------------------
    // Статусы объявления
    // ------------------------------------------------------------------

    /** true, если на фото есть плашка "Новое объявление" */
    @Column(name = "is_new")
    private Boolean isNew;

    /** true, если объявление продвигается (VAS/платное размещение) */
    @Column(name = "is_promoted")
    private Boolean isPromoted;

    /** Дата публикации как показана на Авито: "Вчера", "3 дня назад" */
    @Column(name = "published_date_raw", length = 64)
    private String publishedDateRaw;

    // ------------------------------------------------------------------
    // Продавец
    // ------------------------------------------------------------------

    /** Имя продавца или название агентства */
    @Column(name = "seller_name", length = 255)
    private String sellerName;

    /** Ссылка на профиль продавца */
    @Column(name = "seller_profile_url", length = 1024)
    private String sellerProfileUrl;

    /** Строка вида "3 завершённых объявления" под именем продавца */
    @Column(name = "seller_completed_listings_raw", length = 255)
    private String sellerCompletedListingsRaw;

    /** Число из строки выше, если удалось распарсить */
    @Column(name = "seller_completed_listings_count")
    private Integer sellerCompletedListingsCount;

    // ------------------------------------------------------------------
    // Служебные поля
    // ------------------------------------------------------------------

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

    // ------------------------------------------------------------------
    // Управление коллекциями (поддерживает обратную ссылку child -> parent)
    // ------------------------------------------------------------------

    public void addImage(ApartmentImage image) {
        image.setApartment(this);
        this.images.add(image);
    }

    /** Полностью заменяет набор фотографий (используется при upsert). */
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

    /** Полностью заменяет набор бейджей (используется при upsert). */
    public void replaceBadges(Iterable<ApartmentBadge> newBadges) {
        this.badges.clear();
        if (newBadges != null) {
            for (ApartmentBadge b : newBadges) {
                addBadge(b);
            }
        }
    }
}