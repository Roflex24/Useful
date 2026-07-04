package my.help.finance.avito;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

/**
 * Один бейдж объявления или продавца.
 * Примеры ITEM-бейджей (data-marker="iva-item/&lt;код&gt;"):
 *   "Собственник", "Проверено в Росреестре", "Кирпичный дом",
 *   "Свободная продажа", "Надёжный партнёр".
 * Примеры SELLER-бейджей (data-marker="badge-title-&lt;код&gt;"):
 *   "Документы проверены", "Реквизиты проверены", "Самозанятый", "яПомогаю".
 * GALLERY-бейджи — плашки на фото, например "Новое объявление".
 *
 * Набор и смысл кодов Авито не документирует и может меняться, поэтому
 * хранится и код (для группировки/аналитики), и человекочитаемый текст.
 */
@Entity
@Table(name = "apartment_badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApartmentBadge {

    public enum BadgeType { ITEM, SELLER, GALLERY }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private BadgeType type;

    /** Технический код бейджа, например "iva-item/39" или "badge-title-31" */
    @Column(length = 64)
    private String code;

    /** Видимый текст бейджа, например "Собственник" */
    @Column(length = 255)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    @JsonIgnore
    private Apartment apartment;
}
