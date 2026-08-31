package my.help.kanban.idea;

import my.help.kanban.idea.dto.IdeaRq;
import my.help.kanban.idea.dto.IdeaRs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface IdeaMapper {

    IdeaRs toRs(Idea idea);

    @Mapping(target = "id", ignore = true)
    Idea toEntity(IdeaRq ideaRq);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(IdeaRq dto, @MappingTarget Idea entity);
}