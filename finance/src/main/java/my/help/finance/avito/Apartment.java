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
    // Обход ботом: статус посещения детальной страницы объявления
    // ------------------------------------------------------------------

    /**
     * true — бот уже открыл страницу объявления и распарсил её в поля ниже.
     * Null/false — объявление ещё стоит в очереди на обход.
     */
    @Column(name = "detail_visited")
    private Boolean detailVisited;

    /** Когда бот последний раз успешно распарсил страницу объявления. */
    @Column(name = "detail_visited_at")
    private LocalDateTime detailVisitedAt;

    /**
     * Устарело: раньше сюда писался путь к сохранённому HTML-файлу.
     * Текущая версия бота файлы не создаёт (копирует исходник через буфер
     * обмена и сразу парсит в поля ниже), но колонка оставлена — она может
     * пригодиться, если когда-нибудь снова понадобится сохранять сырой HTML.
     */
    @Column(name = "detail_file_path", length = 1024)
    private String detailFilePath;

    /** Сколько раз бот пытался открыть страницу (для диагностики зависших/сбойных попыток). */
    @Column(name = "detail_visit_attempts")
    @Builder.Default
    private Integer detailVisitAttempts = 0;

    // ------------------------------------------------------------------
    // Данные со страницы САМОГО объявления (не из выдачи поиска) —
    // заполняются AvitoDetailPageParserService после обхода ботом.
    // Раздел "О квартире" на детальной странице.
    // ------------------------------------------------------------------

    /** "Площадь кухни" в м² */
    @Column(name = "kitchen_area")
    private Double kitchenArea;

    /** "Жилая площадь" в м² */
    @Column(name = "living_area")
    private Double livingArea;

    /** "Балкон или лоджия": "балкон, лоджия" */
    @Column(name = "balcony_or_loggia", length = 255)
    private String balconyOrLoggia;

    /** "Тип комнат": "изолированные, смежные" */
    @Column(name = "rooms_type", length = 255)
    private String roomsType;

    /** "Санузел": "раздельный" / "совмещённый" */
    @Column(name = "bathroom_type", length = 255)
    private String bathroomType;

    /** "Окна": "во двор" / "на улицу" / "во двор и на улицу" */
    @Column(name = "windows_view", length = 255)
    private String windowsView;

    /** "Ремонт": "косметический" / "требует ремонта" / "евро" и т.д. */
    @Column(length = 255)
    private String renovation;

    /** "Способ продажи": "свободная" / "альтернативная" */
    @Column(name = "sale_method", length = 255)
    private String saleMethod;

    /** "Условия продажи": "возможна ипотека" и т.п. */
    @Column(name = "sale_conditions", length = 255)
    private String saleConditions;

    /** "Высота потолков" в метрах, например 2.6 */
    @Column(name = "ceiling_height")
    private Double ceilingHeight;

    /**
     * "Стоимость ремонта" — Авито показывает это отдельной ссылкой-оценкой,
     * а не простым текстом: "от 840 000 ₽ за 60 м²".
     */
    @Column(name = "renovation_cost_estimate", length = 255)
    private String renovationCostEstimate;

    // ------------------------------------------------------------------
    // Раздел "О доме" на детальной странице
    // ------------------------------------------------------------------

    /** "Тип дома": "панельный" / "кирпичный" / "монолитный" и т.д. */
    @Column(name = "building_type", length = 255)
    private String buildingType;

    /** "Год постройки" дома, например 1973 */
    @Column(name = "year_built")
    private Integer yearBuilt;

    /** "Пассажирский лифт": обычно количество ("1", "2") или "нет" */
    @Column(name = "passenger_elevator", length = 64)
    private String passengerElevator;

    /** "Грузовой лифт": количество или "нет" */
    @Column(name = "freight_elevator", length = 64)
    private String freightElevator;

    /** "В доме": коммуникации дома, например "газ" */
    @Column(name = "house_utilities", length = 255)
    private String houseUtilities;

    /** "Двор": благоустройство двора, например "детская площадка" */
    @Column(name = "yard_features", length = 255)
    private String yardFeatures;

    /** "Парковка": например "открытая во дворе" */
    @Column(length = 255)
    private String parking;

    /** Рейтинг дома (например 4.5), если Авито его показывает */
    @Column(name = "house_rating")
    private Double houseRating;

    /** Число отзывов о доме */
    @Column(name = "house_reviews_count")
    private Integer houseReviewsCount;

    /** Ссылка на карточку дома в каталоге ("Узнать больше о доме") */
    @Column(name = "house_catalog_url", length = 1024)
    private String houseCatalogUrl;

    // ------------------------------------------------------------------
    // "Проверка в Росреестре" — блок domoteka-entry-block на детальной
    // странице. Авито показывает его в виде списка коротких утверждений
    // с иконкой "done"/"attention", без стабильных отдельных полей на
    // каждое утверждение, поэтому сохраняем и сырой список, и несколько
    // наиболее полезных распознанных значений.
    // ------------------------------------------------------------------

    /** Заголовок блока, обычно "Проверка в Росреестре" */
    @Column(name = "rosreestr_check_title", length = 255)
    private String rosreestrCheckTitle;

    /** Все строки блока проверки одним JSON-массивом строк, как есть */
    @Column(name = "rosreestr_checks_json", columnDefinition = "TEXT")
    private String rosreestrChecksJson;

    /** Строка про число собственников, например "2 собственника или больше" */
    @Column(name = "rosreestr_owners_count_raw", length = 255)
    private String rosreestrOwnersCountRaw;

    /** Строка про последнюю смену собственника, например "Последняя смена собственника 15 апреля 2016" */
    @Column(name = "rosreestr_last_owner_change_raw", length = 255)
    private String rosreestrLastOwnerChangeRaw;

    /**
     * Найдены ли ограничения/обременения. true — найдены, false — Авито
     * явно написал "Не найдены ограничения и обременения", null — блок
     * либо отсутствует, либо формулировка не распознана.
     */
    @Column(name = "rosreestr_has_restrictions")
    private Boolean rosreestrHasRestrictions;

    /**
     * Совпадают ли площадь/адрес/этаж с данными Росреестра. true — Авито
     * написал "Совпадают площадь, адрес и этаж", false — явно написал
     * про несовпадение, null — не распознано.
     */
    @Column(name = "rosreestr_data_matches")
    private Boolean rosreestrDataMatches;

    /** Кадастровый номер, если Авито его показывает (часто пустой) */
    @Column(name = "rosreestr_cadastral_number", length = 64)
    private String rosreestrCadastralNumber;

    // ------------------------------------------------------------------
    // Адрес, координаты, просмотры, контакт — с детальной страницы
    // ------------------------------------------------------------------

    /** Полный адрес одной строкой: "Нижегородская обл., Нижний Новгород, ул. Буревестника, 16" */
    @Column(name = "full_address", length = 512)
    private String fullAddress;

    /**
     * Весь блок "Расположение" как есть (адрес + станции метро + время
     * пешком) — на случай, если структурированного metro-парсинга
     * недостаточно. Сырой текст, без разметки.
     */
    @Column(name = "location_section_raw", columnDefinition = "TEXT")
    private String locationSectionRaw;

    /** Полное описание с детальной страницы (описание из выдачи поиска Авито обрезает) */
    @Column(name = "description_full_detail", columnDefinition = "TEXT")
    private String descriptionFullDetail;

    /** Имя контактного лица (риелтора/собственника), если удалось распознать */
    @Column(name = "contact_person_name", length = 255)
    private String contactPersonName;

    /** Замаскированный телефон вида "8 958 XXX-XX-XX" (полный номер недоступен без клика "Показать телефон") */
    @Column(name = "phone_masked", length = 64)
    private String phoneMasked;

    /** Сколько раз объявление посмотрели всего */
    @Column(name = "total_views")
    private Integer totalViews;

    /** Сколько раз посмотрели сегодня */
    @Column(name = "today_views")
    private Integer todayViews;

    /** Дата/время публикации как показано на детальной странице: "3 июля в 10:09" */
    @Column(name = "detail_published_raw", length = 128)
    private String detailPublishedRaw;

    /** Быстрые особенности-чипсы объявления через запятую: "Изолир. комнаты, Окна во двор, Раздельный с/у" */
    @Column(name = "quick_features", length = 512)
    private String quickFeatures;

    /**
     * ВСЕ пары ключ-значение из разделов "О квартире" и "О доме" одним
     * JSON-объектом ({"Количество комнат":"3","Общая площадь":"63 м²",...}).
     * Дедуцированные поля выше — это удобные колонки для частых запросов,
     * а здесь — полный сырой набор на случай, если Авито добавит что-то
     * новое, что ещё не вынесено в отдельную колонку.
     */
    @Column(name = "detail_params_json", columnDefinition = "TEXT")
    private String detailParamsJson;

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