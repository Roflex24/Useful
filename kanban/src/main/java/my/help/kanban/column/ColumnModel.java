package my.help.kanban.column;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnModel {
    private Long id;
    private String title;
    private int orderIndex;
    private Long projectId;
}