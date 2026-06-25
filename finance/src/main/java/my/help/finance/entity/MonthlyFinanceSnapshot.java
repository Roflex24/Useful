// entity/MonthlyFinanceSnapshot.java
package my.help.finance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_finance_snapshots",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"snapshotDate", "accountId"})
        },
        indexes = {
                @Index(name = "idx_snapshot_date", columnList = "snapshotDate"),
                @Index(name = "idx_account_id", columnList = "accountId")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyFinanceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Дата снимка (всегда последний день месяца, например 2026-01-31)
    @Column(nullable = false)
    private LocalDate snapshotDate;

    // Данные счёта
    @Column(nullable = false)
    private Long accountId;

    private String bankName;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    @Column(columnDefinition = "TEXT")
    private String comment;

    // Кешбеки этого счёта (храним как JSON)
    @Column(columnDefinition = "TEXT")
    private String cashbacksJson;

    // Метаданные
    @CreationTimestamp
    private LocalDateTime createdAt;

    private String createdBy;

    // Для быстрой агрегации (денормализованные поля)
    private BigDecimal totalAmountByBank;  // Сумма всех счетов этого банка на момент снимка

    @Column(columnDefinition = "TEXT")
    private String depositJson;  // Информация о депозите в JSON

    @Column(columnDefinition = "TEXT")
    private String securitiesJson; // Список бумаг в JSON (для INVESTMENT)
}