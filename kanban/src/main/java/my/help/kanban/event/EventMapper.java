package my.help.kanban.event;

import my.help.kanban.event.dto.EventRs;
import my.help.kanban.event.dto.EventRq;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    Event rqToEntity(EventRq rq);

    EventRs toModel(Event event);
    List<EventRs> toModelList(List<Event> eventList);
}
