package my.help.useful.food.micronutrients.model;

import jakarta.persistence.*;

@Entity
@Table(name = "micronutrients")
public class Micronutrient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;              // "Витамин C", "Кальций", "Омега-3 (EPA)"

    @Column(length = 500)
    private String description;       // описание для организма

    private String norm;              // суточная норма для человека

    @Column(length = 500)
    private String benefits;          // польза для здоровья

    @Column(length = 500)
    private String deficiency;        // симптомы дефицита

    @Enumerated(EnumType.STRING)
    private MicronutrientType type;   // VITAMIN, MINERAL, FATTY_ACID

    private String unit;              // мг, мкг, г

    // Конструкторы
    public Micronutrient() {}

    public Micronutrient(String name, String description, String norm, String benefits,
                         String deficiency, MicronutrientType type, String unit) {
        this.name = name;
        this.description = description;
        this.norm = norm;
        this.benefits = benefits;
        this.deficiency = deficiency;
        this.type = type;
        this.unit = unit;
    }

    // Getters и Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getNorm() { return norm; }
    public void setNorm(String norm) { this.norm = norm; }

    public String getBenefits() { return benefits; }
    public void setBenefits(String benefits) { this.benefits = benefits; }

    public String getDeficiency() { return deficiency; }
    public void setDeficiency(String deficiency) { this.deficiency = deficiency; }

    public MicronutrientType getType() { return type; }
    public void setType(MicronutrientType type) { this.type = type; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getTypeDisplay() {
        return type.getDisplayName();
    }
}