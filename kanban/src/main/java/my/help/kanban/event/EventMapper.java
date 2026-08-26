package my.help.kanban.event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    EventEntity rqToEntity(EventRq rq);

    EventModel toModel(EventEntity eventEntity);
    List<EventModel> toModelList(List<EventEntity> eventEntityList);
}
