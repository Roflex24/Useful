package my.help.useful.kanban.column;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ColumnRq {

    private String title;
    private int orderIndex;
    private Long projectId;
}
