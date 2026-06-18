package my.help.kanban.column;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.help.kanban.project.ProjectEntity;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "columns")
public class ColumnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private int orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectId")
    private ProjectEntity project;
}
