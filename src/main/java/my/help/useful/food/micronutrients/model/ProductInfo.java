package my.help.useful.food.micronutrients.model;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "products_info")
public class ProductInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;              // название продукта

    private String category;          // категория: фрукты, овощи, рыба и т.д.

    @ElementCollection
    @CollectionTable(name = "product_micronutrients",
            joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "micronutrient_name")
    @Column(name = "amount", length = 50)
    private Map<String, String> micronutrients = new HashMap<>(); // название -> количество на 100г

    // Конструкторы
    public ProductInfo() {}

    public ProductInfo(String name, String category, Map<String, String> micronutrients) {
        this.name = name;
        this.category = category;
        this.micronutrients = micronutrients;
    }

    // Getters и Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Map<String, String> getMicronutrients() { return micronutrients; }
    public void setMicronutrients(Map<String, String> micronutrients) { this.micronutrients = micronutrients; }
}