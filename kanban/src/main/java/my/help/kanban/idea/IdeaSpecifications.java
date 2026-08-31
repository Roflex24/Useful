package my.help.kanban.idea;

import jakarta.persistence.criteria.*;
import my.help.kanban.idea.enums.IdeaPriority;
import my.help.kanban.idea.enums.IdeaStatus;
import org.springframework.data.jpa.domain.Specification;

public class IdeaSpecifications {

    public static Specification<Idea> withStatus(IdeaStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Idea> withPriority(IdeaPriority priority) {
        return (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }

    public static Specification<Idea> withSearch(String search) {
        return (root, query, cb) -> {
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.exists(createTagsSubquery(root, query, cb, pattern))
            );
        };
    }

    private static Subquery<Long> createTagsSubquery(Root<Idea> root, CriteriaQuery<?> query, CriteriaBuilder cb, String pattern) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Idea> subRoot = subquery.correlate(root);
        Join<Idea, String> tagsJoin = subRoot.join("tags");
        subquery.select(cb.literal(1L))
                .where(cb.like(cb.lower(tagsJoin), pattern));
        return subquery;
    }
}