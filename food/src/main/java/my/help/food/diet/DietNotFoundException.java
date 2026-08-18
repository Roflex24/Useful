package my.help.food.diet;

public class DietNotFoundException extends RuntimeException {
    public DietNotFoundException(Long id) {
        super("Рацион с id " + id + " не найден");
    }
}