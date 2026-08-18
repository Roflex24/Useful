package my.help.food.common.exception;

public class NutrientsNotFoundException extends RuntimeException {
    public NutrientsNotFoundException(String message) {
        super(message);
    }
}