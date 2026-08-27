package my.help.kanban.task;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.help.kanban.column.Column;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String description;
    private int orderIndex;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private LocalDate createDate;
    private LocalDate closeDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "column_id")
    private Column column;


    @PrePersist
    protected void onCreate() {
        createDate = LocalDate.now();
    }
}
