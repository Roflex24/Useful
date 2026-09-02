package my.help.finance.invest;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface BondMapper {

    OFZBondSummary toSummary(OFZBond bond);

    OFZBondSummary entityToSummary(BondDailyData entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", source = "date")
    BondDailyData toEntity(OFZBond bond, LocalDate date);
}