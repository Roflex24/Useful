package my.help.finance.invest;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "bond_daily_data")
@Getter
@Setter
public class BondDailyData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String isin;

    @Column(nullable = false)
    private String secid;

    private String shortname;

    private Integer faceValue;

    private Double couponValue;

    private String bondTypeDisplay;

    private String maturityDate;

    private Double price;

    private Double yield;

    // Конструкторы, геттеры и сеттеры генерируются Lombok
    public BondDailyData() {
    }
}