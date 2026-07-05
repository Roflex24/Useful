package my.help.finance.avito;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Одна фотография объявления. Авито в статичном HTML лениво подгружает
 * картинки через JS, но реальные URL всех фото слайдера закодированы
 * в атрибутах data-marker="slider-image/image-&lt;URL&gt;" — их может быть
 * несколько десятков на одно объявление, поэтому это отдельная таблица,
 * а не колонка.
 */
@Entity
@Table(name = "apartment_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApartmentImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** URL картинки */
    @Column(length = 1024, nullable = false)
    private String url;

    /** Порядковый номер в слайдере, начиная с 0 (0 — главное фото) */
    private Integer position;

    /**
     * @OnDelete(CASCADE) заставляет Hibernate создать FK с ON DELETE CASCADE
     * на уровне самой БД. Это важно: repository.deleteAllInBatch() в
     * ApartmentController/AvitoParserService шлёт прямой SQL DELETE, минуя
     * JPA-каскад (cascade/orphanRemoval работают только при удалении через
     * EntityManager) — без ON DELETE CASCADE такой batch-delete родителя
     * упадёт с нарушением внешнего ключа.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Apartment apartment;
}