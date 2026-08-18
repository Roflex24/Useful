package my.help.food.nutrients;

public class NutrientsNotFoundException extends RuntimeException {
    public NutrientsNotFoundException(String message) {
        super(message);
    }
}