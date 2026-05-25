package my.help.useful.key_rate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "key_rates")
public class KeyRateEntity {

    public KeyRateEntity(double keyRate, String date) {
        this.keyRate = keyRate;
        this.date = date;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private double keyRate;

    @Column(nullable = false)
    private String date;

    @Column(nullable = false)
    private LocalDate actualDate;

    @PrePersist
    protected void onCreate() {
        if (actualDate == null) {
            actualDate = LocalDate.now();
        }
    }
}
