package my.help.finance.general.entity;

import jakarta.persistence.*;
import lombok.*;
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
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyFinanceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate snapshotDate;

    @Column(nullable = false)
    private Long accountId;

    private String bankName;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(columnDefinition = "TEXT")
    private String cashbacksJson;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private String createdBy;

    private BigDecimal totalAmountByBank;

    @Column(columnDefinition = "TEXT")
    private String depositJson;

    @Column(columnDefinition = "TEXT")
    private String securitiesJson;
}