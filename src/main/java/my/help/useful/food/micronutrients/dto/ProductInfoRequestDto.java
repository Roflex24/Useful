package my.help.useful.food.micronutrients.dto;

import java.util.Map;

public class ProductInfoRequestDto {
    private String name;
    private String category;
    private Map<String, String> micronutrients;

    public ProductInfoRequestDto() {}

    public ProductInfoRequestDto(String name, String category, Map<String, String> micronutrients) {
        this.name = name;
        this.category = category;
        this.micronutrients = micronutrients;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Map<String, String> getMicronutrients() { return micronutrients; }
    public void setMicronutrients(Map<String, String> micronutrients) { this.micronutrients = micronutrients; }
}