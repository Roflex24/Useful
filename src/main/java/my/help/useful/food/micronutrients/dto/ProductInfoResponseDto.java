package my.help.useful.food.micronutrients.dto;

import my.help.useful.food.micronutrients.model.ProductInfo;

import java.util.Map;

public class ProductInfoResponseDto {
    private Long id;
    private String name;
    private String category;
    private Map<String, String> micronutrients;

    public ProductInfoResponseDto() {}

    public ProductInfoResponseDto(Long id, String name, String category, Map<String, String> micronutrients) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.micronutrients = micronutrients;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Map<String, String> getMicronutrients() { return micronutrients; }
    public void setMicronutrients(Map<String, String> micronutrients) { this.micronutrients = micronutrients; }

    public static ProductInfoResponseDto fromEntity(ProductInfo productInfo) {
        return new ProductInfoResponseDto(
                productInfo.getId(),
                productInfo.getName(),
                productInfo.getCategory(),
                productInfo.getMicronutrients()
        );
    }
}