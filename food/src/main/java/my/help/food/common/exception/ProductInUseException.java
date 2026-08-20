package my.help.food.common.exception;

public class ProductInUseException extends RuntimeException {
    public ProductInUseException(Long id) {
        super("Продукт с id " + id + " используется в рационах или истории питания");
    }
}
