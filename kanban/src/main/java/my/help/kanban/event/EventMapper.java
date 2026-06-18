package my.help.kanban.event;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventEntity rqToEntity(EventRq rq);
    EventEntity toEntity(EventModel eventModel);
    EventModel toModel(EventEntity eventEntity);
    List<EventModel> toModelList(List<EventEntity> eventEntityList);
}
