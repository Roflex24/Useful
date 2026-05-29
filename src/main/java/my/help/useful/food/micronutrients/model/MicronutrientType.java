package my.help.useful.food.micronutrients.model;

public enum MicronutrientType {
    VITAMIN("Витамин"),
    MINERAL("Минерал"),
    FATTY_ACID("Жирная кислота");

    private final String displayName;

    MicronutrientType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}