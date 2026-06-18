package my.help.kanban.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRq {

    private String name;
    private String description;
    private LocalDateTime dateTime;
}
