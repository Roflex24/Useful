package my.help.useful.kanban.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventModel {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime dateTime;
}
