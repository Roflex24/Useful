package my.help.useful.key_rate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Table(name = "key_rates")
public class KeyRateEntity {

    @Id
    private LocalDate date;

    @Column(nullable = false)
    private double keyRate;
}
