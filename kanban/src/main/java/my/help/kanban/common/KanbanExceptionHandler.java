package my.help.kanban.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class KanbanExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        log.warn("Ошибка валидации: {}", details);

        ApiError error = new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Ошибка валидации входных данных",
                details
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Ресурс не найден: {}", ex.getMessage());

        ApiError error = new ApiError(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Некорректный аргумент: {}", ex.getMessage());

        ApiError error = new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Некорректное значение параметра",
                List.of(ex.getMessage())
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableJson(HttpMessageNotReadableException ex) {
        log.warn("Некорректный JSON: {}", ex.getMostSpecificCause().getMessage());

        ApiError error = new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Некорректный формат запроса",
                List.of(ex.getMostSpecificCause().getMessage())
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Неверный тип параметра '{}': {}", ex.getName(), ex.getValue());

        ApiError error = new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Некорректное значение параметра: " + ex.getName(),
                List.of("Ожидался тип: " + (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"))
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("Нарушение целостности данных", ex);

        ApiError error = new ApiError(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Нарушение целостности данных",
                List.of("Операция не может быть выполнена из-за связанных данных")
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("Непредвиденная ошибка", ex);

        ApiError error = new ApiError(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Внутренняя ошибка сервера",
                List.of(ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}