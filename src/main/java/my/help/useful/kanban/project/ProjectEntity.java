package my.help.useful.kanban.project;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate createDate;

    @Column(nullable = false)
    private boolean archived = false;

    private LocalDate archiveDate;

    @PrePersist
    protected void onCreate() {
        createDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        if (archived && archiveDate == null) {
            archiveDate = LocalDate.now();
        } else if (!archived) {
            archiveDate = null;
        }
    }
}