package my.help.food.common.exception;

public class DietNotFoundException extends RuntimeException {
    public DietNotFoundException(Long id) {
        super("Рацион с id " + id + " не найден");
    }
}